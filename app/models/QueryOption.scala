package models

case class QueryOption(valid: Boolean, fieldMap: Map[String, String], conj: String)


object QueryOption {
  val pat = "(paperId|title|kind|authors|venue|pubYear):\"[\\w\\s]+\"".r
  val conjList = List("OR", "AND")

  def apply(queryString: String): QueryOption = {

    null
  }
}