package models.core;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import models.utility.Config;
import models.xml.PubHandler;
import models.xml.Publication;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class IndexWrapper {

    private static final Logger logger = LoggerFactory.getLogger(IndexWrapper.class);

    List<Publication> publicationList;

    public IndexWrapper(String uri) {
        logger.info("parsing");
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            SAXParser parser = parserFactory.newSAXParser();
            PubHandler handler = new PubHandler();
            parser.getXMLReader().setFeature(Config.VALIDATION, true);
            File inputFile = new File(uri);
            if (!inputFile.exists()) {
                System.out.printf("%s doesn't exist\n", uri);
                System.exit(1);
            }
            parser.parse(inputFile, handler);
            publicationList = handler.getPublicationList();
        } catch (IOException e) {
            System.out.println("Error reading URI: " + e.getMessage());
        } catch (SAXException e) {
            System.out.println("Error in parsing: " + e.getMessage());
        } catch (ParserConfigurationException e) {
            System.out.println("Error in XML parser configuration: " + e.getMessage());
        }
    }

    private void addDocText(String key, String value, Document document) {
        Field field = new TextField(key, value, Field.Store.YES);
        document.add(field);
    }

    private void addDocString(String key, String value, Document document) {
        Field field = new StringField(key, value, Field.Store.YES);
        document.add(field);
    }

    public void indexPublication(IndexWriter writer, Publication publication) {
        Document document = new Document();
//        is the form of "xxx/xxx/xxx", use TextField
        addDocText("paperId", publication.getPaperId(), document);
//        TextField
        addDocText("title", publication.getTitle(), document);
//        StringField
        addDocText("kind", String.valueOf(publication.getKind()), document);
//        ???
        addDocText("venue", publication.getVenue(), document);
//        StringField
        addDocText("pubYear", publication.getPubYear(), document);
//        TextField (how to join/split author list ???)
        String authorString = Joiner.on(Config.splitString).join(publication.getAuthors());
        addDocText("authors", authorString, document);
        try {
            if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                writer.addDocument(document);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    public void index(boolean stemming, boolean ignoreCase, String stopWordDict) {
        logger.info("indexing");
        try {
            Directory dir = FSDirectory.open(Paths.get(Config.indexFold));
            Analyzer analyzer = new IRAnalyzer(stemming, ignoreCase, stopWordDict, null);
            Analyzer listAnalyzer = new IRAnalyzer(stemming, ignoreCase, stopWordDict, Config.splitString);
            Map<String, Analyzer> analyzerMap = Maps.newHashMap();
            analyzerMap.put(Config.I_AUTHORS, listAnalyzer);
            PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(analyzer, analyzerMap);
            IndexWriterConfig iwc = new IndexWriterConfig(wrapper);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            IndexWriter writer = new IndexWriter(dir, iwc);
            for (Publication publication : publicationList) {
                indexPublication(writer, publication);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}


