package models.search

import models.common.Config
import org.apache.lucene.index.IndexReader
import org.apache.lucene.search.{IndexSearcher, TopDocs}
import org.apache.lucene.util.BytesRef
import play.api.Logger

import scala.collection.mutable

case class A1Term(topDocs: TopDocs, searcher: IndexSearcher, reader: IndexReader) {

  import Config._

  private def selectionSort(textMap: scala.collection.mutable.Map[String, Long], topN: Int): Array[TopEntryTy] = {
    val array = Array.fill[TopEntryTy](topN)(0L -> null)
    var i = 0
    while (i < array.length) {
      var current: TopEntryTy = 0L -> null
      for (entry <- textMap) {
        if (current._1 < entry._2) {
          current = entry._2 -> entry._1
        }
      }
      array(i) = current
      textMap -= current._2
      i += 1
    }
    array
  }


  def run(topN: Int, topicsField: String = I_TITLE): Array[TopEntryTy] = {
    val termMap = mutable.Map.empty[String, Long]
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
          if (!ignoredTerms.contains(termText)) {
            val tf = itr.totalTermFreq()
            if (termMap.contains(termText)) {
              termMap += termText -> (termMap(termText) + tf)
            } else {
              termMap += termText -> tf
            }
          }
          bytesRef = itr.next()
        }
      } else {
        Logger.info(s"$topicsField null TermVector: $topicsField=${
          searcher.doc(docID).get(topicsField)
        }, paperId=${
          searcher.doc(docID).get(I_PAPER_ID)
        }")
      }
      i += 1
    }
    selectionSort(termMap, topN)
  }

}
