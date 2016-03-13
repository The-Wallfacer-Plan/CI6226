package controllers

import models.core.{LIndexDriver, LIndexOption, LIndexer, LSearcher}
import models.utility.Config
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

  def home = Action {
    Ok(views.html.home("testIT"))
  }

  def searchDoc = Action(parse.json) { request => {
    val body = request.body
    Logger.info(s"$body")
    val fields = (body \ "fields").as[JsArray].value.map(jsValue => jsValue.as[String]).toList
    val content = (body \ "content").as[String]

    Logger.info(s"content=$content, fields=${fields.mkString("(", ", ", ")")}")

    val wrapper = new LSearcher(indexFolder)
    val returnContent = wrapper.search(fields, content)
    val res = JsObject(Seq(
      "status" -> JsString("OK"),
      "result" -> returnContent
    ))
    Logger.info(s"$res")
    Ok(res)
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
        new LIndexOption(stemming, ignoreCase, swDict)
      }
      LIndexer(indexOption, indexFolder)
    }
    val start = System.currentTimeMillis()
    driver.run(indexer)
    val duration = System.currentTimeMillis() - start
    Logger.info(s"index took ${duration}ms")
    //    val sizeString = FileUtils.byteCountToDisplaySize(FileUtils.sizeOfDirectory(new File(indexFolder)))
    val size = Process(s"du -sk $indexFolder").!!.split("\\s+")(0)
    val res = JsObject(Seq(
      "status" -> JsString("OK"),
      "time" -> JsString(duration.toString + "ms"),
      "size" -> JsString(size.toString + "K")
    ))
    Ok(res)
  }
  }

  def testGet = Action {
    implicit request =>
      Ok("<h1>--testGet</h1>").as(HTML)
  }

  def testPost = Action(BodyParsers.parse.json) {
    request => {
      Ok("testPost")
    }
  }

}
