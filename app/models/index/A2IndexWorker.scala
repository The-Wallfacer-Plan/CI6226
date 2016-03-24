package models.index

import java.nio.file.Paths

import models.common.{LAnalyzer, LOption}
import models.xml.Publication
import org.apache.lucene.document.Document
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.FSDirectory
import play.api.Logger

import scala.collection.mutable

object A2IndexWorker {
  def apply(lOption: LOption, indexFolderString: String): A2IndexWorker = {
    val analyzer = new LAnalyzer(lOption, None)
    val iwc = new IndexWriterConfig(analyzer)
    iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE)

    val dir = {
      Logger.info(s"a2 index Folder: $indexFolderString")
      val indexFolder = Paths.get(indexFolderString)
      FSDirectory.open(indexFolder)
    }
    val writer = new IndexWriter(dir, iwc)
    new A2IndexWorker(writer)
  }
}

class A2IndexWorker(indexWriter: IndexWriter) extends LIndexWorker(indexWriter) {

  import LIndexWorker._

  val docMap = mutable.Map.empty[String, Document]

  override def index(pub: Publication): Unit = {
    val docSign = pub.pubYear + "+" + pub.venue
    if (!docMap.contains(docSign)) {
      docMap += docSign -> new Document
    }
    val doc = docMap(docSign)
//    Logger.info(s"add ${pub.paperId} into $docSign")
    addDocText("a2", pub.title, doc)

  }

  override def writeDone(): Unit = {
    for (entry <- docMap) {
      indexWriter.addDocument(entry._2)
    }
    indexWriter.close()
    Logger.info("a2 index done")
  }
}
