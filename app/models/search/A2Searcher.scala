package models.search

import models.common.Config._
import models.common.LOption
import org.apache.lucene.search.BooleanQuery.Builder
import org.apache.lucene.search.TopDocs
import play.api.Logger
import play.api.libs.json._

case class A2Result(stats: SearchStats, lOption: Option[LOption], docs: Array[A2DocTy]) {
  def toJson(): JsValue = {
    val lOptionJson = {
      lOption match {
        case Some(l) => l.toJson()
        case None => JsNull
      }
    }
    JsObject(Seq(
      "stats" -> stats.toJson(),
      "lOption" -> lOptionJson
    ))
  }

  def toJsonString(): String = Json.prettyPrint(toJson())
}


class A2Searcher(lOption: LOption, indexFolderString: String, topN: Int) extends LSearcher(lOption, indexFolderString, topN) {

  def search(contentMap: Map[String, Option[String]]): A2Result = {
    val startTime = System.currentTimeMillis()
    val queryBuilder = new Builder()
    addTermQuery(contentMap, queryBuilder)
    val query = queryBuilder.build()
    Logger.info(s"contentMap=$contentMap query=$query")
    val topDocs = searcher.search(query, topN)
    val a2Docs = getA2Docs(topDocs)
    val duration = System.currentTimeMillis() - startTime
    val stats = SearchStats(duration, Some(query))
    A2Result(stats, Some(lOption), a2Docs)
  }

  private def getA2Docs(topDocs: TopDocs): Array[A2DocTy] = {
    val scoreDocs = topDocs.scoreDocs
    for (scoreDoc <- scoreDocs) {
      val scoreDoc = scoreDocs(0)
      val docID = scoreDoc.doc
      val hitDoc = searcher.doc(docID)
      val fields = hitDoc.getFields(I_TITLE)
      for (field <- fields) {
        println(field.stringValue())
      }
    }
    Array.empty
  }

}
