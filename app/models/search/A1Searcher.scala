package models.search

import models.common.{Config, LOption}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search._
import play.api.Logger
import play.api.libs.json._

case class A1Result(stats: SearchStats, lOption: Option[LOption], termResult: A1TermResult, malletResult: MalletResult) {
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


class A1Searcher(lOption: LOption, sOption: SOption, indexFolder: String) extends LSearcher(lOption, sOption, indexFolder) {

  import Config._

  def run(contentMap: Map[String, Option[String]], topicsField: String = I_TITLE): A1Result = {
    val queryString = getMustQuery(contentMap)
    val query = new QueryParser(I_PUB_YEAR, analyzer).parse(queryString)
    val collector = new TotalHitCountCollector()
    val timeStart = System.currentTimeMillis()
    searcher.search(query, collector)
    Logger.info(s"${collector.getTotalHits} hit docs")
    val result = searcher.search(query, math.max(1, collector.getTotalHits))
    val termResult = getTermResults(result, topicsField)
    val malletResult = getMalletResults(result, topicsField)
    val duration = System.currentTimeMillis() - timeStart
    reader.close()
    val stats = SearchStats(duration, Some(query), "OK")
    new A1Result(stats, Some(lOption), termResult, malletResult)
  }

  private def getMalletResults(topDocs: TopDocs, topicsField: String): MalletResult = {
    val instanceList = A1Mallet.getProcessedInstances(topDocs, searcher)
    val a1Mallet = new A1Mallet(instanceList)
    a1Mallet.run(sOption.topN)
  }

  private def getTermResults(topicDocs: TopDocs, topicsField: String): A1TermResult = {
    val a1Term = A1Term(topicDocs, searcher, reader)
    a1Term.run(sOption.topN, I_TITLE)
  }

}
