name := "primo-member-app"

version := "0.1"

scalaVersion := "2.13.7"

val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.7"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
  "org.neo4j.driver" % "neo4j-java-driver" % "4.4.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.sun.mail" % "javax.mail" % "1.6.2"

)

assemblyJarName in assembly := "primo-member-app-1.0.jar"

mainClass in(Compile, run) := Some("PrimoMemberApp")

assemblyMergeStrategy in assembly := {
  case PathList("reference.conf") => MergeStrategy.concat
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case "application.conf" => MergeStrategy.concat
  case x => MergeStrategy.first
}
