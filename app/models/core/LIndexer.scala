package models.core

import java.nio.file.{Files, Paths}

import models.utility.Config
import models.xml.Publication
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper
import org.apache.lucene.document.{Document, Field, TextField}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.FSDirectory
import play.api.Logger

import scala.collection.JavaConversions._
import scala.sys.process.Process

class LIndexer(writer: IndexWriter) {

  def writeBack() = writer.close()

  def addDocText(key: String, value: String, document: Document) = {
    val field = new TextField(key, value, Field.Store.YES)
    document.add(field)
  }

  def index(pub: Publication): Unit = {
    if (pub.paperId.startsWith(Config.DBLPNOTE)) {
      Logger.warn("dblpnote entry, ignoring")
      return
    }
    pub.validate()
    Logger.debug(s"=> $pub")
    val document = new Document()
    //        is the form of "xxx/xxx/xxx", use TextField
    addDocText("paperId", pub.paperId, document)
    //        TextField
    addDocText("title", pub.title, document)
    //        StringField
    addDocText("kind", pub.kind, document)
    //        ???
    addDocText("venue", pub.venue, document)
    //        StringField
    addDocText("pubYear", pub.pubYear, document)
    //        TextField (how to join/split author list ???)
    val authorString = pub.authors.mkString(Config.splitString)
    addDocText("authors", authorString, document)

    // for free text search
    addDocText("ALL", pub.combinedString(), document)

    if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
      writer.addDocument(document)
    }
  }

}

object LIndexer {

  def apply(option: LOption, indexFolderString: String): LIndexer = {
    val analyzerWrapper = {
      val analyzer = new LAnalyzer(option, null)
      val listAnalyzer = new LAnalyzer(option, Config.splitString)
      val analyzerMap = Map(Config.I_AUTHORS -> listAnalyzer)
      new PerFieldAnalyzerWrapper(analyzer, analyzerMap)
    }
    val iwc = new IndexWriterConfig(analyzerWrapper)
    iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE)

    val dir = {
      Logger.info(s"indexing folder: $indexFolderString")
      val indexFolder = Paths.get(indexFolderString)
      if (Files.exists(indexFolder)) {
        Logger.info("indexing folder already exists, delete")
        Process(s"rm -rf $indexFolderString").!!
      }
      FSDirectory.open(indexFolder)
    }

    val writer = new IndexWriter(dir, iwc)
    new LIndexer(writer)
  }
}
