package models.search

import java.nio.file.{Files, Paths}

import models.common.{Config, LAnalyzer, LOption}
import org.apache.lucene.index.{DirectoryReader, Term}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search._
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.BytesRef
import play.api.Logger
import play.api.libs.json._

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.control.Breaks.{break, breakable}


case class LSearchPub(docID: Int, score: Double, info: Map[String, String])

case class LSearchStats(time: Long, queryString: String, query: Option[Query]) {
  def toJson(): JsValue = {
    val parsedQuery = {
      query match {
        case Some(q) => JsString(q.toString())
        case None => JsNull
      }
    }
    JsObject(Seq(
      "time" -> JsString(time.toString + "ms"),
      "queryString" -> JsString(queryString),
      "parsedQuery" -> parsedQuery
    ))
  }
}

class LSearchResult(stats: LSearchStats, lOption: Option[LOption], val pubs: Array[LSearchPub]) {
  def toJson(): JsValue = {
    val lOptionJson = {
      lOption match {
        case Some(l) => l.toJson()
        case None => JsNull
      }
    }
    JsObject(Seq(
      "stats" -> stats.toJson(),
      "found" -> JsNumber(pubs.length),
      "searchOption" -> lOptionJson
    ))
  }

  def toJsonString(): String = {
    Json.prettyPrint(toJson())
  }
}

class LSearcher(lOption: LOption, indexFolderString: String, topN: Int) {

  type TopEntryTy = (Long, String)

  val analyzer = new LAnalyzer(lOption, None)
  val reader = {
    val indexFolder = Paths.get(indexFolderString)
    require(Files.exists(indexFolder))
    val directory = FSDirectory.open(indexFolder)
    DirectoryReader.open(directory)
  }

  val searcher = {
    val s = new IndexSearcher(reader)

    //    val similarity = new BM25Similarity()
    //    s.setSimilarity(similarity)
    s
  }

  private def validate(queryString: String): Unit = {
  }

  def reOrder(entry: TopEntryTy, topsArray: Array[TopEntryTy]): Unit = {
    var i = 0
    breakable {
      while (i < topsArray.length) {
        if (entry._1 > topsArray(i)._1) {
          topsArray(i) = entry
          break
        }
        i += 1
      }
    }
  }

  def getTopFreq(topDocs: TopDocs, topicsField: String): Array[TopEntryTy] = {
    var visitedSet = mutable.Set[String]()
    val topsArray = Array.fill[TopEntryTy](topN)(0L -> null)
    val scoreDocs = topDocs.scoreDocs
    var i = 0
    while (i < scoreDocs.size) {
      val scoreDoc = scoreDocs(i)
      val docID = scoreDoc.doc
      val terms = reader.getTermVector(docID, topicsField)
      if (terms != null) {
        Logger.debug(s"=${searcher.doc(docID).get(topicsField)} ${scoreDocs.size}")
        val itr = terms.iterator()
        var bytesRef: BytesRef = itr.next()
        while (bytesRef != null) {
          val termText = bytesRef.utf8ToString()
          if (!Config.ignoredTerms.contains(termText) && !visitedSet.contains(termText)) {
            val termInstance = new Term(topicsField, bytesRef)
            val tf = reader.totalTermFreq(termInstance)
            reOrder((tf, termText), topsArray)
            visitedSet += termText
          }
          bytesRef = itr.next()
        }
      } else {
        Logger.info(s"$topicsField null TermVector: $topicsField=${searcher.doc(docID).get(topicsField)}, paperId=${searcher.doc(docID).get(Config.I_PAPER_ID)}")
      }
      i += 1
    }
    topsArray
  }

  def evaluate(queryString: String, topicsField: String = Config.I_TITLE): Array[TopEntryTy] = {
    val queryOrNone = Option {
      val parser = new QueryParser(Config.I_PUB_YEAR, analyzer)
      parser.setAllowLeadingWildcard(false)
      parser.parse(queryString)
    }
    Logger.info(s"string=$queryString, query=$queryOrNone")
    queryOrNone match {
      case None => Array.empty[TopEntryTy]
      case Some(query) => {
        val collector = new TotalHitCountCollector()
        searcher.search(query, collector)
        Logger.info(s"${collector.getTotalHits}")
        val result = searcher.search(query, math.max(1, collector.getTotalHits))
        getTopFreq(result, topicsField)
      }
    }
  }

  def search(queryString: String): LSearchResult = {
    val startTime = System.currentTimeMillis()
    validate(queryString)
    val queryOrNone = Option {
      val parser = new QueryParser(Config.COMBINED_FIELD, analyzer)
      parser.setAllowLeadingWildcard(true)
      parser.parse(queryString)
    }
    Logger.info(s"string=$queryString, query=$queryOrNone")
    queryOrNone match {
      case None => {
        val searchStats = LSearchStats(0, queryString, queryOrNone)
        new LSearchResult(searchStats, Some(lOption), Array())
      }
      case Some(query) => {
        val topDocs = searcher.search(query, topN)
        val duration = System.currentTimeMillis() - startTime
        val foundPubs = getSearchPub(topDocs, query)
        reader.close()
        val searchStats = LSearchStats(duration, queryString, queryOrNone)
        new LSearchResult(searchStats, Some(lOption), foundPubs)
      }
    }
  }

  def getSearchPub(topDocs: TopDocs, query: Query): Array[LSearchPub] = {
    for (hit <- topDocs.scoreDocs) yield {
      val (docID, score) = (hit.doc, hit.score)
      val hitDoc = searcher.doc(docID)
      //      Logger.info(s"Explain: ${searcher.explain(query, docID)}")
      val fieldValues = for {
        field <- hitDoc.getFields
        if field.name() != Config.COMBINED_FIELD
      } yield field.name() -> field.stringValue()
      val fieldDocMap = Map(fieldValues: _*)
      new LSearchPub(docID, score, fieldDocMap)
    }
  }

}
