package member.service

import akka.actor.ActorSystem
import akka.event
import member.config.MessageConfig.{registerMemberSuccess, updateMemberSuccess}
import member.model.Member
import member.neo4j.Neo4jConnector.{createNode, getNeo4j, updateNode}

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


}
