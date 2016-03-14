package models

case class Stats(time: Long)

case class SearchPub(score: Double, info: List[String])

class LSearchResult(val status: String, val stats: Stats, val pubs: List[SearchPub]) {
}
