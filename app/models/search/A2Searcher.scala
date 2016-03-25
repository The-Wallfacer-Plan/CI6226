package models.search

import models.common.Config._
import models.common.LOption
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanQuery.Builder
import org.apache.lucene.search.{BooleanClause, TermQuery, TopDocs}
import play.api.libs.json._

class A3SearchResult(stats: SearchStats, queryString: String, lOption: Option[LOption], docs: Array[A2DocTy]) {
  def toString: JsValue = {
    val lOptionJson = {
      lOption match {
        case Some(l) => l.toJson(),
        case None => JsNull
      }
    }
    JsObject(Seq(
      "stats" -> stats.toJson(),
      "queryString" -> JsString(queryString),
      "lOption" -> lOptionJson
    ))
  }
}


class A2Searcher(lOption: LOption, indexFolderString: String, topN: Int) extends LSearcher(lOption, indexFolderString, topN) {

  // TODO write as in functional way
  def parseQuery(queryString: String): Map[String, String] = {
    val ss = queryString.split("\\s+")
    require(ss.length == 2)
    val pubYear = try {
      Some(ss.last.toInt)
    } catch {
      case e: NumberFormatException => None
    }
    require(pubYear.isDefined)
    Map(I_VENUE -> ss.head, I_PUB_YEAR -> ss.last)
  }

  def search(queryString: String): Array[A2DocTy] = {
    val contentMap = parseQuery(queryString)
    val queryBuilder = new Builder()
    for (entry <- contentMap) {
      val term = new Term(entry._1, entry._2)
      val termQuery = new TermQuery(term)
      queryBuilder.add(termQuery, BooleanClause.Occur.MUST)
    }
    val query = queryBuilder.build()
    val topDocs = searcher.search(query, topN)
    val result = getA2Result(topDocs)
    Array.empty
  }

  private def getA2Result(topDocs: TopDocs) = {
    val scoreDocs = topDocs.scoreDocs
    require(scoreDocs.length == 1)
    val scoreDoc = scoreDocs(0)
    val docID = scoreDoc.doc
    val hitDoc = searcher.doc(docID)
    val fields = hitDoc.getFields(I_TITLE)
    for (field <- fields) {
      println(field.stringValue())
    }
  }

}
