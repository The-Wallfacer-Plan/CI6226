package models.index

import java.nio.file.{Files, Paths}

import org.apache.lucene.index.{DirectoryReader, IndexReader}
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory
import play.api.libs.json.{JsNumber, JsObject}

class LDocInfo(indexFolderString: String) {
  val reader: IndexReader = {
    val indexFolder = Paths.get(indexFolderString)
    require(Files.exists(indexFolder))
    val directory = FSDirectory.open(indexFolder)
    DirectoryReader.open(directory)
  }

  def getFieldInfo(field: String): JsObject = {
    val searcher = new IndexSearcher(reader)
    val stats = searcher.collectionStatistics(field)
    JsObject(Seq(
      "doc Count" -> JsNumber(stats.docCount()),
      "max doc" -> JsNumber(stats.maxDoc()),
      "posting number" -> JsNumber(stats.sumDocFreq()),
      "tokens number" -> JsNumber(stats.sumTotalTermFreq())
    ))
  }


}
