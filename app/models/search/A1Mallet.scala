package models.search

import java.io.{File, IOException}
import java.util
import java.util.regex.Pattern

import cc.mallet.pipe._
import cc.mallet.topics.{ParallelTopicModel, TopicalNGrams}
import cc.mallet.types.{FeatureSequence, Instance, InstanceList}
import cc.mallet.util.Randoms
import models.common.Config
import org.apache.lucene.search.{IndexSearcher, TopDocs}

import scala.collection.JavaConversions._

case class MalletOption(numOfTopics: Int, alphaSum: Double, beta: Double, threads: Int, iterations: Int)

class A1Mallet(instanceList: InstanceList, malletOption: MalletOption) {
  val model = {
    val m = new ParallelTopicModel(malletOption.numOfTopics, malletOption.alphaSum, malletOption.beta)
    m.addInstances(instanceList)
    m.setNumThreads(malletOption.threads)
    m.setNumIterations(malletOption.iterations)
    m
  }

  def runTNG(topN: Int) = {
    val m = new TopicalNGrams(topN)
    m.estimate(instanceList, 20, 1, 1, "MODEL_CHX", new Randoms())
  }

  def run(topN: Int) = {
    println("start TNG")
    runTNG(topN)
    println("end TNG")
//    model.estimate()
    //    val dataAlphabet = instanceList.getDataAlphabet
    //    val tokens = model.getData.get(0).instance.getData.asInstanceOf[FeatureSequence]
    //    val topics = model.getData.get(0).topicSequence
    //    for (i <- 0 until tokens.getLength) {
    //      println(s"${dataAlphabet.lookupObject(tokens.getIndexAtPosition(i)).toString.replace('_', ' ')}, ${topics.getIndexAtPosition(i)}")
    //    }
  }
}


object A1Mallet {

  import Config._

  def initMalletInstanceList() = {
    val pipeList = new util.ArrayList[Pipe]()
    pipeList.add(new CharSequenceLowercase())
    //        val tokenPattern = CharSequenceLexer.LEX_NONWHITESPACE_CLASSES
    val tokenPattern = Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")
    pipeList.add(new CharSequence2TokenSequence(tokenPattern))
    pipeList.add(new TokenSequenceRemoveNonAlpha(true))
    val malletStopWordFile = new File(malletSWFileName)
    if (!malletStopWordFile.exists()) {
      throw new IOException(s"mallet stopWords file $malletStopWordFile not exist")
    }
    pipeList.add(new TokenSequenceRemoveStopwords(new File(malletSWFileName), "UTF-8", false, false, false))
    //    val sizes = Array(4)
    //    pipeList.add(new TokenSequenceNGrams(sizes))
    //    pipeList.add(new TokenSequence2FeatureSequence())
    pipeList.add(new TokenSequence2FeatureSequenceWithBigrams())
    new InstanceList(new SerialPipes(pipeList))
  }

  def getProcessedInstances(topDocs: TopDocs, searcher: IndexSearcher): InstanceList = {
    val instanceList = initMalletInstanceList()
    val scoreDocs = topDocs.scoreDocs
    val instanceArray = for (scoreDoc <- scoreDocs) yield {
      val docID = scoreDoc.doc
      val hitDoc = searcher.doc(docID)
      val paperID = hitDoc.get(I_PAPER_ID)
      val title = hitDoc.get(I_TITLE)
      new Instance(title, "X", paperID, null)
    }
    instanceList.addThruPipe(instanceArray.iterator)
    instanceList
  }

  def getInstanceData(instanceList: InstanceList) = {
    for (instance <- instanceList) yield {
      val data = instance.getData.asInstanceOf[FeatureSequence]
      //      System.err.println(instance.getData)
    }
  }

}