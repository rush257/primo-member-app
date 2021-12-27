import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext
import member.http.WebServer
import member.service.DBService

object PrimoMemberApp extends App {

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val bindingFuture = new WebServer(new DBService()).start

}
