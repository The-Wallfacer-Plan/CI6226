package controllers

import models.common.{Config, LOption}
import models.index._
import models.search.{LSearchResult, LSearchStats, LSearcher, LTopRecorder}
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
      case Some(queryString) if queryString.length != 0 => {
        val lOption = LOption(request)
        val topN = request.getQueryString("topN").get.toInt
        val searcher = new LSearcher(lOption, indexFolder, topN)
        Logger.info(s"queryString=$queryString")
        val res = searcher.search(queryString)
        Ok(views.html.bMain(res))
      }
      case _ => {
        val result = new LSearchResult(LSearchStats(0, "", None), None, Array())
        Ok(views.html.bMain(result))
      }
    }
  }
  }

  def indexDoc = Action(parse.json) { request => {
    val body = request.body
    val indexer = new LIndexer(inputFile)
    val lOption = LOption(body)
    val worker = LIndexWorker(lOption, indexFolder)

    val stats = indexer.run(worker)
    val indexInfo = new LDocInfoReader(indexFolder)
    val fieldInfo = indexInfo.getFieldInfo("title")
    ///
    val res = JsObject(Seq(
      "stats" -> stats.toJson(),
      "docInfo" -> LDocInfoReader.toJson(fieldInfo),
      "options" -> lOption.toJson()
    ))
    Logger.debug(s"info: $res")
    Ok(res)
  }
  }

  def app1 = Action { request => {
    val lOption = LOption(request)
    val topN = request.getQueryString("topN").get.toInt
    val topRecorder = new LTopRecorder(lOption, indexFolder, topN)
    topRecorder.evaluate(queryString = null)
    Ok(views.html.aMain())
  }
  }


}
