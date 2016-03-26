package models.common

import java.io.{PrintWriter, StringWriter}

object Helper {

  def getStackTrack(exception: Throwable): String = {
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    exception.printStackTrace(pw)
    sw.toString
  }

  class LError(val msg: String) extends Exception(msg)

}
