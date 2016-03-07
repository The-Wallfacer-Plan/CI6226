package ir.ui;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import ir.core.IndexWrapper;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import ir.utility.Config;

public class Indexer {

    public static void main(String[] args) {
        String inputFile = Config.xmlFile;
        IndexWrapper indexer = new IndexWrapper(inputFile);
        ArgumentParser parser = ArgumentParsers.newArgumentParser("Indexer").defaultHelp(true);
        parser.addArgument("-i", "--ignoreCase").action(Arguments.storeTrue()).help("whether ignore case");
        parser.addArgument("-t", "--stemming").action(Arguments.storeTrue()).help("whether stem postings");
        parser.addArgument("-s", "--stopWords").choices(Lists.newArrayList("None", "Lucene")).setDefault("None").help("specify stopwords dictionary");
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            System.exit(0);
        }
        Preconditions.checkNotNull(ns);
        boolean stem = ns.getBoolean("stemming");
        boolean ignoreCase = ns.getBoolean("ignoreCase");
        String stopWordsDict = ns.getString("stopWords");
        indexer.index(stem, ignoreCase, stopWordsDict);
    }
}
