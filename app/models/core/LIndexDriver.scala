package models.core

import java.io.{File, IOException}
import javax.xml.parsers.{ParserConfigurationException, SAXParserFactory}

import models.Stats
import models.utility.Config
import models.xml.PubHandler
import org.xml.sax.SAXException
import play.api.Logger

class LIndexDriver(source: String) {

  def run(indexer: LIndexer): Stats = {
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
      new Stats(duration)
    } catch {
      case e: IOException => {
        Logger.error(s"error reading URI: $e")
        Stats(0L)
      }
      case e: SAXException => {
        Logger.error(s"error in parsing: $e")
        Stats(0L)
      }
      case e: ParserConfigurationException => {
        Logger.error(s"error in xml configuration: $e")
        Stats(0L)
      }
    }
  }
}
