package models.xml

import models.core.LIndexer
import org.slf4j.LoggerFactory
import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.{Attributes, SAXException, SAXParseException}
import play.api.Logger

class PubHandler(indexer: LIndexer) extends DefaultHandler {

  val logger = LoggerFactory.getLogger(classOf[PubHandler])
  private val ARTICLE = "article"
  private val INPROCEEDINGS = "inproceedings"
  private val JOURNAL = "journal"
  private val BOOKTITLE = "booktitle"

  private var inEntry = false
  private var isTitle = false
  private var isAuthor = false
  private var isYear = false
  private var isVenue = false
  private var unknown = false

  var pub: Publication = null
  var sb: StringBuilder = new StringBuilder()

  def isInterestingEntry(name: String) = List(ARTICLE, INPROCEEDINGS).contains(name)

  def isVenueEntry(name: String) = List(JOURNAL, BOOKTITLE).contains(name)

  override def startElement(namespaceURI: String, localName: String, qName: String, attributes: Attributes): Unit = {
    unknown = false
    if (isInterestingEntry(qName)) {
      require(!inEntry)
      inEntry = true
      require(pub == null)
      pub = new Publication()
      if (qName.equalsIgnoreCase(ARTICLE)) {
        pub.kind = ARTICLE
      } else if (qName.equalsIgnoreCase(INPROCEEDINGS)) {
        pub.kind = INPROCEEDINGS
      }
      pub.paperId = attributes.getValue("key")
    } else if (inEntry) {
      if (qName.equalsIgnoreCase("author")) {
        isAuthor = true
      } else if (qName.equalsIgnoreCase("title")) {
        isTitle = true
      } else if (qName.equalsIgnoreCase("year")) {
        isYear = true
      }
      if (isVenueEntry(qName)) {
        if (qName.equalsIgnoreCase(JOURNAL)) {
          require(pub.kind.equals(ARTICLE))
          isVenue = true
        } else if (qName.equalsIgnoreCase(BOOKTITLE)) {
          require(List(ARTICLE, INPROCEEDINGS).contains(pub.kind))
          isVenue = true
        }
      } else {
        unknown = true
      }
    }
  }

  override def endElement(namespaceURI: String, localName: String, qName: String): Unit = {
    val qLowerName = qName.toLowerCase
    if (isInterestingEntry(qLowerName)) {
      if (inEntry) {
        inEntry = false
        indexer.index(pub)
        pub = null
      }
    } else {
      val value = sb.toString()
      Logger.info(s"isTitle=$isTitle, isAuthor=$isAuthor, isYear=$isYear, isVenue=$isVenue, unknown=$unknown\tvalue=$value")
      sb.setLength(0)
      if (isTitle) {
        isTitle = false
        pub.title = value
      } else if (isAuthor) {
        isAuthor = false
        pub.addAuthor(value)
      } else if (isYear) {
        isYear = false
        pub.pubYear = value
      } else if (isVenue) {
        isVenue = false
        pub.venue = value
      } else if (unknown) {
        unknown = false
      }
    }
  }

  override def characters(ch: Array[Char], start: Int, length: Int): Unit = {
    if (inEntry) {
      sb.append(new String(ch, start, length))
    }
  }

  private def Message(mode: String, e: SAXParseException) {
    Logger.info(s"$mode Line:${e.getLineNumber} URI: ${e.getSystemId} + ${e.getMessage}")
  }

  override def warning(e: SAXParseException): Unit = {
    Message("**Parsing Warning**\n", e)
    throw new SAXException("Warning encountered")
  }

  override def error(e: SAXParseException): Unit = {
    Message("**Parsing Error**\n", e)
    throw new SAXException("Error encountered")
  }

  override def fatalError(e: SAXParseException): Unit = {
    Message("**Parsing Fatal Error**\n", e)
    throw new SAXException("Fatal Error encountered")
  }
}
