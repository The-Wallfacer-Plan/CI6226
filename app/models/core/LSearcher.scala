package models.core

import java.nio.file.{Files, Paths}

import models.utility.Config
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.{BooleanClause, BooleanQuery, IndexSearcher}
import org.apache.lucene.store.FSDirectory
import play.api.libs.json.{JsValue, Json}

import scala.collection.JavaConversions._

class LSearcher(item: String) {
  val analyzer = new LAnalyzer()
  val reader = {
    val itemFolder = Paths.get(item)
    require(Files.exists(itemFolder))
    val directory = FSDirectory.open(itemFolder)
    DirectoryReader.open(directory)
  }
  val searcher = new IndexSearcher(reader)

  def search(fields: List[String], queryString: String): JsValue = {
    val raw = fields.map(field => {
      field -> {
        val parser = new QueryParser(field, analyzer)
        parser.setAllowLeadingWildcard(true)
        val query = parser.parse(queryString)
        val booleanQuery = new BooleanQuery.Builder().add(query, BooleanClause.Occur.MUST).build()
        val topDocs = searcher.search(booleanQuery, Config.topN)
        topDocs.scoreDocs
      }
    }).toMap

    val resList = raw.flatMap(
      entry => {
        val hits = entry._2
        hits.map(
          hit => {
            val hitDoc = searcher.doc(hit.doc)
            val hitDocMap = hitDoc.getFields().map(
              field => {
                field.name() -> field.stringValue()
              }
            ).toMap + ("No." -> hit.doc.toString())
            Json.toJson(hitDocMap)
          }
        ).toList
      }
    )
    reader.close()
    Json.toJson(resList)
  }
}
