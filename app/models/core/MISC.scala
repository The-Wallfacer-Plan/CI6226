package models.core

import java.nio.file.{Files, Paths}

import org.apache.lucene.index.{DirectoryReader, IndexReader, Term}
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.BytesRef

class MISC(indexFolderString: String) {
  val reader: IndexReader = {
    val indexFolder = Paths.get(indexFolderString)
    require(Files.exists(indexFolder))
    val directory = FSDirectory.open(indexFolder)
    DirectoryReader.open(directory)
  }

  def analyze(fieldName: String) = {
    val terms = reader.getTermVector(2, fieldName)
    val itr = terms.iterator()
    var term: BytesRef = itr.next()
    while (term != null) {
      val termText = term.utf8ToString()
      val termInstance = new Term(fieldName, term)
      val tf = reader.totalTermFreq(termInstance)
      val docCount = reader.docFreq(termInstance)
      println(s"term: $termText, termFreq=$tf, docCount=$docCount")
      term = itr.next()
    }

  }

}
