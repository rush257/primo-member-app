package member.service

import akka.actor.ActorSystem
import akka.event
import member.config.Application.{hostName, port}
import member.config.MessageConfig.{loopCreationSuccess, modifyMsgSendSuccess, newMsgSendSuccess, registerMemberSuccess, subscriptionMemberSuccess, updateMemberSuccess}
import member.model.{LoopRequest, LoopResponse, Member, Message, ModifyMessage}
import member.neo4j.Neo4jConnector.{createNode, getNeo4j, updateNode, updateRelation}

import scala.collection.JavaConverters._
import scala.util.Try

class DBService(implicit val system: ActorSystem) {

  lazy val log = event.Logging(system, classOf[DBService])

  def findMemberByEmailAndPassword(email: String, password: String): Try[Option[String]] = {

    val script = s"MATCH (member:Member {primary_email:'${email}',password : '${password}'}) RETURN ID(member) as memberId"
    Try({
      val result = getNeo4j(script)
      log.info("finding member by email: {} ", email)
      if (result.hasNext) {
        Some(result.next().get("memberId").toString)
      } else
        None
    })
  }

  def registerMember(member: Member): Try[Option[String]] = {
    val script = s"MERGE (${member.firstName}:Member { password : '${member.password}', primary_email : '${member.primary_email}', primary_email_validated : ${member.primary_email_validated},notify_me_by_email_newMessageArrival : ${member.notify_me_by_email_newMessageArrival} ,notify_me_by_email_ChangeMessage : ${member.notify_me_by_email_ChangeMessage}, firstName : '${member.firstName}', lastName : '${member.lastName}'});"
    Try({
      log.info("creating member by email: {} ", member.primary_email)
      val result = createNode(script)
      if (result > 0)
        Some(registerMemberSuccess)
      else
        None
    })
  }

  def updateMember(memberId: String, fieldName: String): Try[Option[String]] = {
    val script = s"MATCH (member) WHERE ID(member)= ${memberId} SET member.${fieldName} = true RETURN member;"
    Try({
      log.info("updating member by Id: {} and fieldName: {}", memberId, fieldName)
      val result = updateNode(script)
      if (result > 0)
        Some(updateMemberSuccess)
      else
        None
    })
  }

  def getLoops(memberId: String, organization: String, account: String): Try[List[LoopResponse]] = {

    val script =
      s"""MATCH (organization:Organization) - [:CREATED] -> (account:Account) - [:CREATED] -> (loop:Loop)
    MATCH (member:Member)
    WHERE organization.name =~ '.*${organization}.*'
    AND account.name =~ '.*${account}.*'
    AND ID(member) = ${memberId}
    AND NOT (member)-[:SUBSCRIBED]-(loop)
    RETURN ID(loop) as ID, loop.name as Name"""

    Try({
      log.info("finding loops by memberId: {}, organization: {}, account: {}", memberId, organization, account)
      val results = getNeo4j(script)
      results.asScala.toList.map(result => LoopResponse(result.get("ID").toString, result.get("Name").toString))

    })
  }

  def createSubscription(memberId: String, loopId: String): Try[Option[String]] = {

    val script =
      s"""MATCH (loop:Loop)
    MATCH (member:Member)
    WHERE ID(loop) = ${loopId} and ID(member) =  ${memberId}
    MERGE (member) - [sub:SUBSCRIBED] -> (loop)"""

    Try({
      log.info("create subscription loops by memberId: {}, loopId: {}", memberId, loopId)
      val result = updateRelation(script)

      if (result > 0)
        Some(subscriptionMemberSuccess)
      else
        None

    })
  }

  def createLoop(loopRequest: LoopRequest): Try[Option[String]] = {

    val script =
      s"""MATCH (organization:Organization)
         MATCH (account:Account)
         WHERE ID(organization) = ${loopRequest.organizationId}
         AND ID(account) = ${loopRequest.accountId}
         AND (organization)-[:CREATED]-(account)
         CREATE (loop:Loop{name:'${loopRequest.name}', desc: '${loopRequest.desc}'})
         CREATE (account)-[:CREATED]->(loop)"""

    Try({
      log.info("create loop loops for organizationId: {}, accountId: {}", loopRequest.organizationId, loopRequest.accountId)
      val result = createNode(script)

      if (result > 0)
        Some(loopCreationSuccess)
      else
        None

    })
  }

  def sendMessage(msg: Message): Try[Option[String]] = {

    val script =
      s"""MATCH (member:Member{primary_email_validated:true,notify_me_by_email_newMessageArrival : true, notify_me_by_email_ChangeMessage: true}) - [:SUBSCRIBED] -> (loop:Loop)
      WHERE ID(loop) = ${msg.loopId}
      MERGE (loop) - [:Notified] -> (msg:MSG{text:'${msg.text}', category:'${msg.category}' , created_at : date()})
      CREATE (msg) - [:CREATED] -> (member)
      RETURN ID(member) as memberId, ID(msg) as msgId, member.primary_email as emailId
      """

    Try({
      log.info("send msg for loopId: {}", msg.loopId)
      val results = getNeo4j(script)
      results.asScala.toList.map(result => {
        val msgId = result.get("msgId").toString
        val emailId = result.get("emailId").toString

        MailerAPI.sendEmail(emailId, "/getNewMessage?msgId=" + msgId)

      })
      Some(newMsgSendSuccess)
    })
  }

  def sendModifyMessage(msg: ModifyMessage): Try[Option[String]] = {

    val script =
      s"""MATCH (member:Member{primary_email_validated:true,notify_me_by_email_newMessageArrival : true, notify_me_by_email_ChangeMessage: true}) - [:SUBSCRIBED] -> (loop:Loop) - [:Notified] -> (msg:MSG)
      WHERE ID(loop) = ${msg.loopId} AND
      ID(msg) = ${msg.msgId}
      set msg.modifiedText = '${msg.text}', msg.category = '${msg.category}'
      CREATE (msg) - [:MODIFIED] -> (member)
      RETURN member.primary_email
      """

    Try({
      log.info("send modify msg for loopId: {}", msg.loopId)
      val results = getNeo4j(script)
      results.asScala.toList.map(result => {
        val emailId = result.get("emailId").toString

        MailerAPI.sendEmail(emailId, "/getModifyMessage?msgId=" + msg.msgId)

      })
      Some(modifyMsgSendSuccess)
    })
  }

  def findNewMessage(msgId: String): Try[Option[String]] = {

    val script =
      s"""MATCH (msg:MSG)
      WHERE ID(msg) = ${msgId}
      RETURN msg.text as text, msg.category as category
      """

    Try({
      log.info("get  msg for msgId: {}", msgId)
      val results = getNeo4j(script)

      if (results.hasNext) {
        val result = results.next()
        val msgText = result.get("text").toString
        Some(s"<h1> $msgText </h1>")
      } else
        None
    }

    )
  }

  def findChangeMessage(msgId: String): Try[Option[String]] = {

    val script =
      s"""MATCH (msg:MSG)
      WHERE ID(msg) = ${msgId}
      RETURN msg.modifiedText as text, msg.category as category
      """

    Try({
      log.info("get  msg for msgId: {}", msgId)
      val results = getNeo4j(script)

      if (results.hasNext) {
        val result = results.next()
        val msgText = result.get("text").toString
        Some(s"<h1> $msgText </h1>")
      } else
        None
    }

    )
  }

}
