package controllers

import models.common.{Config, LOption}
import models.index._
import models.search._
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
      case Some(queryContent) if queryContent.length != 0 => {
        val lOption = LOption(request)
        val topN = request.getQueryString("topN").get.toInt
        val searcher = new LSearcher(lOption, indexFolder, topN)
        Logger.info(s"queryContent=$queryContent")
        val res = searcher.search(queryContent)
        Ok(views.html.bMain(res))
      }
      case _ => {
        val result = new LSearchResult(LSearchStats(0, "", None), None, Array())
        Ok(views.html.bMain(result))
      }
    }
  }
  }


  def topRecord = Action { request => {
    request.getQueryString(Config.I_PUB_YEAR) match {
      case Some(pubYear) => {
        val lOption = LOption(request)
        val topN = request.getQueryString("topN").get.toInt
        val attrContentMap = Map(Config.I_VENUE -> request.getQueryString(Config.I_VENUE), Config.I_AUTHORS -> request.getQueryString(Config.I_AUTHORS))
        val topRecorder = new LTopRecorder(lOption, indexFolder, topN)
        val result = topRecorder.evaluate(pubYear, attrContentMap)
        Ok(views.html.aMain(result))
      }
      case None => {
        val stats = LTopRecordStats(0L, None)
        val result = LTopRecordResult(stats, None, Array.empty)
        Ok(views.html.aMain(result))
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
    val fieldInfo = indexInfo.getFieldInfo(Config.I_TITLE)
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


}
