package models.common

import play.api.libs.json._

case class LOption(stemming: Boolean, ignoreCase: Boolean, swDict: String, evaluate: Boolean) {

  def toJson(): JsValue = {
    this match {
      case null => JsNull
      case _ => {
        JsObject(Seq(
          "stemming" -> JsBoolean(stemming),
          "lowerCase" -> JsBoolean(ignoreCase),
          "swDict" -> JsString(swDict),
          "evaluate" -> JsBoolean(evaluate)
        ))
      }
    }
  }
}