package models.common

import java.util.regex.Pattern

import org.apache.lucene.analysis.Analyzer.TokenStreamComponents
import org.apache.lucene.analysis.core.{LowerCaseFilter, StopAnalyzer, StopFilter}
import org.apache.lucene.analysis.en.PorterStemFilter
import org.apache.lucene.analysis.pattern.PatternTokenizer
import org.apache.lucene.analysis.standard.{StandardFilter, StandardTokenizer}
import org.apache.lucene.analysis.{Analyzer, TokenStream}
import play.api.Logger

class LAnalyzer(option: LOption, patternString: Option[String]) extends Analyzer {

  override def createComponents(fieldName: String): TokenStreamComponents = {
    val tokenizer = {
      patternString match {
        case None => new StandardTokenizer()
        case Some(s) => {
          val pattern = Pattern.compile(s)
          new PatternTokenizer(pattern, -1)
        }
      }
    }

    var result: TokenStream = new StandardFilter(tokenizer)
    if (option.ignoreCase) {
      Logger.debug("lower case filter")
      result = new LowerCaseFilter(result)
    }
    if (option.swDict.equalsIgnoreCase("Lucene")) {
      Logger.debug("Lucene stopWords filter")
      result = new StopFilter(result, StopAnalyzer.ENGLISH_STOP_WORDS_SET)
    } else if (option.swDict.equals("None")) {
      Logger.debug("empty stopWords filter")
    }
    if (option.stemming) {
      Logger.debug("porter stemming filter")
      result = new PorterStemFilter(result)
    }
    val tokenStreamComponents = new TokenStreamComponents(tokenizer, result)
    tokenStreamComponents

  }
}
