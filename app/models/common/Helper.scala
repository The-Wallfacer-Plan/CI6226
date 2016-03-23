package models.common

object Helper {
  def getAsBoolean(option: Option[String], defaultB: Boolean): Boolean = {
    option match {
      case Some(arg) => arg.equalsIgnoreCase("true")
      case None => defaultB
    }
  }

}
