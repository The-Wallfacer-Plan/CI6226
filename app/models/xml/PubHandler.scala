package models.xml

import models.index.LIndexWorker
import org.slf4j.LoggerFactory
import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.{Attributes, SAXException, SAXParseException}
import play.api.Logger

class PubHandler(indexer: LIndexWorker) extends DefaultHandler {

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
  private var isInner = false

  val ignoredUnknown = List("number", "pages", "ee", "url", "volume", "crossref", "isbn", "editor", "school", "cite", "cdrom", "month", "publisher", "note")
  val innerTags = List("i", "tt", "sub", "sup")

  var pub: Publication = null
  var sb: StringBuilder = new StringBuilder()

  def isInterestingEntry(name: String) = List(ARTICLE, INPROCEEDINGS).contains(name)

  def isVenueEntry(name: String) = List(JOURNAL, BOOKTITLE).contains(name)

  override def startElement(namespaceURI: String, localName: String, qName: String, attributes: Attributes): Unit = {
    Logger.debug(s"<<< tag=$qName")
    val qLowerName = qName.toLowerCase()
    if (isInterestingEntry(qLowerName)) {
      require(!inEntry)
      inEntry = true
      require(pub == null)
      pub = new Publication()
      pub.kind = qLowerName
      pub.paperId = attributes.getValue("key")
    } else if (inEntry) {
      if (qLowerName.equals("author")) {
        isAuthor = true
      } else if (qLowerName.equals("title")) {
        isTitle = true
      } else if (qLowerName.equals("year")) {
        isYear = true
      } else if (isVenueEntry(qLowerName)) {
        if (qLowerName.equals(JOURNAL)) {
          require(pub.kind.equals(ARTICLE))
          isVenue = true
        } else if (qLowerName.equals(BOOKTITLE)) {
          require(List(ARTICLE, INPROCEEDINGS).contains(pub.kind))
          isVenue = true
        }
      } // normal cases inside interesting entries
      else {
        if (innerTags.contains(qLowerName)) {
          isInner = true
        }
      }
    } // inEntry
  }

  override def endElement(namespaceURI: String, localName: String, qName: String): Unit = {
    Logger.debug(s">>> tag=$qName")
    val qLowerName = qName.toLowerCase
    if (inEntry) {
      if (isInterestingEntry(qLowerName)) {
        indexer.index(pub)
        inEntry = false
        pub = null
      } else {
        if (isInner) {
          isInner = false
        } // rest flatg
        else {
          val value = sb.toString()
          Logger.debug(s"isTitle=$isTitle, isAuthor=$isAuthor, isYear=$isYear, isVenue=$isVenue\tvalue=[$value]")
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
          }
        } // set pub field
      } // not Entry (article/inproceedings)
    } // inEntry
  }

  override def characters(ch: Array[Char], start: Int, length: Int): Unit = {
    if (inEntry) {
      val value = new String(ch, start, length)
      Logger.debug(s"=== $value")
      if (isInner) {
        sb.append(" " + value + "")
      } else {
        sb.append(value)
      }
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
