package models.search

import models.common.Config._
import models.common.{Config, LOption}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search._
import play.api.Logger
import play.api.libs.json._

case class A1Result(stats: SearchStats, lOption: Option[LOption], topEntries: Array[TopEntryTy]) {
  def toJson(): JsValue = {
    val lOptionJson = {
      lOption match {
        case Some(l) => l.toJson()
        case None => JsNull
      }
    }
    JsObject(Seq(
      "stats" -> stats.toJson(),
      "lOption" -> lOptionJson,
      "listed" -> JsNumber(topEntries.length)
    ))
  }

  def toJsonString(): String = Json.prettyPrint(toJson())
}


class A1Searcher(lOption: LOption, sOption: SOption, indexFolder: String) extends LSearcher(lOption, sOption, indexFolder) {

  import Config._

  def run(contentMap: Map[String, Option[String]], topicsField: String = I_TITLE): A1Result = {
    val queryString = getMustQuery(contentMap)
    val query = new QueryParser(I_PUB_YEAR, analyzer).parse(queryString)
    val collector = new TotalHitCountCollector()
    val timeStart = System.currentTimeMillis()
    searcher.search(query, collector)
    Logger.info(s"${collector.getTotalHits} hit docs")
    val topDocs = searcher.search(query, math.max(1, collector.getTotalHits))
    val ngramResults = {
      val a1Mallet = A1Mallet(topDocs, searcher)
      a1Mallet.runNGrams(sOption.topN, defaultNGramSizes)
    }
    val duration = System.currentTimeMillis() - timeStart
    reader.close()
    val stats = SearchStats(duration, Some(query), "OK")
    new A1Result(stats, Some(lOption), ngramResults)
  }

}
