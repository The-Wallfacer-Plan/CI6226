package models.core

import java.nio.file.{Files, Paths}

import models.utility.Config
import models.{LSearchPub, LSearchResult, LSearchStats}
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search._
import org.apache.lucene.search.similarities.BM25Similarity
import org.apache.lucene.store.FSDirectory
import play.api.Logger

import scala.collection.JavaConversions._

class LSearcher(lOption: LOption, indexFolderString: String, topN: Int) {
  val analyzer = new LAnalyzer(lOption, null)
  val reader = {
    val indexFolder = Paths.get(indexFolderString)
    require(Files.exists(indexFolder))
    val directory = FSDirectory.open(indexFolder)
    DirectoryReader.open(directory)
  }

  val searcher = {
    val s = new IndexSearcher(reader)
    val similarity = new BM25Similarity()
    s.setSimilarity(similarity)
    s
  }

  private def validate(queryString: String): Unit = {
  }

  def searchImp(queryString: String): LSearchResult = {
    validate(queryString)
    val startTime = System.currentTimeMillis()
    val query = {
      val parser = new QueryParser(Config.COMBINED_FIELD, analyzer)
      parser.setAllowLeadingWildcard(true)
      parser.parse(queryString)
    }
    Logger.info(s"string=$queryString, query=$query")
    val topDocs = searcher.search(query, topN)

    val duration = System.currentTimeMillis() - startTime
    val foundPubs = getSearchPub(topDocs)
    reader.close()
    val searchStats = LSearchStats(duration, query.toString())
    new LSearchResult(searchStats, Some(lOption), foundPubs)
  }

  def getSearchPub(topDocs: TopDocs): List[LSearchPub] = {
    topDocs.scoreDocs.toList map {
      hit => {
        val docID = hit.doc
        val score = hit.score
        val hitDoc = searcher.doc(docID)
        //        Logger.info(s"${hitDoc.getField(field)}")
        val fieldValues = for {
          field <- hitDoc.getFields
          if field.name() != Config.COMBINED_FIELD
        } yield {
          field.name() -> field.stringValue()
        }
        val fieldDocMap = Map(fieldValues: _*)
        new LSearchPub(docID, score, fieldDocMap)
      }
    }
  }
}
