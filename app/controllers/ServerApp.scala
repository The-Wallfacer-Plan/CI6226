package controllers

import models.core.{LIndexDriver, LIndexOption, LIndexer, LSearcher}
import models.utility.Config
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._

import scala.collection.JavaConversions._

class ServerApp extends Controller {

  implicit val LCharset = Codec.javaSupported("utf-8")

  val inputFile = Config.xmlFile
  val indexFolder = {
    val fileName = inputFile.split(java.io.File.separator).last
    Config.indexRoot + java.io.File.separator + fileName.split('.')(0)
  }
  
  def searchDoc = Action(parse.json) { request => {
    val body = request.body
    Logger.info(s"$body")
    val fields = (body \ "fields").as[JsArray].value.map(jsValue => jsValue.as[String]).toList
    val content = (body \ "content").as[String]

    Logger.info(s"content=$content, fields=${fields.mkString("(", ", ", ")")}")

    val wrapper = new LSearcher(indexFolder)
    val matchedFieldsMap = wrapper.search(fields, content)
    val searcher = wrapper.searcher

    val resList = matchedFieldsMap.flatMap(
      entry => {
        val hits = entry._2
        hits.map(
          hit => {
            val hitDoc = searcher.doc(hit.doc)
            val hitDocMap = hitDoc.getFields().map(
              field => {
                field.name() -> field.stringValue()
              }
            ).toMap
            Json.toJson(hitDocMap)
          }
        ).toList
      }
    )
    wrapper.close()
    Logger.info(s"$resList")
    val res = Json.toJson(resList)
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
    val msg = s"indexing cost $duration ms"
    Logger.info(msg)
    Ok(msg)
  }
  }

  def testGet = Action {
    implicit request =>
      Ok("testGet")
  }

  def testPost = Action(BodyParsers.parse.json) {
    request => {
      Ok("testPost")
    }
  }

}
