package controllers

import models.core._
import models.utility.Config
import models.{LSearchResult, LSearchStats}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._

class Application extends Controller {

  val inputFile = Config.xmlFile
  val indexFolder = {
    val fileName = inputFile.split(java.io.File.separator).last
    Config.indexRoot + java.io.File.separator + fileName.split('.')(0)
  }

  def searchDoc = Action { request => {
    request.getQueryString("content") match {
      case Some(queryString) => {
        val searchOption = {
          val stemming = request.getQueryString("stem").get.equalsIgnoreCase("true")
          val ignoreCase = request.getQueryString("ignore").get.equalsIgnoreCase("true")
          val swDict = request.getQueryString("swDict").get
          new LOption(stemming, ignoreCase, swDict)
        }
        val topN = request.getQueryString("topN").get.toInt
        val searcher = new LSearcher(searchOption, indexFolder, topN)
        val res = searcher.searchImp(queryString)
        Ok(views.html.home(res))
      }
      case None => {
        val result = new LSearchResult(LSearchStats(0, ""), None, Array())
        Ok(views.html.home(result))
      }
    }
  }
  }

  def indexDoc = Action(parse.json) { request => {
    val body = request.body
    val driver = new LIndexDriver(inputFile)
    val indexOption = {
      val stemming = (body \ "stem").as[Boolean]
      val ignoreCase = (body \ "ignore").as[Boolean]
      val swDict = (body \ "swDict").as[String]
      new LOption(stemming, ignoreCase, swDict)
    }
    val indexer = LIndexer(indexOption, indexFolder)

    val stats = driver.run(indexer)
    val statistics = new LIndexEval(indexFolder)
    val fieldStats = statistics.getFieldStats("title")
    val res = JsObject(Seq(
      "status" -> JsString("OK"),
      "index time" -> JsString(stats.time + "ms"),
      "index file" -> JsString(stats.source),
      "stats" -> fieldStats,
      "options" -> indexOption.toJson()
    ))
    Logger.info(s"info: $res")
    val misc = new MISC(indexFolder)
    misc.analyze("title")
    Ok(res)
  }
  }

}
