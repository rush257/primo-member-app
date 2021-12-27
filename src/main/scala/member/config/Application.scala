package member.config

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Future

object Application {
  val config: Config = ConfigFactory.load

  val hostName: String = config.getString("server.hostName")
  val port: Int = config.getString("server.port").toInt

  //neo4j config
  val neo4jUrl = config.getString("neo4j.url")
  val neo4jUsername = config.getString("neo4j.username")
  var neo4jPassword = config.getString("neo4j.password")


}
