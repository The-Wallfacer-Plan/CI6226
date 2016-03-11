package models.core

import java.nio.file.{Files, Paths}

import models.utility.Config
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.{BooleanClause, BooleanQuery, IndexSearcher, ScoreDoc}
import org.apache.lucene.store.FSDirectory

class LSearcher(item: String) {
  val analyzer = new LAnalyzer()
  val reader = {
    val itemFolder = Paths.get(item)
    require(Files.exists(itemFolder))
    val directory = FSDirectory.open(itemFolder)
    DirectoryReader.open(directory)
  }
  val searcher = new IndexSearcher(reader)

  def search(fields: List[String], queryString: String): Map[String, Array[ScoreDoc]] = {
    fields.map(field => {
      field -> {
        val parser = new QueryParser(field, analyzer)
        parser.setAllowLeadingWildcard(true)
        val query = parser.parse(queryString)
        val booleanQuery = new BooleanQuery.Builder().add(query, BooleanClause.Occur.MUST).build()
        val topDocs = searcher.search(booleanQuery, Config.topN)
        topDocs.scoreDocs
      }
    }).toMap
  }

  def close() = reader.close()
}
