package models.core

import java.nio.file.{Files, Paths}

import models.utility.Config
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.{BooleanClause, BooleanQuery, IndexSearcher}
import org.apache.lucene.store.FSDirectory
import play.api.libs.json.Json

import scala.collection.JavaConversions._

class LSearcher(lOption: LOption, indexFolderString: String) {
  val analyzer = new LAnalyzer(lOption, null)
  val reader = {
    val indexFolder = Paths.get(indexFolderString)
    require(Files.exists(indexFolder))
    val directory = FSDirectory.open(indexFolder)
    DirectoryReader.open(directory)
  }
  val searcher = new IndexSearcher(reader)

  def parse(queryString: String): Unit = {
  }

  def search(queryString: String) = {
    //    TODO
    //    val fields = Config.defaultFields
    val fields = List("ALL")
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
    List(queryString, "good", "bad")
  }
}
