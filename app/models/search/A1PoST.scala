package models.search

import edu.stanford.nlp.ling.Sentence
import edu.stanford.nlp.tagger.maxent.MaxentTagger
import models.common.Config._
import org.apache.lucene.index.IndexReader
import org.apache.lucene.search.{IndexSearcher, TopDocs}
import org.apache.lucene.util.BytesRef
import play.api.Logger

import scala.collection.mutable

case class A1PoST(topDocs: TopDocs, searcher: IndexSearcher, reader: IndexReader) {
  val tagger = {
    val taggerFile = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger"
    new MaxentTagger(taggerFile)
  }

  def getTitles() = {
    val scoreDocs = topDocs.scoreDocs
    for (scoreDoc <- scoreDocs) yield {
      val docID = scoreDoc.doc
      val terms = reader.getTermVector(docID, I_TITLE)
      val termArrayBuffer = new mutable.ArrayBuffer[String]()
      if (terms != null) {
        val itr = terms.iterator()
        var bytesRef: BytesRef = itr.next()
        while (bytesRef != null) {
          val termText = bytesRef.utf8ToString()
          termArrayBuffer += termText
          bytesRef = itr.next()
        }
      } else {
        Logger.info(s"$I_TITLE null TermVector: $I_TITLE=${
          searcher.doc(docID).get(I_TITLE)
        }, paperId=${
          searcher.doc(docID).get(I_PAPER_ID)
        }")
      }
      termArrayBuffer.toArray
    }
  }


  def run(topN: Int): Array[TopEntryTy] = {
    val tokenized = {
      for (title <- getTitles()) yield {
        Sentence.toWordList(title: _*)
      }
    }

    for (sentence <- tokenized) {
      val tSentence = tagger.tagSentence(sentence)
      println(Sentence.listToString(tSentence, false))
      println(Sentence.listToString(tSentence, true))
    }
    Array.empty
  }
}

