package models.search

import java.nio.file.{Files, Paths}

import models.common.{LAnalyzer, LOption}
import org.apache.lucene.index.{DirectoryReader, Term}
import org.apache.lucene.search.{BooleanClause, BooleanQuery, IndexSearcher, TermQuery}
import org.apache.lucene.store.FSDirectory

abstract class LSearcher(lOption: LOption, indexFolderString: String, topN: Int) {

  val analyzer = new LAnalyzer(lOption, None)
  val reader = {
    val indexFolder = Paths.get(indexFolderString)
    require(Files.exists(indexFolder))
    val directory = FSDirectory.open(indexFolder)
    DirectoryReader.open(directory)
  }

  val searcher = {
    val s = new IndexSearcher(reader)

    //    val similarity = new BM25Similarity()
    //    s.setSimilarity(similarity)
    s
  }

  protected def validate(queryString: String): Unit = {
  }

  def addTermQuery(contentMap: Map[String, Option[String]], builder: BooleanQuery.Builder): Unit = {
    for (entry <- contentMap) {
      entry._2 match {
        case Some(content) => {
          val term = new Term(entry._1, content)
          val contentQuery = new TermQuery(term)
          builder.add(contentQuery, BooleanClause.Occur.MUST)
        }
        case None =>
      }
    }
  }
}
