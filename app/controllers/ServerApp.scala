package controllers

import com.google.common.collect.Lists
import ir.core.SearchWrapper
import models.Book._
import org.slf4j.LoggerFactory
import play.api.libs.json._
import play.api.mvc._

import scala.collection.JavaConversions._

object ServerApp extends Controller {

  val logger = LoggerFactory.getLogger(getClass)

  def listBooks = Action {
    Ok(Json.toJson(books))
  }

  def listResult = Action {
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

  def saveBook = Action(BodyParsers.parse.json) { request =>
    val b = request.body.validate[Book]
    b.fold(
      errors => {
        BadRequest(Json.obj("status" -> "OK", "message" -> JsError.toJson(errors)))
      },
      book => {
        addBook(book)
        Ok(Json.obj("status" -> "OK"))
      }
    )
  }
}
