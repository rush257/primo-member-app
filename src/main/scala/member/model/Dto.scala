package member.model

case class LoginDto(email: String, password: String)

case class Member(primary_email: String, password: String, firstName: String, lastName: String, primary_email_validated: Boolean, notify_me_by_email_newMessageArrival: Boolean, notify_me_by_email_ChangeMessage: Boolean)