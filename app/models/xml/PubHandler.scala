package models.xml

import models.core.LIndexer
import org.slf4j.LoggerFactory
import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.{Attributes, SAXException, SAXParseException}

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

  def isInterestingEntry(qName: String) = List(ARTICLE, INPROCEEDINGS).contains(qName)

  def isVenueEntry(qName: String) = List(JOURNAL, BOOKTITLE).contains(qName)

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
    if (isInterestingEntry(qName)) {
      if (inEntry) {
        inEntry = false
        isTitle = false
        isAuthor = false
        isYear = false
        isVenue = false
        unknown = false
        indexer.index(pub)
        pub = null
      }
    }
  }

  override def characters(ch: Array[Char], start: Int, length: Int): Unit = {
    //        other venues might be not interesting
    if (inEntry) {
      val value = new String(ch, start, length)
      if (isTitle) {
        pub.title = value
        isTitle = false
      }
      if (isAuthor) {
        pub.addAuthor(value)
        isAuthor = false
      }
      if (isYear) {
        pub.pubYear = value
        isYear = false
      }
      if (isVenue) {
        pub.venue = value
        isVenue = false
      }
      if (unknown) {
        unknown = false
      }
    }
  }

  private def Message(mode: String, exception: SAXParseException) {
    System.out.println(mode + " Line: " + exception.getLineNumber()
      + " URI: " + exception.getSystemId() + "\n" + " Message: " + exception.getMessage());
  }

  override def warning(exception: SAXParseException): Unit = {
    Message("**Parsing Warning**\n", exception)
    throw new SAXException("Warning encountered")
  }

  override def error(exception: SAXParseException): Unit = {
    Message("**Parsing Error**\n", exception)
    throw new SAXException("Error encountered")
  }

  override def fatalError(exception: SAXParseException): Unit = {
    Message("**Parsing Fatal Error**\n", exception)
    throw new SAXException("Fatal Error encountered")
  }
}
