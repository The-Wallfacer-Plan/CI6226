package models.core;

import models.utility.Config;
import models.xml.PubHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;

public class LIndexDriver {

    private static final Logger logger = LoggerFactory.getLogger(LIndexDriver.class);

    private String source;

    public LIndexDriver(String source) {
        logger.info("parsing {}", source);
        this.source = source;
    }

    public void run(LIndexer indexer) {
        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            SAXParser parser = parserFactory.newSAXParser();
            parser.getXMLReader().setFeature(Config.VALIDATION, true);
            File inputFile = new File(source);
            if (!inputFile.exists()) {
                System.out.printf("%s doesn't exist\n", source);
                System.exit(1);
            }
            PubHandler handler = new PubHandler(indexer);
            parser.parse(inputFile, handler);
        } catch (IOException e) {
            System.out.println("Error reading URI: " + e.getMessage());
        } catch (SAXException e) {
            System.out.println("Error in parsing: " + e.getMessage());
        } catch (ParserConfigurationException e) {
            System.out.println("Error in XML parser configuration: " + e.getMessage());
        }
    }
}
