package models.search

import java.io.{File, IOException}
import java.util
import java.util.regex.Pattern

import cc.mallet.pipe._
import cc.mallet.topics.{ForkedTopicalNGrams, StringPair}
import cc.mallet.types.{Instance, InstanceList}
import cc.mallet.util.Randoms
import models.common.{Config, Helper}
import org.apache.lucene.search.{IndexSearcher, TopDocs}
import play.api.Logger

import scala.collection.JavaConversions._
import scala.collection.mutable

case class MalletOption(numOfTopics: Int, alphaSum: Double, beta: Double, threads: Int, iterations: Int)

case class TNGResults(data: List[StringPair], duration: Long)

class A1Mallet(instances: Array[Instance]) {

  import Config._
  import Helper._

  def getSimpleInstances(): InstanceList = {
    val pipeList = new util.ArrayList[Pipe]()
    pipeList.add(new CharSequenceLowercase())
    val tokenPattern = Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")
    pipeList.add(new CharSequence2TokenSequence(tokenPattern))
    pipeList.add(new TokenSequenceRemoveNonAlpha(true))
    val malletStopWordFile = new File(longSWFName)
    if (!malletStopWordFile.exists()) {
      throw new IOException(s"mallet stopWords file $malletStopWordFile not exist")
    }
    pipeList.add(new TokenSequenceRemoveStopwords(malletStopWordFile, "UTF-8", false, false, false))
    pipeList.add(new TokenSequence2FeatureSequenceWithBigrams())
    val instanceList = new InstanceList(new SerialPipes(pipeList))
    instanceList.addThruPipe(instances.iterator)
    instanceList
  }

  def getNGramInstances(sizes: Array[Int]): InstanceList = {
    val pipeList = new util.ArrayList[Pipe]()
    pipeList.add(new CharSequenceLowercase())
    val tokenPattern = Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")
    pipeList.add(new CharSequence2TokenSequence(tokenPattern))
    pipeList.add(new TokenSequenceRemoveNonAlpha(true))
    val malletStopWordFile = new File(longSWFName)
    if (!malletStopWordFile.exists()) {
      throw new IOException(s"mallet stopWords file $malletStopWordFile not exist")
    }
    pipeList.add(new TokenSequenceRemoveStopwords(malletStopWordFile, "UTF-8", false, false, false))
    pipeList.add(new TokenSequenceNGrams(sizes))
    pipeList.add(new TokenSequence2FeatureSequence())
    //    pipeList.add(new TokenSequence2FeatureSequenceWithBigrams())
    val instanceList = new InstanceList(new SerialPipes(pipeList))
    instanceList.addThruPipe(instances.iterator)
    instanceList
  }

  def runTNG(topN: Int): TNGResults = {
    val instanceList = getSimpleInstances()
    val startTime = System.currentTimeMillis()
    Logger.info("starting TNG")
    val m = new ForkedTopicalNGrams(topN)
    val res = m.estimate(instanceList, TNG_ITERATIONS, TNG_ITERATIONS / 10, new Randoms())
    val duration = System.currentTimeMillis() - startTime
    Logger.info(s"ending TNG, cost ${duration}ms")
    TNGResults(res.toList, duration)
  }

  def runNGrams(topN: Int, sizes: Array[Int]): Array[TopEntryTy] = {
    val instanceList = getNGramInstances(sizes)
    val map = mutable.Map[String, Long]()
    for (instance <- instanceList) {
      val data = instance.getData
      for (s <- data.toString.split("\n")) {
        val ngrams = s.split("\\s+")
        if (ngrams.length >= 2) {
          val ngram = ngrams(1)
          if (map.contains(ngram)) {
            map += ngram -> (map(ngram) + 1)
          } else {
            map += ngram -> 1
          }
        }
      }
    }
    val res = selectionSort(map, topN)
    for (entry <- res) yield entry._1 -> entry._2.replace('_', ' ')
  }
}


object A1Mallet {

  import Config._

  def apply(topDocs: TopDocs, searcher: IndexSearcher): A1Mallet = {
    val scoreDocs = topDocs.scoreDocs
    val instances = for (scoreDoc <- scoreDocs) yield {
      val docID = scoreDoc.doc
      val hitDoc = searcher.doc(docID)
      val paperID = hitDoc.get(I_PAPER_ID)
      val title = hitDoc.get(I_TITLE)
      new Instance(title, "X", paperID, null)
    }
    new A1Mallet(instances)
  }

}
