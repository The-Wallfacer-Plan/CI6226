package models.index

import java.io.File
import javax.xml.parsers.SAXParserFactory

import models.common.Config
import models.xml.PubHandler

case class LIndexStats(time: Long, source: String)

class LIndexer(source: String) {

  def run(indexer: LIndexWorker): LIndexStats = {
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
    new LIndexStats(duration, source)
  }
}
