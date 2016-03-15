package models

import models.core.LQueryOption
import play.api.libs.json.{JsNumber, JsObject, JsString, Json}

case class IndexStats(time: Long, source: String)

case class LSearchPub(docID: Int, score: Double, info: Map[String, String])

case class LSearchStats(time: Long, lOption: LQueryOption)

case class LSearchResult(stats: LSearchStats, pubs: List[LSearchPub]) {
  def statsString(): String = Json.prettyPrint({
    JsObject(Seq(
      "time" -> JsString(stats.time.toString + "ms"),
      "queryOptions" -> stats.lOption.jsonify(),
      "found" -> JsNumber(pubs.size)
    ))
  })
}