package models.core

import java.io.File
import javax.xml.parsers.SAXParserFactory

import models.IndexStats
import models.utility.Config
import models.xml.PubHandler
import play.api.Logger

class LIndexDriver(source: String) {

  def run(indexer: LIndexer): IndexStats = {
    try {
      val parserFactory = SAXParserFactory.newInstance()
      val parser = parserFactory.newSAXParser()
      parser.getXMLReader().setFeature(Config.VALIDATION, true)
      val inputFile = new File(source)
      if (!inputFile.exists()) {
        val msg = source + " doesn't exist"
        throw new RuntimeException(msg)
      }
      val handler = new PubHandler(indexer)
      val timeStart = System.currentTimeMillis()
      parser.parse(inputFile, handler)
      val duration = System.currentTimeMillis() - timeStart
      indexer.writeBack()
      new IndexStats(duration, source)
    } catch {
      case e: Exception => {
        Logger.error(s"exception: $e")
        IndexStats(0L, source)
      }
    }
  }
}
