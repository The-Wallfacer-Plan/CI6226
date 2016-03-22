package models.index

import java.nio.file.{Files, Paths}

import org.apache.lucene.index.{DirectoryReader, IndexReader}
import org.apache.lucene.search.{CollectionStatistics, IndexSearcher}
import org.apache.lucene.store.FSDirectory
import play.api.libs.json.{JsNumber, JsObject, JsValue}

class LDocInfoReader(indexFolderString: String) {
  val reader: IndexReader = {
    val indexFolder = Paths.get(indexFolderString)
    require(Files.exists(indexFolder))
    val directory = FSDirectory.open(indexFolder)
    DirectoryReader.open(directory)
  }

  def getFieldInfo(field: String): CollectionStatistics = {
    val searcher = new IndexSearcher(reader)
    searcher.collectionStatistics(field)
  }


}

object LDocInfoReader {
  def toJson(info: CollectionStatistics): JsValue = {
    JsObject(Seq(
      "doc Count" -> JsNumber(info.docCount()),
      "max doc" -> JsNumber(info.maxDoc()),
      "posting number" -> JsNumber(info.sumDocFreq()),
      "tokens number" -> JsNumber(info.sumTotalTermFreq())
    ))
  }
}
