package models.common

import java.io.File

import scala.io.Source

object Config {

  val homeDir = System.getProperty("user.home")
  val tempDir = System.getProperty("java.io.tmpdir")
  val rootDir = homeDir + File.separator + "Dropbox/PHDCourses/IR/assignment"
  val VALIDATION = "http://xml.org/sax/features/validation"
  val indexRoot = tempDir + File.separator + "index"
  val splitString = "; "
  //  val xmlFile = rootDir + File.separator + "dblp.xml"
  val xmlFile = rootDir + File.separator + "sample.xml"

  val ignoredTerms = {
    val filterFileName = rootDir + File.separator + "topics_filter"
    val ignored = Source.fromFile(filterFileName).getLines().toSet
    require(ignored.nonEmpty)
    ignored
  }


  val I_PAPER_ID = "paperId"
  val I_TITLE = "title"
  val I_KIND = "kind"
  val I_AUTHORS = "authors"
  val I_VENUE = "venue"
  val I_PUB_YEAR = "pubYear"

  val defaultFields = List(I_PAPER_ID, I_TITLE, I_KIND, I_AUTHORS, I_VENUE, I_PUB_YEAR)
  val DBLPNOTE = "dblpnote"

  val DEFAULT_CONJ = "NIL"
  val COMBINED_FIELD = "ALL"

  //    interface Configurable
  val topN = 20

  val defaultSearchString = "title:\"Google\" authors:\"Thilo Weichert\""
}
