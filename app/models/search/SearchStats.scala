package models.search

import org.apache.lucene.search.Query
import play.api.libs.json.{JsNull, JsObject, JsString, JsValue}


case class SearchStats(time: Long, query: Option[Query]) {
  def toJson(): JsValue = {
    val parsedQuery = {
      query match {
        case Some(q) => JsString(q.toString())
        case None => JsNull
      }
    }
    JsObject(Seq(
      "time" -> JsString(time.toString + "ms"),
      "parsedQuery" -> parsedQuery
    ))
  }
}
