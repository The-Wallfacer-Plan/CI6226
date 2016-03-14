package models

case class SearchStats(time: Long)

case class IndexStats(time: Long, source: String)

case class SearchPub(docID: Int, score: Double, info: Map[String, String])

class LSearchResult(val status: String, val stats: SearchStats, val pubs: List[SearchPub]) {
}