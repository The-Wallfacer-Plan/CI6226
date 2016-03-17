package models.core

import java.nio.file.{Files, Paths}

import org.apache.lucene.index.{DirectoryReader, IndexReader}
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory
import play.api.Logger

class LStatistics(indexFolderString: String) {
  val reader: IndexReader = {
    val indexFolder = Paths.get(indexFolderString)
    require(Files.exists(indexFolder))
    val directory = FSDirectory.open(indexFolder)
    DirectoryReader.open(directory)
  }

  def analyze(field: String): Unit = {
    val searcher = new IndexSearcher(reader)
    val collectionStatistics = searcher.collectionStatistics(field)
    collectionStatistics.field()
    Logger.info(s"doc count: ${collectionStatistics.docCount()}")
    Logger.info(s"postings for the field: ${collectionStatistics.sumDocFreq()}")
    Logger.info(s"total tokens for the field: ${collectionStatistics.sumTotalTermFreq()}")
  }


}
