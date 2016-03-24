package models.index

import java.nio.file.Paths

import models.common.{Config, LAnalyzer, LOption}
import models.xml.Publication
import org.apache.lucene.document.Document
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.FSDirectory
import play.api.Logger


object BIndexWorker {
  def apply(option: LOption, indexFolderString: String): BIndexWorker = {
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
      FSDirectory.open(indexFolder)
    }

    val writer = new IndexWriter(dir, iwc)
    new BIndexWorker(writer)
  }
}

class BIndexWorker(writer: IndexWriter) extends LIndexWorker(writer) {

  import Config._
  import LIndexWorker._

  def writeDone() = {
    writer.close()
    Logger.info("index done")
  }

  override def index(pub: Publication): Unit = {
    if (pub.paperId.startsWith(DBLPNOTE)) {
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
