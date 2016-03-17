package models

import models.core.{LOption, LQueryInfo}
import play.api.libs.json.{JsNumber, JsObject, JsString, Json}

case class IndexStats(time: Long, source: String)

case class LSearchPub(docID: Int, score: Double, info: Map[String, String])

case class LSearchStats(time: Long, queryInfo: LQueryInfo)

class LSearchResult(stats: LSearchStats, lOption: Option[LOption], val pubs: List[LSearchPub]) {
  def statsString(): String = {
    val lOptionJson = {
      lOption match {
        case Some(l) => l.toJson()
        case None => JsString("NIL")
      }
    }
    val js = JsObject(Seq(
      "time" -> JsString(stats.time.toString + "ms"),
      "queryOptions" -> stats.queryInfo.toJson(),
      "found" -> JsNumber(pubs.size),
      "searchOption" -> lOptionJson
    ))
    Json.prettyPrint(js)
  }
}