package models.search

import models.common.Config._
import models.common.LOption
import org.apache.lucene.queries.mlt.MoreLikeThis
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.TopDocs
import play.api.Logger
import play.api.libs.json._

case class A2Result(stats: SearchStats, lOption: Option[LOption], docs: Map[String, Array[A2DocTy]]) {
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


class A2Searcher(lOption: LOption, sOption: SOption, indexFolderString: String) extends LSearcher(lOption, sOption, indexFolderString) {

  def search(contentMap: Map[String, Option[String]]): A2Result = {
    val startTime = System.currentTimeMillis()
    val queryString = getMustQuery(contentMap)
    val query = new QueryParser(I_PUB_YEAR, analyzer).parse(queryString)
    Logger.info(s"contentMap=$contentMap query=$query")
    val topDocs = searcher.search(query, sOption.topN)
    val a2Docs = getA2Docs(topDocs)
    val duration = System.currentTimeMillis() - startTime
    val stats = SearchStats(duration, Some(query))
    A2Result(stats, Some(lOption), a2Docs)
  }

  def getSimilarDocs(docID: Int): Array[A2DocTy] = {
    val mlt = new MoreLikeThis(reader)
    mlt.setFieldNames(Array(I_TITLE))
    val query = mlt.like(docID)
    val similarDocs = searcher.search(query, sOption.topN + 1)
    for (similarDoc <- similarDocs.scoreDocs; if docID != similarDoc.doc) yield {
      val docID = similarDoc.doc
      val score = similarDoc.score
      val hitDoc = searcher.doc(docID)
      val venue = hitDoc.get(I_VENUE)
      val pubYear = hitDoc.get(I_PUB_YEAR)
      (venue + ", " + pubYear, score)
    }
  }

  private def getA2Docs(topDocs: TopDocs): Map[String, Array[A2DocTy]] = {
    val scoreDocs = topDocs.scoreDocs
    Logger.info(s"# of matched docs: ${scoreDocs.length}")
    val arrayInfo = for (scoreDoc <- scoreDocs) yield {
      val docID = scoreDoc.doc
      val hitDoc = searcher.doc(docID)
      val venue = hitDoc.get(I_VENUE)
      val pubYear = hitDoc.get(I_PUB_YEAR)
      val docInfo = venue + " " + pubYear
      Logger.info(s"$docInfo")
      val similarDocs = getSimilarDocs(docID)
      docInfo -> similarDocs
    }
    arrayInfo.toMap
  }

}
