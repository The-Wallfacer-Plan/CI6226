package models.search

import models.common.Config._
import models.common.LOption
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search._
import play.api.Logger
import play.api.libs.json._


case class BPub(docID: Int, score: Double, info: Map[String, String])

class BResult(stats: SearchStats, hits: Int, queryString: String, lOption: Option[LOption], val pubs: Array[BPub]) {
  def toJson(): JsValue = {
    val lOptionJson = {
      lOption match {
        case Some(l) => l.toJson()
        case None => JsNull
      }
    }
    JsObject(Seq(
      "stats" -> stats.toJson(),
      "queryString" -> JsString(queryString),
      "found" -> JsNumber(hits),
      "listed" -> JsNumber(pubs.length),
      "lOption" -> lOptionJson
    ))
  }

  def toJsonString(): String = Json.prettyPrint(toJson())
}

class BSearcher(lOption: LOption, indexFolderString: String, topN: Int) extends LSearcher(lOption, indexFolderString, topN) {


  def search(queryString: String): BResult = {
    val startTime = System.currentTimeMillis()
    validate(queryString)
    val queryOrNone = Option {
      val parser = new QueryParser(I_ALL, analyzer)
      parser.setAllowLeadingWildcard(true)
      parser.parse(queryString)
    }
    Logger.info(s"string=$queryString, query=$queryOrNone")
    queryOrNone match {
      case None => {
        val searchStats = SearchStats(0, queryOrNone)
        new BResult(searchStats, 0, queryString, Some(lOption), Array())
      }
      case Some(query) => {
        val allDocCollector = new TotalHitCountCollector()
        searcher.search(query, allDocCollector)
        val topDocs = searcher.search(query, topN)
        Logger.info(s"${allDocCollector.getTotalHits} hit docs")
        val duration = System.currentTimeMillis() - startTime
        val foundPubs = getSearchPub(topDocs, query)
        reader.close()
        val searchStats = SearchStats(duration, queryOrNone)
        new BResult(searchStats, allDocCollector.getTotalHits, queryString, Some(lOption), foundPubs)
      }
    }
  }

  def getSearchPub(topDocs: TopDocs, query: Query): Array[BPub] = {
    for (hit <- topDocs.scoreDocs) yield {
      val (docID, score) = (hit.doc, hit.score)
      val hitDoc = searcher.doc(docID)
      Logger.debug(s"Explain: ${searcher.explain(query, docID)}")
      val fieldValues = for (fieldName <- allFields) yield {
        fieldName match {
          case I_AUTHORS => {
            fieldName -> hitDoc.getFields(I_AUTHORS).map(_.stringValue()).mkString(";")
          }
          case _ => fieldName -> hitDoc.getField(fieldName).stringValue()
        }
      }
      val fieldDocMap = Map(fieldValues: _*)
      new BPub(docID, score, fieldDocMap)
    }
  }

}
