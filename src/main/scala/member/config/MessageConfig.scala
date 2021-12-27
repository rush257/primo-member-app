package member.config

import com.typesafe.config.ConfigFactory

object MessageConfig {
  val conf = ConfigFactory.load("messages.conf")

  /* Success Mesages*/
  val loginSuccess = conf.getString("successMessages.loginSuccess")
  val registerMemberSuccess = conf.getString("successMessages.registerMemberSuccess")
  val updateMemberSuccess = conf.getString("successMessages.updateMemberSuccess")

  val userNotFound = conf.getString("failureMessages.userNotFound")
}

