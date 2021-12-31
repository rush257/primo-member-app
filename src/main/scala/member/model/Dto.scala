package member.model

case class LoginDto(email: String, password: String)

case class Member(primary_email: String, password: String, firstName: String, lastName: String, primary_email_validated: Boolean, notify_me_by_email_newMessageArrival: Boolean, notify_me_by_email_ChangeMessage: Boolean)

case class LoopResponse(id: String, name: String)

case class LoopRequest(name: String, desc: String, organizationId: String, accountId: String)

case class Message(text: String, category: String, loopId: String)

case class ModifyMessage(text: String, category: String, loopId: String, msgId: String)