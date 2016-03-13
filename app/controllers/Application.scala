package controllers

import models.core.{LIndexDriver, LIndexer, LOption, LSearcher}
import models.utility.Config
import models.{LSearchResult, Stats}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._

import scala.sys.process.Process

class Application extends Controller {

  implicit val LCharset = Codec.javaSupported("utf-8")

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
        val searcher = new LSearcher(searchOption, indexFolder)
        val res = searcher.searchImp(queryString)
        Ok(views.html.home(res))
      }
      case None => {
        val result = new LSearchResult("OK", Stats(0), List.empty)
        Ok(views.html.home(result))
      }
    }
  }
  }

  def indexDoc = Action(parse.json) { request => {
    val body = request.body
    val driver = new LIndexDriver(inputFile)
    val indexer = {
      val indexOption = {
        val stemming = (body \ "stem").as[Boolean]
        val ignoreCase = (body \ "ignore").as[Boolean]
        val swDict = (body \ "swDict").as[String]
        new LOption(stemming, ignoreCase, swDict)
      }
      LIndexer(indexOption, indexFolder)
    }
    val start = System.currentTimeMillis()
    driver.run(indexer)
    val duration = System.currentTimeMillis() - start
    Logger.info(s"index took ${duration}ms")
    val size = Process(s"du -sk $indexFolder").!!.split("\\s+")(0)
    val res = JsObject(Seq(
      "status" -> JsString("OK"),
      "time" -> JsString(duration.toString + "ms"),
      "size" -> JsString(size.toString + "K")
    ))
    Ok(res)
  }
  }

  ///////////////////////////////////////////////////////

  def testGet = Action {
    implicit request => {
      request.getQueryString("content") match {
        case Some(queryString) => {
          Logger.info(s"[$queryString]")
          Ok("response=" + queryString).as(HTML)
        }
        case None => BadRequest("no result")
      }
    }
  }

  def testPost = Action(BodyParsers.parse.json) {
    request => {
      Ok("testPost")
    }
  }

}
