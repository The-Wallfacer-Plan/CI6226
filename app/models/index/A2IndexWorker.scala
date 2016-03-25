package models.index

import java.nio.file.Paths

import models.common.{Config, LAnalyzer, LOption}
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
      Logger.info(s"a2 indexing, Folder: $indexFolderString")
      val indexFolder = Paths.get(indexFolderString)
      FSDirectory.open(indexFolder)
    }
    val writer = new IndexWriter(dir, iwc)
    new A2IndexWorker(writer)
  }
}

class A2IndexWorker(indexWriter: IndexWriter) extends LIndexWorker(indexWriter) {

  import Config._
  import LIndexWorker._

  val docMap = mutable.Map.empty[String, Document]

  override def index(pub: Publication): Unit = {
    val docSign = pub.pubYear + "+" + pub.venue
    if (!docMap.contains(docSign)) {
      val doc = new Document
      if (pub.venue == "TKDE" && pub.pubYear == "2012") {
        Logger.info(s"pub=$pub, doc=$doc")
      }
      if (pub.pubYear != null) {
        addDocText(I_PUB_YEAR, pub.pubYear, doc)
      } else {
        Logger.info(s"year=null for $pub")
      }
      if (pub.venue != null) {
        addDocText(I_VENUE, pub.venue, doc)
      } else {
        Logger.warn(s"venue=null for $pub")
      }
      addDocText(I_TITLE, pub.title, doc)
      docMap += docSign -> doc
    } else {
      //    Logger.info(s"add ${pub.paperId} into $docSign")
      val doc = docMap(docSign)
      addDocText(I_TITLE, pub.title, doc)
    }

  }

  override def writeDone(): Unit = {
    for (entry <- docMap) {
      indexWriter.addDocument(entry._2)
    }
    indexWriter.close()
    Logger.info("a2 index done")
  }
}
