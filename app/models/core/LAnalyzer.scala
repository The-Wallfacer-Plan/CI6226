package models.core

import java.util.regex.Pattern

import org.apache.lucene.analysis.Analyzer.TokenStreamComponents
import org.apache.lucene.analysis.core.{LowerCaseFilter, StopAnalyzer, StopFilter}
import org.apache.lucene.analysis.en.PorterStemFilter
import org.apache.lucene.analysis.pattern.PatternTokenizer
import org.apache.lucene.analysis.standard.StandardTokenizer
import org.apache.lucene.analysis.{Analyzer, TokenStream}
import org.slf4j.LoggerFactory

class LAnalyzer(option: LIndexOption, patternString: String) extends Analyzer {

  val logger = LoggerFactory.getLogger(classOf[LAnalyzer])

  def this() = this(new LIndexOption(false, true, "None"), null)

  override def createComponents(fieldName: String): TokenStreamComponents = {
    val tokenizer = {
      if (patternString == null) {
        new StandardTokenizer()
      } else {
        val pattern = Pattern.compile(patternString)
        new PatternTokenizer(pattern, -1)
      }
    }

    var result: TokenStream = new EmptyStringTokenFilter(tokenizer)
    if (option.ignoreCase) {
      logger.info("lower case filter")
      result = new LowerCaseFilter(result)
    }
    if (option.swDict.equals("Lucene")) {
      logger.info("Lucene stopWords filter")
      result = new StopFilter(result, StopAnalyzer.ENGLISH_STOP_WORDS_SET)
    } else if (option.swDict.equals("None")) {
      logger.info("empty stopWords filter")
    }
    if (option.stemming) {
      logger.info("porter stemming filter")
      result = new PorterStemFilter(result)
    }
    new TokenStreamComponents(tokenizer, result)

  }
}
