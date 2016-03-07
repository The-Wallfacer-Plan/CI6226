package ir.core;

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

public class IRAnalyzer extends Analyzer {
    private static final Logger logger = LoggerFactory.getLogger(IRAnalyzer.class);

    private boolean stemming;
    private boolean lowering;
    private String stopWordsDict;
    private String patternString;

    public IRAnalyzer(boolean lowering, boolean stemming, String stopWordsDict, String patternString) {
        this.lowering = lowering;
        this.stemming = stemming;
        this.stopWordsDict = stopWordsDict;
        this.patternString = patternString;
    }

    public IRAnalyzer() {
        this.lowering = false;
        this.stemming = false;
        this.stopWordsDict = "None";
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
        if (lowering) {
            logger.info("lower case filter");
            result = new LowerCaseFilter(result);
        }
        if (stopWordsDict.equals("Lucene")) {
            logger.info("Lucene stopWords filter");
            result = new StopFilter(result, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
        } else if (stopWordsDict.equals("None")) {
            logger.info("empty stopWords filter");
        }
        if (stemming) {
            logger.info("stemming filter");
            result = new PorterStemFilter(result);
        }
        return new TokenStreamComponents(tokenizer, result);
    }
}
