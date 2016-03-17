package models.core

import play.api.libs.json._

case class LOption(stemming: Boolean, ignoreCase: Boolean, swDict: String) {
  def toJson(): JsValue = {
    if (this == null) {
      JsNull
    } else {
      JsObject(Seq(
        "stemming" -> JsBoolean(stemming),
        "ignoreCase" -> JsBoolean(ignoreCase),
        "swDict" -> JsString(swDict)
      ))
    }
  }
}