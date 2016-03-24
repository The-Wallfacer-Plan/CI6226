package models.search

import models.common.{Config, LOption}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search._
import play.api.Logger
import play.api.libs.json._

import scala.collection.JavaConversions._


case class BSearchPub(docID: Int, score: Double, info: Map[String, String])

case class BSearchStats(time: Long, queryString: String, query: Option[Query]) {
  def toJson(): JsValue = {
    val parsedQuery = {
      query match {
        case Some(q) => JsString(q.toString())
        case None => JsNull
      }
    }
    JsObject(Seq(
      "time" -> JsString(time.toString + "ms"),
      "queryString" -> JsString(queryString),
      "parsedQuery" -> parsedQuery
    ))
  }
}

class BSearchResult(stats: BSearchStats, lOption: Option[LOption], val pubs: Array[BSearchPub]) {
  def toJson(): JsValue = {
    val lOptionJson = {
      lOption match {
        case Some(l) => l.toJson()
        case None => JsNull
      }
    }
    JsObject(Seq(
      "stats" -> stats.toJson(),
      "found" -> JsNumber(pubs.length),
      "lOption" -> lOptionJson
    ))
  }

  def toJsonString(): String = {
    Json.prettyPrint(toJson())
  }
}

class BSearcher(lOption: LOption, indexFolderString: String, topN: Int) extends LSearcher(lOption, indexFolderString, topN) {


  def search(queryString: String): BSearchResult = {
    val startTime = System.currentTimeMillis()
    validate(queryString)
    val queryOrNone = Option {
      val parser = new QueryParser(Config.COMBINED_FIELD, analyzer)
      parser.setAllowLeadingWildcard(true)
      parser.parse(queryString)
    }
    Logger.info(s"string=$queryString, query=$queryOrNone")
    queryOrNone match {
      case None => {
        val searchStats = BSearchStats(0, queryString, queryOrNone)
        new BSearchResult(searchStats, Some(lOption), Array())
      }
      case Some(query) => {
        val allDocCollector = new TotalHitCountCollector()
        searcher.search(query, allDocCollector)
        val topDocs = searcher.search(query, topN)
        Logger.info(s"${allDocCollector.getTotalHits} hit docs")
        val duration = System.currentTimeMillis() - startTime
        val foundPubs = getSearchPub(topDocs, query)
        reader.close()
        val searchStats = BSearchStats(duration, queryString, queryOrNone)
        new BSearchResult(searchStats, Some(lOption), foundPubs)
      }
    }
  }

  def getSearchPub(topDocs: TopDocs, query: Query): Array[BSearchPub] = {
    for (hit <- topDocs.scoreDocs) yield {
      val (docID, score) = (hit.doc, hit.score)
      val hitDoc = searcher.doc(docID)
      //      Logger.info(s"Explain: ${searcher.explain(query, docID)}")
      val fieldValues = for {
        field <- hitDoc.getFields
        if field.name() != Config.COMBINED_FIELD
      } yield field.name() -> field.stringValue()
      val fieldDocMap = Map(fieldValues: _*)
      new BSearchPub(docID, score, fieldDocMap)
    }
  }

}
