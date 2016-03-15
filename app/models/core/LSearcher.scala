package models.core

import java.nio.file.{Files, Paths}

import models.utility.Config
import models.{LSearchPub, LSearchResult, LSearchStats}
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search._
import org.apache.lucene.store.FSDirectory
import play.api.Logger
import play.api.libs.json.{JsBoolean, JsObject, JsString, Json}

import scala.collection.JavaConversions._

case class LQueryOption(valid: Boolean, fieldMap: Map[String, String] = Map.empty, conj: String = Config.DEFAULT_CONJ) {
  Logger.info(s"query: ${this}")

  def jsonify(): JsObject = {
    JsObject(Seq(
      "valid" -> JsBoolean(valid),
      "conjunctor" -> JsString(conj),
      "field-query" -> Json.toJson(fieldMap)
    ))
  }
}

object LQueryOption {

  val PatternField = "(paperId|title|kind|authors|venue|pubYear):\"([\\w\\s]+)\"".r
  val conjunctions = ("AND", "OR")

  // queryString should be trimmed firstly
  def apply(queryString: String): LQueryOption = {
    try {
      val conj = {
        if (queryString.contains(conjunctions._1)) {
          require(!queryString.contains(conjunctions._2))
          conjunctions._1
        } else if (queryString.contains(conjunctions._2)) {
          require(!queryString.contains(conjunctions._1))
          conjunctions._2
        } else {
          Config.DEFAULT_CONJ
        }
      }
      val fieldMap: Map[String, String] = {
        if (conj.equals(Config.DEFAULT_CONJ)) {
          queryString match {
            case PatternField(field, content) => Map(field -> content)
            case _ => Map(Config.COMBINED_FIELD -> queryString)
          }
        } else {
          queryString.split(conj).map(entry => {
            entry.trim() match {
              case PatternField(field, content) => field -> content
              case _ => {
                new IllegalArgumentException
                Config.COMBINED_FIELD -> queryString
              }
            }
          }).toMap
        }
      }
      LQueryOption(valid = true, fieldMap, conj)
    } catch {
      case e: IllegalArgumentException => new LQueryOption(false)
    }
  }
}


class LSearcher(lOption: LOption, indexFolderString: String) {
  val analyzer = new LAnalyzer(lOption, null)
  val reader = {
    val indexFolder = Paths.get(indexFolderString)
    require(Files.exists(indexFolder))
    val directory = FSDirectory.open(indexFolder)
    DirectoryReader.open(directory)
  }
  //  {
  //    val tokenStream = analyzer.tokenStream("authors", new StringReader("text here"))
  //    val offsetAt = tokenStream.addAttribute(classOf[OffsetAttribute])
  //    tokenStream.reset()
  //    while (tokenStream.incrementToken()) {
  //      Logger.info(s"===>${tokenStream.reflectAsString(false)}")
  //    }
  //  }
  val searcher = new IndexSearcher(reader)

  private def searchOneField(field: String, string: String): TopDocs = {
    val parser = new QueryParser(field, analyzer)
    parser.setAllowLeadingWildcard(true)
    val query = parser.parse(string)
    val booleanQuery = new BooleanQuery.Builder().add(query, BooleanClause.Occur.MUST).build()
    searcher.search(booleanQuery, Config.topN)
  }

  def getSearchPub(topDocs: TopDocs, field: String): List[LSearchPub] = {
    topDocs.scoreDocs.toList map {
      hit => {
        val docID = hit.doc
        val score = hit.score
        val hitDoc = searcher.doc(docID)
        Logger.info(s"${hitDoc.getField(field)}")
        val fieldValues = for {
          field <- hitDoc.getFields
          if field.name() != Config.COMBINED_FIELD
        } yield {
          field.name() -> field.stringValue()
        }
        val fieldDocMap = Map(fieldValues: _*)
        new LSearchPub(docID, score, fieldDocMap)
      }
    }
  }

  def searchImp(queryString: String): LSearchResult = {
    val queryOption = LQueryOption(queryString)
    if (!queryOption.valid) {
      Logger.info(s"invalid query string: $queryString")
      LSearchResult(LSearchStats(0, queryOption), List.empty)
    } else {
      queryOption.conj match {
        case Config.DEFAULT_CONJ => {
          val fieldMap = queryOption.fieldMap
          require(fieldMap.size == 1)
          val (field, query) = fieldMap.head
          Logger.info(s"query string: $queryString\n$field=>\t$query")
          val timeStart = System.currentTimeMillis()
          val topDocs = searchOneField(field, query)
          val duration = System.currentTimeMillis() - timeStart
          val searchPubs = getSearchPub(topDocs, field)
          Logger.info(s"searchPub: $searchPubs")
          LSearchResult(LSearchStats(duration, queryOption), searchPubs)
        }
        case conj => {
          val fieldMap = queryOption.fieldMap
          val fieldQueryList = for ((f, q) <- fieldMap) yield s"$f=>\t$q"
          Logger.info(s"query string: $queryString\nConjunction=$conj\n" +
            s"${fieldQueryList.mkString("\n")}")
          val timeStart = System.currentTimeMillis()
          val fieldResultMap = for ((field, query) <- fieldMap) yield {
            field -> searchOneField(field, query)
          }
          // TODO
          conj match {
            case "AND" => {
              val duration = System.currentTimeMillis() - timeStart
              LSearchResult(LSearchStats(duration, queryOption), List.empty)
            }
            case "OR" => {
              val duration = System.currentTimeMillis() - timeStart
              LSearchResult(LSearchStats(duration, queryOption), List.empty)
            }
          }
        }
      }

    }
  }
}
