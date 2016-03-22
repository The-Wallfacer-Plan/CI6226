package models.common

import java.nio.file.{Files, Paths}

import org.apache.lucene.index._
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.BytesRef
import play.api.Logger


class TopFreq(indexFolderString: String) {

  val reader: IndexReader = {
    val indexFolder = Paths.get(indexFolderString)
    require(Files.exists(indexFolder))
    val directory = FSDirectory.open(indexFolder)
    DirectoryReader.open(directory)
  }

  val searcher = new IndexSearcher(reader)

  val tops = Array.fill[(Term, Int)](Config.topN)((null, 0))

  def analyzeTerm(termsEnum: TermsEnum): Unit = {
    val bytesRef = termsEnum.next()
    val termText = bytesRef.utf8ToString()
    if (Config.ignoredTerms.contains(termText)) {
      return
    }
  }

  def analyze(fieldName: String) = {


    val fields = MultiFields.getFields(reader)
    val terms = fields.terms(fieldName)
    Logger.info(s"size=${terms.size()} ${terms.getMax.utf8ToString()}, ${terms.getMin.utf8ToString()}")
    val itr = terms.iterator()
    var bytesRef: BytesRef = itr.next()
    while (bytesRef != null) {
      val termText = bytesRef.utf8ToString()
      if (!Config.ignoredTerms.contains(termText)) {
        val termInstance = new Term(fieldName, bytesRef)
        val tf = reader.totalTermFreq(termInstance)
        require(tf == itr.totalTermFreq())
        val docCount = reader.docFreq(termInstance)
        println(s"term: $termText, termFreq=$tf, docCount=$docCount")
        bytesRef = itr.next()
      }
    }
  }

}
