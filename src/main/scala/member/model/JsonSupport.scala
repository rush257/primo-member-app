package member.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat, RootJsonReader}

trait JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val loginFormats: RootJsonFormat[LoginDto] = jsonFormat2(LoginDto)
  implicit val memberFormats: RootJsonFormat[Member] = jsonFormat7(Member)
  implicit val loopResponseFormats: RootJsonFormat[LoopResponse] = jsonFormat2(LoopResponse)
  implicit val loopRequestFormats: RootJsonFormat[LoopRequest] = jsonFormat4(LoopRequest)
  implicit val messageFormats: RootJsonFormat[Message] = jsonFormat3(Message)
  implicit val modifyMessageFormats: RootJsonFormat[ModifyMessage] = jsonFormat4(ModifyMessage)

}
