package models.utility

import java.io.File

object Config {
  val homeDir = System.getProperty("user.home")
  val tempDir = System.getProperty("java.io.tmpdir")
  val rootDir = homeDir + File.separator + "Dropbox/PHDCourses/IR/assignment"
  val xmlFile = rootDir + File.separator + "sample.xml"
  val VALIDATION = "http://xml.org/sax/features/validation"
  val indexRoot = tempDir + File.separator + "index"
  val splitString = "; "

  val I_PAPER_ID = "paperId"
  val I_TITLE = "title"
  val I_KIND = "kind"
  val I_AUTHORS = "authors"
  val I_VENUE = "venue"
  val I_PUB_YEAR = "pubYear"

  val defaultFields = List(I_PAPER_ID, I_TITLE, I_KIND, I_AUTHORS, I_VENUE, I_PUB_YEAR)
  val DBLPNOTE = "dblpnote"

  //    interface Configurable
  val topN = 10
}
