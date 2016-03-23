package models.search

import java.nio.file.{Files, Paths}

import models.common.{LAnalyzer, LOption}
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory

abstract class LSBase(lOption: LOption, indexFolderString: String, topN: Int) {

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
}
