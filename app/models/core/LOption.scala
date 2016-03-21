package models.core

import play.api.libs.json._

case class LOption(stemming: Boolean, ignoreCase: Boolean, swDict: String) {
  def this() = this(true, true, "None")

  def toJson(): JsValue = {
    this match {
      case null => JsNull
      case _ => {
        JsObject(Seq(
          "stemming" -> JsBoolean(stemming),
          "ignoreCase" -> JsBoolean(ignoreCase),
          "swDict" -> JsString(swDict)
        ))
      }
    }
  }
}