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
import play.api.Logger

class LIndexer(writer: IndexWriter) {

  def writeBack() = {
    Logger.info("write index done")
    writer.close()
  }

  def addDocText(key: String, value: String, document: Document) = {
    val field = new TextField(key, value, Field.Store.YES)
    document.add(field)
  }

  def index(publication: Publication): Unit = {
    if (publication.paperId.startsWith(Config.DBLPNOTE)) {
      Logger.warn("dblpnote entry, ignoring")
      return
    }
    publication.validate()
    val document = new Document()
    try {
      //        is the form of "xxx/xxx/xxx", use TextField
      addDocText("paperId", publication.paperId, document)
      //        TextField
      addDocText("title", publication.title, document)
      //        StringField
      addDocText("kind", publication.kind, document)
      //        ???
      addDocText("venue", publication.venue, document)
      //        StringField
      addDocText("pubYear", publication.pubYear, document)
      //        TextField (how to join/split author list ???)
      val authorString = publication.authors.mkString(Config.splitString)
      addDocText("authors", authorString, document)
    } catch {
      case e: java.io.IOException => {
        e.printStackTrace()
      }
    }
  }
}

object LIndexer {

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
      Logger.info(s"indexing folder: $indexFolderString")
      val indexFolder = Paths.get(indexFolderString)
      if (Files.exists(indexFolder)) {
        Logger.info("indexing folder already exists, delete")
        Helper.deleteFiles(indexFolder)
      }
      FSDirectory.open(indexFolder)
    }

    val writer = new IndexWriter(dir, iwc)
    new LIndexer(writer)
  }
}
