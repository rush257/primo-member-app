package member.config

import com.typesafe.config.ConfigFactory

object MessageConfig {
  val conf = ConfigFactory.load("messages.conf")

  /* Success Mesages*/
  val loginSuccess = conf.getString("successMessages.loginSuccess")
  val registerMemberSuccess = conf.getString("successMessages.registerMemberSuccess")
  val updateMemberSuccess = conf.getString("successMessages.updateMemberSuccess")
  val subscriptionMemberSuccess = conf.getString("successMessages.subscriptionMemberSuccess")
  val loopCreationSuccess = conf.getString("successMessages.loopCreationSuccess")
  val newMsgSendSuccess = conf.getString("successMessages.newMsgSendSuccess")
  val modifyMsgSendSuccess = conf.getString("successMessages.modifyMsgSendSuccess")

  val userNotFound = conf.getString("failureMessages.userNotFound")
}

