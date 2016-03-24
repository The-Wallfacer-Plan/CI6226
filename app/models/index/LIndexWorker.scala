package models.index

import models.xml.Publication
import org.apache.lucene.document.{Document, Field, FieldType}
import org.apache.lucene.index.{IndexOptions, IndexWriter}
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

  def addDocText(key: String, value: String, document: Document) = {
    val field = new Field(key, value, ft1)
    document.add(field)
  }

}


abstract class LIndexWorker(writer: IndexWriter) {

  def writeDone(): Unit

  def index(pub: Publication): Unit

}

