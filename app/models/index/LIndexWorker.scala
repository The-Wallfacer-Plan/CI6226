package models.index

import java.nio.file.Paths

import models.common.{Config, LAnalyzer, LOption}
import models.xml.Publication
import org.apache.lucene.document.{Document, Field, FieldType}
import org.apache.lucene.index.{IndexOptions, IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.FSDirectory
import play.api.Logger


object LIndexWorker {

  val ft1 = {
    val ft = new FieldType()
    ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS)
    ft.setTokenized(true)
    ft.setStored(true)
    ft.setStoreTermVectors(true)
    ft.freeze()
    ft
  }

  val ft2 = {
    val ft = new FieldType()
    ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS)
    ft.setTokenized(true)
    ft.setStored(true)
    ft.setStoreTermVectors(false)
    ft.freeze()
    ft
  }
}


abstract class LIndexWorker(writer: IndexWriter) {

  def writeBack() = {
    writer.close()
    Logger.info("index done")
  }

  def index(pub: Publication): Unit;

  }

