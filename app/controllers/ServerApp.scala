package controllers

import com.google.common.collect.Lists
import ir.core.SearchWrapper
import org.slf4j.LoggerFactory
import play.api.libs.json._
import play.api.mvc._

import scala.collection.JavaConversions._

object ServerApp extends Controller {

  val logger = LoggerFactory.getLogger(getClass)

  def rawSearchResult = Action {
    val wrapper = new SearchWrapper()
    val fields = Lists.newArrayList("title", "venue")
    val matchedFieldsMap = wrapper.search(fields, "Integer")
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

  def testGet = Action { implicit request =>
    val entries = request.queryString
    val res = entries.map(
      entry => {
        s"${entry._1} => ${entry._2.get(0)}"
      }
    ).mkString("\n")
    Ok(res)
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
