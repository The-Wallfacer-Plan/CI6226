package models.common

import play.api.libs.json._

case class LOption(stemming: Boolean, ignoreCase: Boolean, swDict: String) {
  def this() = this(true, true, "None")

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