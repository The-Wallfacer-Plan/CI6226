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

case class A1Result(stats: SearchStats, lOption: Option[LOption], tops: Array[TopEntryTy]) {
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

class A1Searcher(lOption: LOption, indexFolder: String, topN: Int) extends LSearcher(lOption, indexFolder, topN) {

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

  def selectionSort(textMap: scala.collection.mutable.Map[String, Long], topN: Int): Array[TopEntryTy] = {
    val array = Array.fill[TopEntryTy](topN)(0L -> null)
    var i = 0
    while (i < array.length) {
      var current: TopEntryTy = 0L -> null
      for (entry <- textMap) {
        if (current._1 < entry._2) {
          current = entry._2 -> entry._1
        }
      }
      array(i) = current
      textMap -= current._2
      i += 1
    }
    array
  }


  def evaluate(queryString: String, contentMap: Map[String, Option[String]], topicsField: String = I_TITLE): A1Result = {
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
    Logger.info(s"${collector.getTotalHits} hit docs")
    val timeStart = System.currentTimeMillis()
    val result = searcher.search(query, math.max(1, collector.getTotalHits))
    val tops = getTopFreq(result, topicsField)
    val duration = System.currentTimeMillis() - timeStart
    reader.close()
    val stats = SearchStats(duration, Some(query))
    new A1Result(stats, Some(lOption), tops)
  }


  private def getTopFreq(topDocs: TopDocs, topicsField: String): Array[TopEntryTy] = {
    //    val termMap = new util.HashMap[String, Long]()
    val termMap = mutable.Map.empty[String, Long]
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
          if (!ignoredTerms.contains(termText)) {
            val tf = itr.totalTermFreq()
            if (termMap.contains(termText)) {
              termMap += termText -> (termMap(termText) + tf)
            } else {
              termMap += termText -> tf
            }
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
    selectionSort(termMap, topN)
  }

}
