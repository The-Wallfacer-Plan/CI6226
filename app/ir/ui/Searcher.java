package ir.ui;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import ir.core.SearchWrapper;
import ir.utility.Config;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Searcher {
    private static final Logger logger = LoggerFactory.getLogger(Searcher.class);

    public static void displayResult(Map<String, ScoreDoc[]> map, IndexSearcher searcher) {
        for (Map.Entry<String, ScoreDoc[]> entry : map.entrySet()) {
            ScoreDoc[] scoreDocs = entry.getValue();
            for (ScoreDoc hit : scoreDocs) {
                Document hitDoc = null;
                try {
                    hitDoc = searcher.doc(hit.doc);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                System.out.println(Strings.repeat("=", 80));
                System.out.printf("field: %s, score: %f\n", entry.getKey(), hit.score);
                for (IndexableField field : hitDoc.getFields()) {
                    System.out.println(field.name() + "\t" + field.stringValue());
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {

        ArgumentParser parser = ArgumentParsers.newArgumentParser("Searcher").defaultHelp(true);
        parser.addArgument("-f", "--fields").nargs("+").setDefault(Config.defaultFields).help("set search fields");
        parser.addArgument("-q", "--query").required(true).help("query string");
        if (args.length == 0) {
            parser.printHelp();
            System.exit(0);
        }
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            System.exit(0);
        }
        Preconditions.checkNotNull(ns);
        List<String> fields = ns.getList("fields");
        String queryString = ns.getString("query");

        SearchWrapper wrapper = new SearchWrapper();
        Map<String, ScoreDoc[]> map = wrapper.search(fields, queryString);
        IndexSearcher searcher = wrapper.getSearcher();
        displayResult(map, searcher);
        wrapper.close();
    }
}
