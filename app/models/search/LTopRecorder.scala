package models.search

import models.common.Config.TopEntryTy
import models.common.{Config, LOption}
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanQuery.Builder
import org.apache.lucene.search._
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

  import Config._

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


  def evaluate(queryString: String, contentMap: Map[String, Option[String]], topicsField: String = I_TITLE): LTopRecordResult = {
    val pubYearTerm = new Term(I_PUB_YEAR, queryString)
    val pubYearQuery = new TermQuery(pubYearTerm)
    val queryBuilder = new Builder().add(pubYearQuery, BooleanClause.Occur.MUST)
    for (entry <- contentMap) {
      entry._2 match {
        case Some(content) => {
          val term = new Term(entry._1, content)
          val contentQuery = new TermQuery(term)
          queryBuilder.add(contentQuery, BooleanClause.Occur.MUST)
        }
        case None =>
      }
    }
    val query = queryBuilder.build()
    val collector = new TotalHitCountCollector()
    searcher.search(query, collector)
    Logger.info(s"${collector.getTotalHits}")
    val timeStart = System.currentTimeMillis()
    val result = searcher.search(query, math.max(1, collector.getTotalHits))
    val tops = getTopFreq(result, topicsField)
    val duration = System.currentTimeMillis() - timeStart
    reader.close()
    val stats = LTopRecordStats(duration, Some(query))
    new LTopRecordResult(stats, Some(lOption), tops)
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
          if (!ignoredTerms.contains(termText) && !visitedSet.contains(termText)) {
            val termInstance = new Term(topicsField, bytesRef)
            val tf = reader.totalTermFreq(termInstance)
            //            require(tf == itr.totalTermFreq(), (tf, itr.totalTermFreq()))
            reOrder((tf, termText), topsArray)
            visitedSet += termText
          }
          bytesRef = itr.next()
        }
      } else {
        Logger.info(s"$topicsField null TermVector: $topicsField=${
          searcher.doc(docID).get(topicsField)
        }, paperId=${
          searcher.doc(docID).get(I_PAPER_ID)
        }")
      }
      i += 1
    }
    topsArray
  }

}
