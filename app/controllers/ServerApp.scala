package controllers

import java.util

import ir.core.{IndexWrapper, SearchWrapper}
import ir.utility.Config
import org.slf4j.LoggerFactory
import play.api.libs.json._
import play.api.mvc._

import scala.collection.JavaConversions._

object ServerApp extends Controller {

  val logger = LoggerFactory.getLogger(getClass)

  def searchDoc = Action { request => {
    //    FIXME param should only be string
    //    TODO should add option for "exact match" phrase
    val fields = request.getQueryString("fields").getOrElse(Config.defaultFields).asInstanceOf[util.List[String]]
    val content = request.getQueryString("content").get

    logger.info(s"content=$content, fields=${fields.toString}")

    val wrapper = new SearchWrapper()
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

  def indexDoc = Action { request => {
    request.body.asJson match {
      case None => BadRequest("invalid request body, should be json")
      case Some(body) => {
        val indexer = new IndexWrapper(Config.xmlFile)
        val stemming = (body \ "stem").as[Boolean]
        val ignoreCase = (body \ "ignore").as[Boolean]
        val swDict = (body \ "swDict").as[String]
        logger.info(s"stem=$stemming, ignoreCase=$ignoreCase, swDict=$swDict")
        indexer.index(stemming, ignoreCase, swDict)
        Ok("server index done")
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
