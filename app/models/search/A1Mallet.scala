package models.search

import java.io.File
import java.util
import java.util.regex.Pattern

import cc.mallet.pipe._
import cc.mallet.topics.ParallelTopicModel
import cc.mallet.types.{FeatureSequence, Instance, InstanceList}
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

  def run() = {
    model.estimate()
    val dataAlphabet = instanceList.getDataAlphabet
    val tokens = model.getData.get(0).instance.getData.asInstanceOf[FeatureSequence]
    val topics = model.getData.get(0).topicSequence
    for (i <- 0 until tokens.getLength) {
      println(s"${dataAlphabet.lookupObject(tokens.getIndexAtPosition(i))}, ${topics.getIndexAtPosition(i)}")
    }
  }
}


object A1Mallet {

  import Config._

  def initMalletInstanceList() = {
    val pipeList = new util.ArrayList[Pipe]()
    pipeList.add(new CharSequenceLowercase())
    pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")))
    pipeList.add(new TokenSequenceRemoveStopwords(new File(malletRoot + "/stoplists/en.txt"), "UTF-8", false, false, false))
    pipeList.add(new TokenSequence2FeatureSequence())
    new InstanceList(new SerialPipes(pipeList))
  }

  def getProcessedInstances(topDocs: TopDocs, searcher: IndexSearcher): InstanceList = {
    val scoreDocs = topDocs.scoreDocs
    val instanceList = initMalletInstanceList()
    val instanceArray = for (scoreDoc <- scoreDocs) yield {
      val docID = scoreDoc.doc
      val hitDoc = searcher.doc(docID)
      val paperID = hitDoc.get(I_PAPER_ID)
      val title = hitDoc.get(I_TITLE)
      new Instance(title, "X", paperID, null)
    }
    instanceList.addThruPipe(instanceArray.iterator)
    //    for (i <- 0 until 10) {
    //      val instance = instanceArray(i)
    //      System.err.print(s"${instance.getName} ${instance.getData}")
    //    }
    instanceList
  }

  def getInstanceData(instanceList: InstanceList) = {
    for (i <- 0 until 10) {
      val instance = instanceList(i)
      val data = instance.getData
    }
  }

}
