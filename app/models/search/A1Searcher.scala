package models.search

import models.common.Config.TopEntryTy
import models.common.{Config, LOption}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search._
import play.api.Logger
import play.api.libs.json._

case class A1Result(stats: SearchStats, lOption: Option[LOption], tops: Array[TopEntryTy]) {
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
    val tops = getTopics(result, topicsField)
    val duration = System.currentTimeMillis() - timeStart
    reader.close()
    val stats = SearchStats(duration, Some(query), "OK")
    new A1Result(stats, Some(lOption), tops)
  }

  private def getTopics(topicDocs: TopDocs, topicsField: String): Array[TopEntryTy] = {
    //
    //    val a1Term = A1Term(topicDocs, searcher, reader)
    //    a1Term.run(sOption.topN, I_TITLE)
    //
    val instanceList = A1Mallet.getProcessedInstances(topicDocs, searcher)
    //    A1Mallet.getInstanceData(instanceList)
    //
    val malletOption = MalletOption(100, 1.0, 0.1, 6, 60)
    val a1Mallet = new A1Mallet(instanceList, malletOption)
    a1Mallet.run(sOption.topN)
    //
    //    val a1PoST = A1PoST(topicDocs, searcher, reader)
    //    a1PoST.run(sOption.topN)
    //    a1PoST.dealWith(instanceList)
    //    a1PoST.run(instanceList, sOption.topN)
    Array.empty
  }

}
