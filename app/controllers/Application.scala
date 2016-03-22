package controllers

import models.common.{Config, LOption, TopFreq}
import models.index._
import models.search.{LSearchResult, LSearchStats, LSearcher}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._

class Application extends Controller {

  val inputFile = Config.xmlFile
  val indexFolder = {
    val fileName = inputFile.split(java.io.File.separator).last
    Config.indexRoot + java.io.File.separator + fileName.split('.')(0)
  }

  @inline
  private def getAsBoolean(option: Option[String], defaultB: Boolean): Boolean = {
    option match {
      case Some(arg) => arg.equalsIgnoreCase("true")
      case None => defaultB
    }
  }

  def searchDoc = Action { request => {
    request.getQueryString("content") match {
      case Some(queryString) if queryString.length != 0 => {
        val searchOption = {
          val stemming = getAsBoolean(request.getQueryString("stem"), defaultB = true)
          val ignoreCase = getAsBoolean(request.getQueryString("ignore"), defaultB = true)
          val isEval = getAsBoolean(request.getQueryString("isEval"), defaultB = false)
          val swDict = request.getQueryString("swDict").get
          new LOption(stemming, ignoreCase, swDict, isEval)
        }
        val topN = request.getQueryString("topN").get.toInt
        val searcher = new LSearcher(searchOption, indexFolder, topN)
        Logger.info(s"queryString=${queryString.length}")
        val res = searcher.search(queryString)
        Ok(views.html.home(res))
      }
      case _ => {
        val result = new LSearchResult(LSearchStats(0, "", None), None, Array())
        Ok(views.html.home(result))
      }
    }
  }
  }

  def indexDoc = Action(parse.json) { request => {
    val body = request.body
    val indexer = new LIndexer(inputFile)
    val indexOption = {
      val stemming = (body \ "stem").as[Boolean]
      val ignoreCase = (body \ "ignore").as[Boolean]
      val swDict = (body \ "swDict").as[String]
      new LOption(stemming, ignoreCase, swDict, false)
    }
    val worker = LIndexWorker(indexOption, indexFolder)

    val stats = indexer.run(worker)
    val indexInfo = new LDocInfoReader(indexFolder)
    val fieldInfo = indexInfo.getFieldInfo("title")
    ///
    val misc = new TopFreq(indexFolder)
    misc.analyze("title")
    ///
    val res = JsObject(Seq(
      "stats" -> stats.toJson(),
      "docInfo" -> LDocInfoReader.toJson(fieldInfo),
      "options" -> indexOption.toJson()
    ))
    Logger.debug(s"info: $res")
    Ok(res)
  }
  }

  def app1 = Action { request => {
    Ok(views.html.app())
  }
  }


}
