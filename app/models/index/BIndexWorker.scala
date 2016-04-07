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

  private def combinedAddField(fieldName: String, fieldValue: String, document: Document, tokenized: Boolean): Unit = {
    if (tokenized) {
      addTokenizedField(fieldName, fieldValue, document)
      addTokenizedField(I_ALL, fieldValue, document)
    } else {
      addStringField(fieldName, fieldValue, document)
      addStringField(I_ALL, fieldValue, document)
    }
  }

  override def index(pub: Publication): Unit = {
    pub.validate()
    Logger.debug(s"=> $pub")
    val document = new Document()

    combinedAddField(I_PAPER_ID, pub.paperId.replace('/', ' '), document, tokenized = true)
    combinedAddField(I_TITLE, pub.title, document, tokenized = true)
    combinedAddField(I_VENUE, pub.venue, document, tokenized = true)
    combinedAddField(I_PUB_YEAR, pub.pubYear, document, tokenized = false)
    combinedAddField(I_KIND, pub.kind, document, tokenized = false)
    pub.authors.foreach(author => combinedAddField(I_AUTHORS, author, document, tokenized = true))

    if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
      writer.addDocument(document)
    }
  }
}
