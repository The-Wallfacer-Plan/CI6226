package models.index

import java.io.FileWriter
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
    val docSign = pub.pubYear + "\t" + pub.venue
    if (!docMap.contains(docSign)) {
      val doc = new Document
      addField(I_PUB_YEAR, pub.pubYear, doc)
      addField(I_VENUE, pub.venue, doc)
      addField(I_TITLE, pub.title, doc)
      docMap += docSign -> doc
    } else {
      val doc = docMap(docSign)
      addField(I_TITLE, pub.title, doc)
    }
  }

  override def writeDone(): Unit = {
    val projectResourceDir = play.Play.application().path()
    val outFileName = projectResourceDir + "/public/tmp/outfile.txt"
    val fw = new FileWriter(outFileName, false)
    for (entry <- docMap) {
      indexWriter.addDocument(entry._2)
      fw.write(entry._1 + '\n')
    }
    fw.close()
    indexWriter.close()
    Logger.info("a2 index done")
  }
}
