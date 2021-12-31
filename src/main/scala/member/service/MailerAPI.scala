package member.service

import java.util.Properties
import javax.mail._
import javax.mail.internet._
import member.config.Application.{emailSubject, hostName, port, senderEmail, senderHostname, senderpassword}

object MailerAPI {

  def sendEmail(recipientEmailAddress: String, urlPrefix: String): Option[String] = {

    val url = "http://"+ hostName + ":" + port + urlPrefix
    val properties = new Properties()
    properties.setProperty("mail.smtp.starttls.enable", "true")
    val session = Session.getDefaultInstance(properties)
    try {
      val message = new MimeMessage(session)
      message.setFrom(new InternetAddress(senderEmail))
      message.addRecipient(Message.RecipientType.TO, new InternetAddress("vumattefixo-1975@yopmail.com"))
      message.setSubject(emailSubject)
      message.setHeader("Content-Type", "text/plain;")
      message.setContent(s"<a href=${url}>Please find primo deals here</a>", "text/html")
      val transport = session.getTransport("smtp")
      transport.connect(senderHostname, senderEmail, senderpassword)
      transport.sendMessage(message, message.getAllRecipients)

      Some("Successfully Email Sent")
    }
    catch {
      case exception: Exception =>
        None
    }

  }


}
