package models.common

import play.api.libs.json._
import play.api.mvc._

case class LOption(stemming: Boolean, ignoreCase: Boolean, swDict: String) {

  def toJson(): JsValue = {
    this match {
      case null => JsNull
      case _ => {
        JsObject(Seq(
          "stemming" -> JsBoolean(stemming),
          "lowerCase" -> JsBoolean(ignoreCase),
          "swDict" -> JsString(swDict)
        ))
      }
    }
  }
}

object LOption {

  private def getAsBoolean(option: Option[String], defaultB: Boolean): Boolean = {
    option match {
      case Some(arg) => arg.equalsIgnoreCase("true")
      case None => defaultB
    }
  }

  def apply(request: Request[AnyContent]): LOption = {
    val stemming = getAsBoolean(request.getQueryString("stem"), defaultB = true)
    val ignoreCase = getAsBoolean(request.getQueryString("ignore"), defaultB = true)
    val swDict = request.getQueryString("swDict").get
    new LOption(stemming, ignoreCase, swDict)
  }

  def apply(body: JsValue): LOption = {
    val stemming = (body \ "stem").as[Boolean]
    val ignoreCase = (body \ "ignore").as[Boolean]
    val swDict = (body \ "swDict").as[String]
    new LOption(stemming, ignoreCase, swDict)
  }
}