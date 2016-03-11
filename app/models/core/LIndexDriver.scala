package models.core

import java.io.{File, IOException}
import javax.xml.parsers.{ParserConfigurationException, SAXParserFactory}

import models.utility.Config
import models.xml.PubHandler
import org.xml.sax.SAXException
import play.api.Logger

class LIndexDriver(source: String) {

  def run(indexer: LIndexer) {
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
      parser.parse(inputFile, handler)
      indexer.writeBack();
    } catch {
      case e: IOException => Logger.error(s"error reading URI: $e")
      case e: SAXException => Logger.error(s"error in parsing: $e")
      case e: ParserConfigurationException => Logger.error(s"error in xml configuration: $e")
    }
  }
}
