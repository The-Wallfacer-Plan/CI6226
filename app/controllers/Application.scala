package controllers

import java.nio.file.{Files, Paths}

import models.common.Config._
import models.common.{Config, LOption}
import models.index._
import models.search._
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._

import scala.sys.process.Process

class Application extends Controller {

  val inputFile = Config.xmlFile
  val bIndexFolder = {
    val fileName = inputFile.split(java.io.File.separator).last
    Config.indexRoot + java.io.File.separator + "b" + java.io.File.separator + fileName.split('.')(0)
  }
  val a2IndexFolder = {
    val fileName = inputFile.split(java.io.File.separator).last
    Config.indexRoot + java.io.File.separator + "a2" + java.io.File.separator + fileName.split('.')(0)
  }

  def bSearch = Action { request => {
    request.getQueryString("content") match {
      case Some(queryContent) if queryContent.length != 0 => {
        val lOption = LOption(request)
        val topN = request.getQueryString("topN").get.toInt
        val searcher = new BSearcher(lOption, bIndexFolder, topN)
        Logger.info(s"queryContent:\t$queryContent")
        val res = searcher.search(queryContent)
        Ok(views.html.bMain(res))
      }
      case _ => {
        val result = new BResult(SearchStats(0, None), 0, "", None, Array.empty)
        Ok(views.html.bMain(result))
      }
    }
  }
  }


  def a1Search = Action { request => {
    request.getQueryString(Config.I_PUB_YEAR) match {
      case Some(pubYear) => {
        val lOption = LOption(request)
        val topN = request.getQueryString("topN").get.toInt
        val attrContentMap = Map(I_VENUE -> request.getQueryString(I_VENUE), I_AUTHORS -> request.getQueryString(I_AUTHORS), I_PUB_YEAR -> Some(pubYear))
        val topRecorder = new A1Searcher(lOption, bIndexFolder, topN)
        val result = topRecorder.evaluate(attrContentMap)
        Ok(views.html.a1Main(result))
      }
      case None => {
        val stats = SearchStats(0L, None)
        val result = A1Result(stats, None, Array.empty)
        Ok(views.html.a1Main(result))
      }
    }
  }
  }

  def bIndex = Action(parse.json) { request => {
    val body = request.body
    val indexer = new LIndexer(inputFile)
    val reIndex = (body \ "reIndex").as[Boolean]
    val lOption = LOption(body)

    val indexExists = {
      val indexPath = Paths.get(bIndexFolder)
      Files.exists(indexPath)
    }
    if ((indexExists && reIndex) || !indexExists) {
      Process(s"rm -rf $bIndexFolder").!
      val worker = BIndexWorker(lOption, bIndexFolder)

      val stats = indexer.run(worker)
      val fieldInfo = {
        val indexInfo = new BDocInfoReader(bIndexFolder)
        indexInfo.getFieldInfo(Config.I_TITLE)
      }
      ///
      val res = JsObject(Seq(
        "stats" -> stats.toJson(),
        "docInfo" -> BDocInfoReader.toJson(fieldInfo),
        "options" -> lOption.toJson()
      ))
      Logger.debug(s"info: $res")
      Ok(res)
    } else {
      val res = JsObject(Seq(
        "stats" -> JsString("index folder exists"),
        "folder" -> JsString(bIndexFolder)
      ))
      Ok(res)
    }
  }
  }

  def a2Search = Action { request => {
    val attrContentMap = Map(I_PUB_YEAR -> request.getQueryString(I_PUB_YEAR), I_VENUE -> request.getQueryString(I_VENUE))
    val isValid = attrContentMap.values.forall(_.isDefined)
    if (isValid) {
      val lOption = LOption(request)
      val topN = request.getQueryString("topN").get.toInt
      val searcher = new A2Searcher(lOption, a2IndexFolder, topN)
      val res = searcher.search(attrContentMap)
      Ok(views.html.a2Main(res))
    } else {
      val stats = SearchStats(0L, None)
      val res = A2Result(stats, None, Map.empty)
      Logger.info("invalid query")
      Ok(views.html.a2Main(res))
    }
  }
  }

  def a2Index = Action(parse.json) { request => {
    val body = request.body
    val indexer = new LIndexer(inputFile)
    val reIndex = (body \ "reIndex").as[Boolean]
    val lOption = LOption(body)

    val indexExists = {
      val indexPath = Paths.get(a2IndexFolder)
      Files.exists(indexPath)
    }
    if ((indexExists && reIndex) || !indexExists) {
      Process(s"rm -rf $a2IndexFolder").!
      val worker = A2IndexWorker(lOption, a2IndexFolder)

      val stats = indexer.run(worker)
      ///
      val res = JsObject(Seq(
        "stats" -> stats.toJson(),
        "options" -> lOption.toJson()
      ))
      Logger.debug(s"info: $res")
      Ok(res)
    } else {
      val res = JsObject(Seq(
        "stats" -> JsString("index folder exists"),
        "folder" -> JsString(a2IndexFolder)
      ))
      Ok(res)
    }
  }
  }

}
