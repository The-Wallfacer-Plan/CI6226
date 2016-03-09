package models.core;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.pattern.PatternTokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class LAnalyzer extends Analyzer {
    private static final Logger logger = LoggerFactory.getLogger(LAnalyzer.class);

    private String patternString;
    private LIndexOption option;

    public LAnalyzer(LIndexOption option, String patternString) {
        this.option = option;
        this.patternString = patternString;
    }

    public LAnalyzer() {
        this.option = new LIndexOption(false, false, "None");
        this.patternString = null;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {

        Tokenizer tokenizer;
        logger.info("fieldName={}, patternString=\"{}\"", fieldName, patternString);
        if (patternString == null) {
            tokenizer = new StandardTokenizer();
        } else {
            Pattern pattern = Pattern.compile(patternString);
            tokenizer = new PatternTokenizer(pattern, -1);
        }
        TokenStream result = new EmptyStringTokenFilter(tokenizer);
        if (option.ignoreCase) {
            logger.info("lower case filter");
            result = new LowerCaseFilter(result);
        }
        if (option.swDict.equals("Lucene")) {
            logger.info("Lucene stopWords filter");
            result = new StopFilter(result, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
        } else if (option.swDict.equals("None")) {
            logger.info("empty stopWords filter");
        }
        if (option.stemming) {
            logger.info("stemming filter");
            result = new PorterStemFilter(result);
        }
        return new TokenStreamComponents(tokenizer, result);
    }
}
