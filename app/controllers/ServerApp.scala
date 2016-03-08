package controllers

import java.util

import ir.core.SearchWrapper
import ir.utility.Config
import org.slf4j.LoggerFactory
import play.api.libs.json._
import play.api.mvc._

import scala.collection.JavaConversions._

object ServerApp extends Controller {

  val logger = LoggerFactory.getLogger(getClass)

  def rawSearchResult = Action { request => {
    val stemming = request.getQueryString("stem").getOrElse(false)
    val ignoreCase = request.getQueryString("ignore").getOrElse(true)
    val swDict = request.getQueryString("swDict").getOrElse("Lucene")
    val fields = request.getQueryString("fields").getOrElse(Config.defaultFields).asInstanceOf[util.ArrayList[String]];
    val content = request.getQueryString("content").get

    logger.info(s"content=$content stem=$stemming, ignoreCase=$ignoreCase, stopWords=$swDict fields=${fields.toString}")

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

  def testGet = Action { implicit request =>
    val stemming = request.getQueryString("stem").getOrElse(false)
    val ignoreCase = request.getQueryString("ignore").getOrElse(true)
    val swDict = request.getQueryString("swDict").getOrElse("Lucene")
    val fields = request.getQueryString("fields").getOrElse(Config.defaultFields)
    val resString = s"stem=$stemming, ignoreCase=$ignoreCase, stopWords=$swDict fields=${fields.toString}"
    Ok("got " + resString)
  }

  def testPost = Action(BodyParsers.parse.json) { request => {
    val entries = request.queryString
    val res = entries.map(
      entry => {
        s"entry: ${entry._1} => ${entry._2.get(0)}"
      }
    ).mkString("\n")
    Ok(res)
  }
  }

}
