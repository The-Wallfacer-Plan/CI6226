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
      Logger.info(s"b indexing, folder: $indexFolderString")
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

  private def combinedAddField(fieldName: String, fieldValue: String, document: Document): Unit = {
    addField(fieldName, fieldValue, document)
    addField(I_ALL, fieldValue, document)
  }

  override def index(pub: Publication): Unit = {
    pub.validate()
    Logger.debug(s"=> $pub")
    val document = new Document()

    combinedAddField(I_PAPER_ID, pub.paperId, document)
    combinedAddField(I_TITLE, pub.title, document)
    combinedAddField(I_KIND, pub.kind, document)
    combinedAddField(I_VENUE, pub.venue, document)
    combinedAddField(I_PUB_YEAR, pub.pubYear, document)
    pub.authors.foreach(author => combinedAddField(I_AUTHORS, author, document))

    if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
      writer.addDocument(document)
    }
  }
}
