package models.core;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import models.utility.Config;
import models.xml.Publication;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.AnalyzerWrapper;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

public class LIndexer {

    private static final Logger logger = LoggerFactory.getLogger(LIndexer.class);

    private AnalyzerWrapper analyzerWrapper;

    public LIndexer(LIndexOption option) throws IOException {
        Analyzer analyzer = new LAnalyzer(option, null);
        Analyzer listAnalyzer = new LAnalyzer(option, Config.splitString);
        Map<String, Analyzer> analyzerMap = Maps.newHashMap();
        analyzerMap.put(Config.I_AUTHORS, listAnalyzer);
        analyzerWrapper = new PerFieldAnalyzerWrapper(analyzer, analyzerMap);
    }

    private void addDocText(String key, String value, Document document) {
        Field field = new TextField(key, value, Field.Store.YES);
        document.add(field);
    }

    public void index(Publication publication) {
        logger.info("indexing" + publication);
        Directory dir;
        try {
            dir = FSDirectory.open(Paths.get(Config.indexFolder));
            IndexWriterConfig iwc = new IndexWriterConfig(analyzerWrapper);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            IndexWriter writer = new IndexWriter(dir, iwc);
            indexPublication(writer, publication);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

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
}
