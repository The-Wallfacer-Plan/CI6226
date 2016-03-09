package controllers

import models.core.{LIndexDriver, LIndexOption, LIndexer, SearchWrapper}
import models.utility.Config
import org.slf4j.LoggerFactory
import play.api.libs.json._
import play.api.mvc._

import scala.collection.JavaConversions._

object ServerApp extends Controller {

  val badRequestMsg = "invalid request body, should be json"

  val logger = LoggerFactory.getLogger(getClass)

  def searchDoc = Action { request => {
    //    TODO should add option for "exact match" phrase
    request.body.asJson match {
      case None => BadRequest(badRequestMsg)
      case Some(body) => {
        logger.info(s"$body")
        val fields = (body \ "fields").as[JsArray].value.map(jsValue => {
          jsValue.as[String]
        })
        val content = (body \ "content").as[String]

        logger.info(s"content=$content, fields=${fields.mkString("(", ", ", ")")}")

        val wrapper = new SearchWrapper()
        val FFF = Config.defaultFields
        val matchedFieldsMap = wrapper.search(fields, content)
        val searcher = wrapper.getSearcher

        val resList = matchedFieldsMap.flatMap(
          entry => {
            entry._2.map(
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
        Ok(Json.toJson(resList))
      }
    }
  }
  }

  def indexDoc = Action { request => {
    request.body.asJson match {
      case None => BadRequest(badRequestMsg)
      case Some(body) => {
        val driver = new LIndexDriver(Config.xmlFile)
        val stemming = (body \ "stem").as[Boolean]
        val ignoreCase = (body \ "ignore").as[Boolean]
        val swDict = (body \ "swDict").as[String]
        val indexOption = new LIndexOption(stemming, ignoreCase, swDict)
        val indexer = new LIndexer(indexOption)
        val start = System.currentTimeMillis()
        driver.run(indexer)
        val duration = System.currentTimeMillis() - start
        val msg = s"indexing cost $duration ms"
        Ok(msg)
      }
    }
  }
  }

  def testGet = Action { implicit request =>
    Ok("testGet")
  }

  def testPost = Action(BodyParsers.parse.json) { request => {
    Ok("testPost")
  }
  }

}
