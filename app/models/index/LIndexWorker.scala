package models.index

import java.nio.file.{Files, Paths}

import models.common.{Config, LAnalyzer, LOption}
import models.xml.Publication
import org.apache.lucene.document.{Document, Field, FieldType}
import org.apache.lucene.index.{IndexOptions, IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.FSDirectory
import play.api.Logger

import scala.sys.process.Process


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

  def apply(option: LOption, indexFolderString: String): LIndexWorker = {
    val analyzerWrapper = {
      //      val analyzer = new LAnalyzer(option, None)
      //      val listAnalyzer = new LAnalyzer(option, Config.splitString)
      //      val analyzerMap = Map(Config.I_AUTHORS -> listAnalyzer)
      //      new PerFieldAnalyzerWrapper(analyzer, analyzerMap)
      new LAnalyzer(option, None)
    }
    val iwc = new IndexWriterConfig(analyzerWrapper)
    iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE)

    val dir = {
      Logger.info(s"indexing folder: $indexFolderString")
      val indexFolder = Paths.get(indexFolderString)
      if (Files.exists(indexFolder)) {
        Logger.info("indexing folder already exists, delete")
        Process(s"rm -rf $indexFolderString").!
      }
      FSDirectory.open(indexFolder)
    }

    val writer = new IndexWriter(dir, iwc)
    new LIndexWorker(writer)
  }
}

class LIndexWorker(val writer: IndexWriter) {

  import Config._
  import LIndexWorker._

  def writeBack() = {
    writer.close()
    Logger.info("index done")
  }

  private def addDocText(key: String, value: String, document: Document) = {
    val field = new Field(key, value, ft1)
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
    addDocText(I_PAPER_ID, pub.paperId, document)
    //        TextField
    addDocText(I_TITLE, pub.title, document)
    //        StringField
    addDocText(I_KIND, pub.kind, document)
    //        ???
    addDocText(I_VENUE, pub.venue, document)
    //        StringField
    addDocText(I_PUB_YEAR, pub.pubYear, document)
    //        TextField (how to join/split author list ???)
    val authorString = pub.authors.mkString(Config.splitString)
    addDocText(I_AUTHORS, authorString, document)
    //    pub.authors.foreach(author => addDocText("authors", author, document))

    // for free text search
    addDocText("ALL", pub.combinedString(), document)

    if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
      writer.addDocument(document)
    }
  }

}

