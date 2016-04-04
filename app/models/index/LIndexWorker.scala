package models.index

import models.xml.Publication
import org.apache.lucene.document.{Document, Field, FieldType}
import org.apache.lucene.index.{IndexOptions, IndexWriter}


object LIndexWorker {

  val textFT = {
    val ft = new FieldType()
    ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS)
    ft.setTokenized(true)
    ft.setStored(true)
    ft.setStoreTermVectors(false)
    ft.freeze()
    ft
  }

  val stringFT = {
    val ft = new FieldType()
    ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS)
    ft.setTokenized(false)
    ft.setStored(true)
    ft.setStoreTermVectors(false)
    ft.freeze()
    ft
  }

  def addTokenizedField(key: String, value: String, document: Document) = {
    val field = new Field(key, value, textFT)
    document.add(field)
  }

  def addStringField(key: String, value: String, document: Document) = {
    val field = new Field(key, value, stringFT)
    document.add(field)
  }

}


abstract class LIndexWorker(writer: IndexWriter) {

  def writeDone(): Unit

  def index(pub: Publication): Unit

}

