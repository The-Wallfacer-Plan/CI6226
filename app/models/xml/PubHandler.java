package models.xml;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.List;
import java.util.Set;

public class PubHandler extends DefaultHandler {

    List<Publication> publicationList = Lists.newArrayList();

    private static final Logger logger = LoggerFactory.getLogger(PubHandler.class);
    public static final String ARTICLE = "article";
    public static final String INPROCEEDINGS = "inproceedings";
    public static final String PROCEEDINGS = "proceedings";
    public static final String JOURNAL = "journal";
    public static final String BOOKTITLE = "booktitle";

    boolean inEntry = false;

    boolean isTitle = false;
    boolean isAuthor = false;
    boolean isYear = false;
    boolean isVenue = false;
    boolean unknown = false;

    Publication pub = null;


    public List<Publication> getPublicationList() {
        return publicationList;
    }

    private boolean isInterestingEntry(String qName) {
        Set<String> interested = Sets.newHashSet(ARTICLE, INPROCEEDINGS);
        return interested.contains(qName);
    }

    private boolean isVenueEntry(String qName) {
        Set<String> venues = Sets.newHashSet(JOURNAL, BOOKTITLE);
        return venues.contains(qName);
    }


    public void startElement(String namespaceURI, String localName, String qName, Attributes attributes) throws SAXException {
        unknown = false;
        if (isInterestingEntry(qName)) {
            Preconditions.checkArgument(!inEntry);
            inEntry = true;
            Preconditions.checkArgument(pub == null);
            pub = new Publication();
            if (qName.equalsIgnoreCase(ARTICLE)) {
                pub.setKind(Publication.Kind.ARTICLE);
            } else if (qName.equalsIgnoreCase(INPROCEEDINGS)) {
                pub.setKind(Publication.Kind.INPROCEEDINGS);
            }
            String key = attributes.getValue("key");
            Preconditions.checkNotNull(key, "key shouldn't be null");
            pub.setPaperId(key);
        } else if (inEntry) {
            if (qName.equalsIgnoreCase("author")) {
                isAuthor = true;
            } else if (qName.equalsIgnoreCase("title")) {
                isTitle = true;
            } else if (qName.equalsIgnoreCase("year")) {
                isYear = true;
            }
            if (isVenueEntry(qName)) {
                if (qName.equalsIgnoreCase(JOURNAL)) {
                    Preconditions.checkArgument(pub.getKind().equals(Publication.Kind.ARTICLE));
                    isVenue = true;
                } else if (qName.equalsIgnoreCase(BOOKTITLE)) {
                    Publication.Kind kind = pub.getKind();
                    Preconditions.checkArgument(kind.equals(Publication.Kind.ARTICLE) || kind.equals(Publication.Kind.INPROCEEDINGS));
                    isVenue = true;
                }
            } else {
                unknown = true;
            }
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if (isInterestingEntry(qName)) {
            if (inEntry) {
                inEntry = false;
                isTitle = false;
                isAuthor = false;
                isYear = false;
                isVenue = false;
                unknown = false;
                publicationList.add(pub);
                pub = null;
            }
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
//        other venues might be not interesting
        if (inEntry) {
            String value = new String(ch, start, length);
            if (isTitle) {
                pub.setTitle(value);
                isTitle = false;
            }
            if (isAuthor) {
                pub.addAuthor(value);
                isAuthor = false;
            }
            if (isYear) {
                Integer year = Ints.tryParse(value);
                Preconditions.checkNotNull(year);
                pub.setPubYear(value);
                isYear = false;
            }
            if (isVenue) {
                pub.setVenue(value);
                isVenue = false;
            }
            if (unknown) {
//                System.out.println("unknown: " + value);
                unknown = false;
            }
        }
    }

    private void Message(String mode, SAXParseException exception) {
        System.out.println(mode + " Line: " + exception.getLineNumber()
                + " URI: " + exception.getSystemId() + "\n" + " Message: " + exception.getMessage());
    }

    public void warning(SAXParseException exception) throws SAXException {
        Message("**Parsing Warning**\n", exception);
        throw new SAXException("Warning encountered");
    }

    public void error(SAXParseException exception) throws SAXException {
        Message("**Parsing Error**\n", exception);
        throw new SAXException("Error encountered");
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        Message("**Parsing Fatal Error**\n", exception);
        throw new SAXException("Fatal Error encountered");
    }
}
