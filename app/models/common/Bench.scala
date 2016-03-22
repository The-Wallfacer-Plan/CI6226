package models.common

import java.io.{BufferedReader, File, FileReader, PrintWriter}
import java.nio.file.Paths

import org.apache.lucene.benchmark.quality.trec.{TrecJudge, TrecTopicsReader}
import org.apache.lucene.benchmark.quality.utils.{SimpleQQParser, SubmissionReport}
import org.apache.lucene.benchmark.quality.{QualityBenchmark, QualityQuery, QualityStats}
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory

class Bench {
  def run(indexFolder: String) = {
    val topicsFile = new File(Config.tempDir + "/topicsFile")
    val qreslFile = new File(Config.tempDir + "/qrelsFile")
    val dir = FSDirectory.open(Paths.get(indexFolder))
    val reader = DirectoryReader.open(dir)
    val searcher = new IndexSearcher(reader)
    val docNameField = "title"
    val logger = new PrintWriter(System.out, true)
    val qReader = new TrecTopicsReader()
    val bfReader = new BufferedReader(new FileReader(topicsFile))
    val qqs: Array[QualityQuery] = qReader.readQueries(bfReader)
    val qresReader = new BufferedReader(new FileReader(qreslFile))
    val judge = new TrecJudge(qresReader)
    judge.validateData(qqs, logger)
    val qqParser = new SimpleQQParser("title", "content")
    val qrun = new QualityBenchmark(qqs, qqParser, searcher, docNameField)
    val submitLog: SubmissionReport = null
    val stats: Array[QualityStats] = qrun.execute(judge, submitLog, logger)
    val avg = QualityStats.average(stats)
    avg.log("SUMMARY", 2, logger, "  ")
    dir.close()
  }
}
