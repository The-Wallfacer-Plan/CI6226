package models

case class IndexStats(time: Long, source: String)

case class SearchPub(docID: Int, score: Double, info: Map[String, String])

case class SearchStats(time: Long, statusMsg: String)

class LSearchResult(val stats: SearchStats, val pubs: List[SearchPub]) {
}