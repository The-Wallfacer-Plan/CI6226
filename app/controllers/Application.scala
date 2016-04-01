package controllers

import java.nio.file.{Files, Paths}

import models.common.Config._
import models.common.Helper._
import models.common.{Config, LOption}
import models.index._
import models.search._
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._

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
    val contentOpt = request.getQueryString("content")
    try {
      if (contentOpt.isEmpty || contentOpt.get.length == 0) {
        throw new LError("[query] not exist or its value empty")
      }
      val queryContent = contentOpt.get
      val lOption = LOption(request)
      val sOption = SOption(request)
      val searcher = new BSearcher(lOption, sOption, bIndexFolder)
      Logger.info(s"queryContent:\t$queryContent")
      val res = searcher.search(queryContent)
      Ok(views.html.bMain(res))
    } catch {
      case e: Exception => {
        Logger.warn(s"Error: ${e.getStackTrace.mkString("\n")}")
        val msg = e.toString
        val result = new BResult(SearchStats(0, None, msg), 0, "", None, Array.empty)
        Ok(views.html.bMain(result))
      }
    }
  }
  }


  def a1Search = Action { request => {
    val pubYearOpt = request.getQueryString(I_PUB_YEAR)
    try {
      if (pubYearOpt.isEmpty) {
        throw new LError(s"[$I_PUB_YEAR] should be nonEmpty")
      }
      val lOption = LOption(request)
      val sOption = SOption(request)
      val attrContentMap = Map(I_VENUE -> request.getQueryString(I_VENUE), I_AUTHORS -> request.getQueryString(I_AUTHORS), I_PUB_YEAR -> pubYearOpt)
      val topRecorder = new A1Searcher(lOption, sOption, bIndexFolder)
      val result = topRecorder.run(attrContentMap)
      Ok(views.html.a1Main(result))
    } catch {
      case e: Exception => {
        val stats = SearchStats(0L, None, e.toString)
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
      if (indexExists && reIndex) {
        Logger.info(s"$bIndexFolder exists")
        recursivelyDelete(Paths.get(bIndexFolder))
      }
      val worker = BIndexWorker(lOption, bIndexFolder)

      val stats = indexer.run(worker)
      val fieldInfo = {
        val indexInfo = new BDocInfoReader(bIndexFolder)
        indexInfo.getFieldInfo(Config.I_TITLE)
      }
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
    try {
      val isValid = attrContentMap.values.forall(_.isDefined)
      if (!isValid) {
        throw new LError(s"[$I_PUB_YEAR] and [$I_VENUE] should both be nonEmpty")
      }
      val lOption = LOption(request)
      val sOption = SOption(request)
      val searcher = new A2Searcher(lOption, sOption, a2IndexFolder)
      val res = searcher.search(attrContentMap)
      Ok(views.html.a2Main(res))
    } catch {
      case e: Exception => {
        val stats = SearchStats(0L, None, e.toString)
        val res = A2Result(stats, None, Map.empty)
        Ok(views.html.a2Main(res))
      }
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
      recursivelyDelete(Paths.get(a2IndexFolder))
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
