package models.search

import models.common.Config.TopEntryTy
import models.common.{Config, LOption}
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.{Query, TopDocs, TotalHitCountCollector}
import org.apache.lucene.util.BytesRef
import play.api.Logger
import play.api.libs.json._

import scala.collection.mutable
import scala.util.control.Breaks.{break, breakable}


case class LTopRecordStats(time: Long, query: Option[Query]) {
  def toJson(): JsValue = {
    val queryResult = query match {
      case Some(q) => JsString(q.toString)
      case None => JsNull
    }
    JsObject(Seq(
      "time" -> JsString(time.toString + "ms"),
      "query" -> queryResult
    ))
  }
}

case class LTopRecordResult(stats: LTopRecordStats, lOption: Option[LOption], tops: Array[TopEntryTy]) {
  def toJson(): JsValue = {
    val lOptionjson = {
      lOption match {
        case Some(l) => l.toJson()
        case None => JsNull
      }
    }
    JsObject(Seq(
      "stats" -> stats.toJson(),
      "lOption" -> lOptionjson
    ))
  }

  def toJsonString(): String = Json.prettyPrint(toJson())
}

class LTopRecorder(lOption: LOption, indexFolder: String, topN: Int) extends LSBase(lOption, indexFolder, topN) {

  import Config.TopEntryTy

  def reOrder(entry: TopEntryTy, topsArray: Array[TopEntryTy]): Unit = {
    var i = 0
    breakable {
      while (i < topsArray.length) {
        if (entry._1 > topsArray(i)._1) {
          topsArray(i) = entry
          break
        }
        i += 1
      }
    }
  }


  def evaluate(queryString: String, contentMap: Map[String, Option[String]], topicsField: String = Config.I_TITLE): LTopRecordResult = {
    val queryOrNone = Option {
      val parser = new QueryParser(Config.I_PUB_YEAR, analyzer)
      parser.setAllowLeadingWildcard(false)
      parser.parse(queryString)
    }
    Logger.info(s"string=$queryString, query=$queryOrNone")
    queryOrNone match {
      case None => {
        val stats = LTopRecordStats(0L, queryOrNone)
        val tops = Array.empty[TopEntryTy]
        LTopRecordResult(stats, Some(lOption), tops)
      }
      case Some(query) => {
        val collector = new TotalHitCountCollector()
        searcher.search(query, collector)
        Logger.info(s"${collector.getTotalHits}")
        val timeStart = System.currentTimeMillis()
        val result = searcher.search(query, math.max(1, collector.getTotalHits))
        val tops = getTopFreq(result, topicsField)
        val duration = System.currentTimeMillis() - timeStart
        reader.close()
        val stats = LTopRecordStats(duration, queryOrNone)
        new LTopRecordResult(stats, Some(lOption), tops)
      }
    }
  }


  private def getTopFreq(topDocs: TopDocs, topicsField: String): Array[TopEntryTy] = {
    var visitedSet = mutable.Set[String]()
    val topsArray = Array.fill[TopEntryTy](topN)(0L -> null)
    val scoreDocs = topDocs.scoreDocs
    var i = 0
    while (i < scoreDocs.size) {
      val scoreDoc = scoreDocs(i)
      val docID = scoreDoc.doc
      val terms = reader.getTermVector(docID, topicsField)
      if (terms != null) {
        Logger.debug(s"=${searcher.doc(docID).get(topicsField)} ${scoreDocs.size}")
        val itr = terms.iterator()
        var bytesRef: BytesRef = itr.next()
        while (bytesRef != null) {
          val termText = bytesRef.utf8ToString()
          if (!Config.ignoredTerms.contains(termText) && !visitedSet.contains(termText)) {
            val termInstance = new Term(topicsField, bytesRef)
            val tf = reader.totalTermFreq(termInstance)
            reOrder((tf, termText), topsArray)
            visitedSet += termText
          }
          bytesRef = itr.next()
        }
      } else {
        Logger.info(s"$topicsField null TermVector: $topicsField=${searcher.doc(docID).get(topicsField)}, paperId=${searcher.doc(docID).get(Config.I_PAPER_ID)}")
      }
      i += 1
    }
    topsArray
  }

}
