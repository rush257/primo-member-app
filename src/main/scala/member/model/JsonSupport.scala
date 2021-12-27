package member.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat, RootJsonReader}

trait JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val loginFormats: RootJsonFormat[LoginDto] = jsonFormat2(LoginDto)
  implicit val memberFormats: RootJsonFormat[Member] = jsonFormat7(Member)

}
