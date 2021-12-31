package member.http

import akka.actor.ActorSystem
import akka.event
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, NotFound}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{entity, path, _}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import member.config.Application.{hostName, port}
import member.model.{JsonSupport, LoginDto, LoopRequest, Member, Message, ModifyMessage}
import member.service.DBService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class WebServer(val dbService: DBService)
               (implicit val system: ActorSystem, materializer: ActorMaterializer, val ec: ExecutionContext) extends JsonSupport {
  def start: Future[Http.ServerBinding] = Http().newServerAt(hostName, port).bind(routes)

  import spray.json._

  lazy val log = event.Logging(system, classOf[WebServer])

  lazy val routes: Route = {
    pathPrefix("member") {
      concat(
        path("login") {
          {
            get {
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
        },
        path("loops") {
          get {
            parameter(Symbol("memberId"), Symbol("organization"), Symbol("account")) { (memberId, organization, account) =>
              log.info("Received the loops request for memberId: {}, organization: {}, account: {}", memberId, organization, account)
              dbService.getLoops(memberId, organization, account) match {
                case Success(dbResponse) => dbResponse match {
                  case loops =>
                    log.debug("Sending response for memberId: {} and loops: {}", memberId, loops.toString())
                    complete(HttpEntity(ContentTypes.`application/json`, loops.toJson.toString()))
                }
                case Failure(error) =>
                  log.error("Error while fetching loops member: {} {}", memberId + error.getMessage)
                  complete(HttpResponse(InternalServerError, entity = "Internal Server Error"))
              }
            }
          }
        },
        path("subscription") {
          post {
            parameter(Symbol("memberId"), Symbol("loopId")) { (memberId, loopId) =>
              log.info("Received the subscription request for memberId: {}, loopId: {}", memberId, loopId)
              dbService.createSubscription(memberId, loopId) match {
                case Success(dbResponse) => dbResponse match {
                  case Some(successMsg) =>
                    log.debug("Sending subscription response for memberId: {} and msg: {}", memberId, successMsg)
                    complete(HttpEntity(ContentTypes.`application/json`, successMsg))
                  case None =>
                    log.error("subscription not updated for memberId: {} ", memberId)
                    complete(StatusCodes.NotFound)
                }
                case Failure(error) =>
                  log.error("Error while subscription member: {} {}", memberId + error.getMessage)
                  complete(HttpResponse(InternalServerError, entity = "Internal Server Error"))
              }
            }
          }
        }
      )
    } ~
      pathPrefix("admin") {
        concat(
          path("loop") {
            {
              post {
                entity(as[LoopRequest]) { dto => {
                  log.info("Received the create loop request for organization: {}", dto.organizationId)
                  dbService.createLoop(dto) match {
                    case Success(dbResponse) => dbResponse match {
                      case Some(loginSuccess) =>
                        log.debug("Sending loop response for organization: {}", dto.organizationId)
                        complete(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, loginSuccess)))
                      case None =>
                        log.info("loop has not created for organization: {}", dto.organizationId)
                        complete(HttpResponse(NotFound, entity = HttpEntity(ContentTypes.`application/json`, "loop not found")))
                    }
                    case Failure(error) =>
                      log.error("Error while creating loop for organization: {}", dto.organizationId)
                      complete(HttpResponse(InternalServerError, entity = "Internal Server Error"))
                  }
                }
                }
              }
            }
          },
          path("sendNewMessage") {
            {
              post {
                entity(as[Message]) { dto => {
                  log.info("Received the sendNewMessage request for loopId: {}", dto.loopId)
                  dbService.sendMessage(dto) match {
                    case Success(dbResponse) => dbResponse match {
                      case Some(loginSuccess) =>
                        log.debug("Sending sendNewMessage response for loopId: {}", dto.loopId)
                        complete(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, loginSuccess)))
                      case None =>
                        log.info("msg has not sent for loopId: {}", dto.loopId)
                        complete(HttpResponse(NotFound, entity = HttpEntity(ContentTypes.`application/json`, "loop not found")))
                    }
                    case Failure(error) =>
                      log.error("Error while sending msg for loopId: {}", dto.loopId)
                      complete(HttpResponse(InternalServerError, entity = "Internal Server Error"))
                  }
                }
                }
              }
            }
          },
          path("sendModifyMessage") {
            {
              post {
                entity(as[ModifyMessage]) { dto => {
                  log.info("Received the sendNewMessage request for loopId: {}", dto.loopId)
                  dbService.sendModifyMessage(dto) match {
                    case Success(dbResponse) => dbResponse match {
                      case Some(loginSuccess) =>
                        log.debug("Sending sendModifyMessage response for loopId: {}", dto.loopId)
                        complete(HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, loginSuccess)))
                      case None =>
                        log.info("modify msg has not sent for loopId: {}", dto.loopId)
                        complete(HttpResponse(NotFound, entity = HttpEntity(ContentTypes.`application/json`, "msg not found")))
                    }
                    case Failure(error) =>
                      log.error("Error while sending modify msg for loopId: {}", dto.loopId)
                      complete(HttpResponse(InternalServerError, entity = "Internal Server Error"))
                  }
                }
                }
              }
            }
          }
        )
      } ~
      path("getNewMessage") {
        {
          get {
            parameter(Symbol("msgId")) { msgId => {
              log.info("Received the getNewMessage request for msgId: {}", msgId)
              dbService.findNewMessage(msgId) match {
                case Success(dbResponse) => dbResponse match {
                  case Some(msg) =>
                    log.debug("Sending getNewMessage response for msgId: {} and msg: {}", msgId, msg)
                    complete(HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, msg)))
                  case None =>
                    log.info("msg not found for msg: {} ", msgId)
                    complete(HttpResponse(NotFound, entity = HttpEntity(ContentTypes.`application/json`, "msg not found")))
                }
                case Failure(error) =>
                  log.error("Error while fetching msg: {} {}", msgId, error.getMessage)
                  complete(HttpResponse(InternalServerError, entity = "Internal Server Error"))
              }
            }
            }
          }
        }
      } ~
      path("getModifyMessage") {
        {
          get {
            parameter(Symbol("msgId")) { msgId => {
              log.info("Received the getChangeMessage request for msgId: {}", msgId)
              dbService.findChangeMessage(msgId) match {
                case Success(dbResponse) => dbResponse match {
                  case Some(msg) =>
                    log.debug("Sending getChangeMessage response for msgId: {} and msg: {}", msgId, msg)
                    complete(HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, msg)))
                  case None =>
                    log.info("msg not found for msg: {} ", msgId)
                    complete(HttpResponse(NotFound, entity = HttpEntity(ContentTypes.`application/json`, "msg not found")))
                }
                case Failure(error) =>
                  log.error("Error while fetching msg: {} {}", msgId, error.getMessage)
                  complete(HttpResponse(InternalServerError, entity = "Internal Server Error"))
              }
            }
            }
          }
        }

      }
  }
}

