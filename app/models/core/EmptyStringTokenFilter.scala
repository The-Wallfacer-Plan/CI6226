package models.core

import org.apache.lucene.analysis.tokenattributes.{CharTermAttribute, PositionIncrementAttribute}
import org.apache.lucene.analysis.{TokenFilter, TokenStream}

class EmptyStringTokenFilter(tokenStream: TokenStream) extends TokenFilter(tokenStream) {

  protected val charTermAttribute = addAttribute(classOf[CharTermAttribute])
  protected val positionIncrementAttribute = addAttribute(classOf[PositionIncrementAttribute])

  override def incrementToken(): Boolean = {
    var nextToken: String = null
    while (nextToken == null) {
      if (!input.incrementToken()) {
        return false
      }
      val currentTokenInStream = input.getAttribute(classOf[CharTermAttribute]).toString.trim
      if (currentTokenInStream.length > 0) {
        nextToken = currentTokenInStream
      }
    }
    charTermAttribute.setEmpty().append(nextToken)
    positionIncrementAttribute.setPositionIncrement(1)
    true
  }
}
