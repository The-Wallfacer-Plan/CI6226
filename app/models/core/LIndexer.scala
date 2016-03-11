package models.core

import java.nio.file.{Files, Paths}
import java.util

import models.utility.{Config, Helper}
import models.xml.Publication
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper
import org.apache.lucene.document.{Document, Field, TextField}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.FSDirectory
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

class LIndexer(writer: IndexWriter) {

  def writeBack() = writer.close()

  def addDocText(key: String, value: String, document: Document) = {
    val field = new TextField(key, value, Field.Store.YES)
    document.add(field)
  }

  def index(publication: Publication): Unit = {
    if (publication.getPaperId().startsWith(Config.DBLPNOTE)) {
      LIndexer.logger.warn("dblpnote entry, ignoring")
      return
    }
    publication.validate()
    val document = new Document()
    //        is the form of "xxx/xxx/xxx", use TextField
    addDocText("paperId", publication.getPaperId(), document)
    //        TextField
    addDocText("title", publication.getTitle(), document)
    //        StringField
    addDocText("kind", String.valueOf(publication.getKind()), document)
    //        ???
    addDocText("venue", publication.getVenue(), document)
    //        StringField
    addDocText("pubYear", publication.getPubYear(), document)
    //        TextField (how to join/split author list ???)
    val authorString = publication.getAuthors().mkString(Config.splitString)
    addDocText("authors", authorString, document)
  }

}

object LIndexer {
  val logger = LoggerFactory.getLogger(getClass)

  def apply(option: LIndexOption, indexFolderString: String): LIndexer = {
    val analyzerWrapper = {
      val analyzer = new LAnalyzer(option, null)
      val listAnalyzer = new LAnalyzer(option, Config.splitString)
      val analyzerMap = new util.HashMap[String, Analyzer]()
      analyzerMap.put(Config.I_AUTHORS, listAnalyzer)
      new PerFieldAnalyzerWrapper(analyzer, analyzerMap)
    }
    val iwc = new IndexWriterConfig(analyzerWrapper)
    iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE)

    val dir = {
      logger.info(s"indexing folder: $indexFolderString")
      val indexFolder = Paths.get(indexFolderString)
      if (Files.exists(indexFolder)) {
        logger.info("indexing folder already exists, delete")
        Helper.deleteFiles(indexFolder)
      }
      FSDirectory.open(indexFolder)
    }

    val writer = new IndexWriter(dir, iwc)
    new LIndexer(writer)
  }
}
