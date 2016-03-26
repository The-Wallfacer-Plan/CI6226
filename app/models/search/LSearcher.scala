package models.search

import java.nio.file.{Files, Paths}

import models.common.{LAnalyzer, LOption}
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.similarities.BM25Similarity
import org.apache.lucene.store.FSDirectory
import play.api.Logger
import play.api.mvc.{AnyContent, Request}

case class SOption(similarityKind: String, topN: Int)

object SOption {
  def apply(request: Request[AnyContent]): SOption = {
    val topN = request.getQueryString("topN").get.toInt
    val similarity = request.getQueryString("similarity").get
    Logger.info(s"$similarity")
    new SOption(similarity, topN)
  }
}

abstract class LSearcher(lOption: LOption, indexFolderString: String) {

  val analyzer = new LAnalyzer(lOption, None)
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

  protected def validate(queryString: String): Unit = {
  }

  def getMustQuery(contentMap: Map[String, Option[String]]): String = {
    val fields = for (entry <- contentMap; s <- entry._2) yield "+" + entry._1 + ":\"" + s + "\""
    fields.mkString(" ")
  }
}
