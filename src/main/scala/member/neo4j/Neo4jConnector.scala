package member.neo4j

import member.config.Application.{neo4jPassword, neo4jUrl, neo4jUsername}
import org.neo4j.driver.{AuthTokens, GraphDatabase, Result}

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object Neo4jConnector {

  implicit val ec: ExecutionContextExecutor =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(50))

  val driver = GraphDatabase.driver(neo4jUrl, AuthTokens.basic(neo4jUsername, neo4jPassword))

  def getNeo4j(script: String): Result = {
    driver.session().run(script)
  }

  def createNode(script: String): Int = {
    driver.session().run(script).consume().counters().nodesCreated()
  }

  def updateNode(script: String): Int = {
    driver.session().run(script).consume().counters().propertiesSet()
  }

}