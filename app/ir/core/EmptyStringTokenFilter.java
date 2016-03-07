package ir.core;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;

public class EmptyStringTokenFilter extends TokenFilter {

    public EmptyStringTokenFilter(TokenStream tokenStream) {
        super(tokenStream);
    }

    protected CharTermAttribute charTermAttribute =
            addAttribute(CharTermAttribute.class);
    protected PositionIncrementAttribute positionIncrementAttribute =
            addAttribute(PositionIncrementAttribute.class);

    @Override
    public boolean incrementToken() throws IOException {

        String nextToken = null;
        while (nextToken == null) {

            if (!this.input.incrementToken()) {
                return false;
            }

            String currentTokenInStream =
                    this.input.getAttribute(CharTermAttribute.class)
                            .toString().trim();

            if (currentTokenInStream.length() > 0) {
                nextToken = currentTokenInStream;
            }
        }

        this.charTermAttribute.setEmpty().append(nextToken);
        this.positionIncrementAttribute.setPositionIncrement(1);

        return true;
    }
}