package models.search

import java.io.{File, IOException}
import java.util
import java.util.regex.Pattern

import cc.mallet.pipe._
import cc.mallet.topics.{ForkedTopicalNGrams, StringPair}
import cc.mallet.types.{Instance, InstanceList}
import cc.mallet.util.Randoms
import models.common.Config
import org.apache.lucene.search.{IndexSearcher, TopDocs}
import play.api.Logger

import scala.collection.JavaConversions._

case class MalletOption(numOfTopics: Int, alphaSum: Double, beta: Double, threads: Int, iterations: Int)

case class MalletResult(data: List[StringPair], duration: Long)

class A1Mallet(instanceList: InstanceList) {

  import Config._

  def run(topN: Int): MalletResult = {
    val startTime = System.currentTimeMillis()
    Logger.info("starting TNG")
    val m = new ForkedTopicalNGrams(topN)
    val res = m.estimate(instanceList, TNG_ITERATIONS, TNG_ITERATIONS / 10, new Randoms())
    val duration = System.currentTimeMillis() - startTime
    Logger.info(s"ending TNG, cost ${duration}ms")
    MalletResult(res.toList, duration)
  }
}


object A1Mallet {

  import Config._

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

  def initMalletInstanceList() = {
    val pipeList = new util.ArrayList[Pipe]()
    pipeList.add(new CharSequenceLowercase())
    val tokenPattern = Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")
    pipeList.add(new CharSequence2TokenSequence(tokenPattern))
    pipeList.add(new TokenSequenceRemoveNonAlpha(true))
    val malletStopWordFile = new File(malletSWFileName)
    if (!malletStopWordFile.exists()) {
      throw new IOException(s"mallet stopWords file $malletStopWordFile not exist")
    }
    pipeList.add(new TokenSequenceRemoveStopwords(malletStopWordFile, "UTF-8", false, false, false))
    //    pipeList.add(new TokenSequence2FeatureSequence())
    pipeList.add(new TokenSequence2FeatureSequenceWithBigrams())
    new InstanceList(new SerialPipes(pipeList))
  }

}
