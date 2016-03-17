package models.core

import play.api.libs.json._

case class LOption(stemming: Boolean, ignoreCase: Boolean, swDict: String) {
  def this() = this(true, true, "None")

  def toJson(): JsValue = {
    if (this == null) {
      JsString("")
    } else {
      JsObject(Seq(
        "stemming" -> JsBoolean(stemming),
        "ignoreCase" -> JsBoolean(ignoreCase),
        "swDict" -> JsString(swDict)
      ))
    }
  }
}