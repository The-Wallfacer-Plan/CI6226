package models.search

import java.util

import cc.mallet.types.InstanceList
import edu.stanford.nlp.ling.{HasWord, Sentence}
import edu.stanford.nlp.tagger.maxent.MaxentTagger
import models.common.Helper
import org.apache.lucene.index.IndexReader
import org.apache.lucene.search.{IndexSearcher, TopDocs}
import play.api.Logger

import scala.collection.JavaConversions._
import scala.collection.mutable

case class A1PoST(topDocs: TopDocs, searcher: IndexSearcher, reader: IndexReader) {

  import Helper._

  val tagger = {
    val taggerFile = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger"
    new MaxentTagger(taggerFile)
  }

  def filtering(sentence: util.List[HasWord], topEntries: util.Map[String, Long]) = {
    val length = sentence.length
    val tSentence = tagger.tagSentence(sentence)
    var i = 0
    while (i < length) {
      val tw = tSentence(0)
      val tag = tw.tag()
      if (tag == "NN" || tag == "NNS") {
        val s = tw.value()
        if (topEntries.containsKey(s)) {
          val count = topEntries.get(s)
          topEntries.put(s, count + 1)
        } else {
          topEntries.put(s, 1)
        }
      }
      i += 1
    }
  }

  def run(instanceList: InstanceList, topN: Int): Any = {
    Logger.info(s"got ngrams, instance size=${instanceList.size()}")
    val sentences = {
      for (instance <- instanceList) yield {
        val sb = new mutable.ArrayBuffer[String]()
        val data = instance.getData.toString
        if (data.length == 0 || data == null) {
          Sentence.toWordList("XXXXXXXXXXXXXXXXXXXXX")
        } else {
          for (s <- data.toString.split("\n")) {
            sb += s.split(' ')(1).replace('_', ' ')
          }
          Sentence.toWordList(sb.toArray: _*)
        }
      }
    }
    Logger.info(s"got sentences, size=${sentences.size}, now tagging")
    val topEntries = new util.HashMap[String, Long]()
    for (i <- sentences.indices) {
      val sentence = sentences(i)
      if (i % 5000 == 0) {
        Logger.info(s"sentence=$sentence, i=$i")
      }
      filtering(sentence, topEntries)
    }
    val res = selectionSort(topEntries, topN)
    for (r <- res) {
      Logger.info(s"$r")
    }

  }
}

