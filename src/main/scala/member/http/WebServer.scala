package member.http

import akka.actor.ActorSystem
import akka.event
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, NotFound}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import member.config.Application.{hostName, port}
import member.model.{JsonSupport, LoginDto, Member}
import member.service.DBService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class WebServer(val dbService: DBService)
               (implicit val system: ActorSystem, materializer: ActorMaterializer, val ec: ExecutionContext) extends JsonSupport {
  def start: Future[Http.ServerBinding] = Http().newServerAt(hostName, port).bind(routes)

  lazy val log = event.Logging(system, classOf[WebServer])

  lazy val routes: Route =
    concat(
      path("login") {
        {
          post {
            entity(as[LoginDto]) { dto => {
              log.info("Received the login request for member: {}", dto.email)
              dbService.findMemberByEmailAndPassword(dto.email, dto.password) match {
                case Success(dbResponse) => dbResponse match {
                  case Some(loginSuccess) =>
                    log.debug("Sending login response for member: {} and msg: {}", dto.email, loginSuccess)
                    complete(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, loginSuccess)))
                  case None =>
                    log.info("user not found for member: {} ", dto.email)
                    complete(HttpResponse(NotFound, entity = HttpEntity(ContentTypes.`application/json`, "user not found")))
                }
                case Failure(error) =>
                  log.error("Error while fetching member: {} {}", dto.email + error.getMessage)
                  complete(HttpResponse(InternalServerError, entity = "Internal Server Error"))
              }
            }
            }
          }
        }
      },
      path("register") {
        {
          post {
            entity(as[Member]) { member => {
              log.info("Received the register request for member: {}", member.primary_email)
              dbService.registerMember(member) match {
                case Success(dbResponse) => dbResponse match {
                  case Some(successMsg) =>
                    log.debug("Sending register response for member: {} and msg: {}", member.primary_email, successMsg)
                    complete(HttpEntity(ContentTypes.`application/json`, successMsg))
                  case None =>
                    log.error("user not created for member: {} ", member.primary_email)
                    complete(StatusCodes.NotFound)
                }
                case Failure(error) =>
                  log.error("Error while creating member: {} {}", member.primary_email + error.getMessage)
                  complete(HttpResponse(InternalServerError, entity = "Internal Server Error"))
              }
            }
            }
          }
        }
      }
      ,
      path("verifyEmail") {
        post {
          parameter(Symbol("memberId")) { memberId =>
            log.info("Received the verifyEmail request for memberId: {}", memberId)
            dbService.updateMember(memberId, "primary_email_validated") match {
              case Success(dbResponse) => dbResponse match {
                case Some(successMsg) =>
                  log.debug("Sending verifyEmail response for memberId: {} and msg: {}", memberId, successMsg)
                  complete(HttpEntity(ContentTypes.`application/json`, successMsg))
                case None =>
                  log.error("verifyEmail not updated for memberId: {} ", memberId)
                  complete(StatusCodes.NotFound)
              }
              case Failure(error) =>
                log.error("Error while verifyEmail member: {} {}", memberId + error.getMessage)
                complete(HttpResponse(InternalServerError, entity = "Internal Server Error"))
            }
          }
        }
      }
      ,
      path("verifyNewMessageArrival") {
        post {
          parameter(Symbol("memberId")) { memberId =>
            log.info("Received the verifyNewMessageArrival request for memberId: {}", memberId)
            dbService.updateMember(memberId, "notify_me_by_email_newMessageArrival") match {
              case Success(dbResponse) => dbResponse match {
                case Some(successMsg) =>
                  log.debug("Sending verifyNewMessageArrival response for memberId: {} and msg: {}", memberId, successMsg)
                  complete(HttpEntity(ContentTypes.`application/json`, successMsg))
                case None =>
                  log.error("verifyNewMessageArrival not updated for memberId: {} ", memberId)
                  complete(StatusCodes.NotFound)
              }
              case Failure(error) =>
                log.error("Error while verifyNewMessageArrival member: {} {}", memberId + error.getMessage)
                complete(HttpResponse(InternalServerError, entity = "Internal Server Error"))
            }
          }
        }
      }
      ,
      path("verifyChangeMessageArrival") {
        post {
          parameter(Symbol("memberId")) { memberId =>
            log.info("Received the verifyChangeMessageArrival request for memberId: {}", memberId)
            dbService.updateMember(memberId, "notify_me_by_email_ChangeMessage") match {
              case Success(dbResponse) => dbResponse match {
                case Some(successMsg) =>
                  log.debug("Sending verifyChangeMessageArrival response for memberId: {} and msg: {}", memberId, successMsg)
                  complete(HttpEntity(ContentTypes.`application/json`, successMsg))
                case None =>
                  log.error("verifyChangeMessageArrival not updated for memberId: {} ", memberId)
                  complete(StatusCodes.NotFound)
              }
              case Failure(error) =>
                log.error("Error while verifyChangeMessageArrival member: {} {}", memberId + error.getMessage)
                complete(HttpResponse(InternalServerError, entity = "Internal Server Error"))
            }
          }
        }
      }
    )
}

