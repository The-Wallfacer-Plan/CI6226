package models.index

import java.io.{File, FileWriter}
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
    //    if (pub.pubYear == "2015" && pub.venue == "IJCC") {
    //      println(s"pub:\t$pub")
    //    }
    if (!docMap.contains(docSign)) {
      val doc = new Document
      addDocText(I_PUB_YEAR, pub.pubYear, doc)
      addDocText(I_VENUE, pub.venue, doc)
      addDocText(I_TITLE, pub.title, doc)
      docMap += docSign -> doc
    } else {
      val doc = docMap(docSign)
      addDocText(I_TITLE, pub.title, doc)
    }
  }

  override def writeDone(): Unit = {
    val outFileName = rootDir + File.separator + "outfile"
    val fw = new FileWriter(outFileName, false)
    for (entry <- docMap) {
      //      if (entry._1.contains("IJCC")) {
      //        println(entry._1)
      //      }
      indexWriter.addDocument(entry._2)
      fw.write(entry._1 + '\n')
    }
    fw.close()
    indexWriter.close()
    Logger.info("a2 index done")
  }
}
