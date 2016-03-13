package models.core

import java.util.regex.Pattern

import org.apache.lucene.analysis.Analyzer.TokenStreamComponents
import org.apache.lucene.analysis.core.{LowerCaseFilter, StopAnalyzer, StopFilter}
import org.apache.lucene.analysis.en.PorterStemFilter
import org.apache.lucene.analysis.pattern.PatternTokenizer
import org.apache.lucene.analysis.standard.StandardTokenizer
import org.apache.lucene.analysis.{Analyzer, TokenStream}
import play.api.Logger

class LAnalyzer(option: LOption, patternString: String) extends Analyzer {

  def this() = this(new LOption(stemming = false, ignoreCase = true, "None"), null)

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
      Logger.info("lower case filter")
      result = new LowerCaseFilter(result)
    }
    if (option.swDict.equalsIgnoreCase("Lucene")) {
      Logger.info("Lucene stopWords filter")
      result = new StopFilter(result, StopAnalyzer.ENGLISH_STOP_WORDS_SET)
    } else if (option.swDict.equals("None")) {
      Logger.info("empty stopWords filter")
    }
    if (option.stemming) {
      Logger.info("porter stemming filter")
      result = new PorterStemFilter(result)
    }
    new TokenStreamComponents(tokenizer, result)

  }
}
