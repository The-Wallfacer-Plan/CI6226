package models.index

import models.xml.Publication
import org.apache.lucene.index.IndexWriter

class A2IndexWorker(indexWriter: IndexWriter) extends LIndexWorker(indexWriter) {
  override def index(pub: Publication): Unit = {
    
  }
}
