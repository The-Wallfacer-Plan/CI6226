package models

case class Stats(time: Long)

case class SearchPub(docID: Int, score: Double, info: List[String])

class LSearchResult(val status: String, val stats: Stats, val pubs: List[SearchPub]) {
}
