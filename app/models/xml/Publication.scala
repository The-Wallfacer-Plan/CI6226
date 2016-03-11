package models.xml

import scala.collection.mutable.ListBuffer

class Publication() {
  var paperId: String = null
  var title: String = null
  var kind: String = null
  var venue: String = null
  var authors = ListBuffer[String]()
  var pubYear: String = null

  def validate() = {
    require(paperId != null)
    require(title != null)
    require(kind != null)
    require(venue != null)
    //    require(authors.nonEmpty)
    require(pubYear != null)
  }

  def addAuthor(author: String): Unit = {
    authors += author
  }

}
