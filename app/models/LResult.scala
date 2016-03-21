package models

import models.core.LOption
import play.api.libs.json.{JsNumber, JsObject, JsString, Json}

case class LIndexStats(time: Long, source: String)

case class LSearchPub(docID: Int, score: Double, info: Map[String, String])

case class LSearchStats(time: Long, queryString: String)

class LSearchResult(stats: LSearchStats, lOption: Option[LOption], val pubs: Array[LSearchPub]) {
  def statsString(): String = {
    val lOptionJson = {
      lOption match {
        case Some(l) => l.toJson()
        case None => JsString("NIL")
      }
    }
    val js = JsObject(Seq(
      "time" -> JsString(stats.time.toString + "ms"),
      "queryString" -> JsString(stats.queryString),
      "found" -> JsNumber(pubs.length),
      "searchOption" -> lOptionJson
    ))
    Json.prettyPrint(js)
  }
}