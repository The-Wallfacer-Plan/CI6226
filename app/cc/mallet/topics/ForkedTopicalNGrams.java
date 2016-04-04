/* Copyright (C) 2005 Univ. of Massachusetts Amherst, Computer Science Dept.
   This file is part of "MALLET" (MAchine Learning for LanguagE Toolkit).
   http://www.cs.umass.edu/~mccallum/mallet
   This software is provided under the terms of the Common Public License,
   version 1.0, as published by http://www.opensource.org.  For further
   information, see the file `LICENSE' included with this distribution. */

package cc.mallet.topics;


import cc.mallet.types.Alphabet;
import cc.mallet.types.AugmentableFeatureVector;
import cc.mallet.types.FeatureSequenceWithBigrams;
import cc.mallet.types.InstanceList;
import cc.mallet.util.Randoms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Like Latent Dirichlet Allocation, but with integrated phrase discovery.
 * Changed by @HongxuChen for LSearcher Application usage
 *
 * @author Andrew McCallum <a href="mailto:mccallum@cs.umass.edu">mccallum@cs.umass.edu</a>
 *         based on C code by Xuerui Wang.
 */

public class ForkedTopicalNGrams {

    public static int displayedNum = 3;

    class WordProb implements Comparable {
        int wi;
        double p;

        public WordProb(int wi, double p) {
            this.wi = wi;
            this.p = p;
        }

        public final int compareTo(Object o2) {
            if (p > ((WordProb) o2).p)
                return -1;
            else if (p == ((WordProb) o2).p)
                return 0;
            else return 1;
        }
    }

    public static final Logger logger = LoggerFactory.getLogger(ForkedTopicalNGrams.class);

    int numTopics;
    Alphabet uniAlphabet;
    Alphabet biAlphabet;
    double alpha, beta, gamma, delta, tAlpha, vBeta, vGamma, delta1, delta2;
    InstanceList ilist; // containing FeatureSequenceWithBigrams in the data field of each instance
    int[][] topics; // {0...T-1}, the topic index, indexed by <document index, sequence index>
    int[][] grams; // {0,1}, the bigram status, indexed by <document index, sequence index> TODO: Make this boolean?
    int numTypes; // number of unique unigrams
    int numBitypes; // number of unique bigrams
    int numTokens; // total number of word occurrences
    // "totalNgram"
    int biTokens; // total number of tokens currently generated as bigrams (only used for progress messages)
    // "docTopic"
    int[][] docTopicCounts; // indexed by <document index, topic index>
    // Used to calculate p(x|w,t).  "ngramCount"
    int[][][] typeNgramTopicCounts; // indexed by <feature index, ngram status, topic index>
    // Used to calculate p(w|t) and p(w|t,w), "topicWord" and "topicNgramWord"
    int[][] unitypeTopicCounts; // indexed by <feature index, topic index>
    int[][] bitypeTopicCounts; // index by <bifeature index, topic index>
    // "sumWords"
    int[] tokensPerTopic; // indexed by <topic index>
    // "sumNgramWords"
    int[][] bitokensPerTopic; // indexed by <feature index, topic index>, where the later is the conditioned word

    public ForkedTopicalNGrams(int numberOfTopics) {
        this(numberOfTopics, 50.0, 0.01, 0.01, 0.03, 0.2, 1000);
    }

    public ForkedTopicalNGrams(int numberOfTopics, double alphaSum, double beta, double gamma, double delta,
                               double delta1, double delta2) {
        this.numTopics = numberOfTopics;
        this.alpha = alphaSum / numTopics; // smoothing over the choice of topic
        this.beta = beta;                  // smoothing over the choice of unigram words
        this.gamma = gamma;                // smoothing over the choice of bigram words
        this.delta = delta;                // smoothing over the choice of unigram/bigram generation
        this.delta1 = delta1;   // TODO: Clean this up.
        this.delta2 = delta2;
        logger.info("alpha :" + alphaSum);
        logger.info("beta :" + beta);
        logger.info("gamma :" + gamma);
        logger.info("delta :" + delta);
        logger.info("delta1 :" + delta1);
        logger.info("delta2 :" + delta2);
    }

    public List<StringPair> estimate(InstanceList documents, int numIterations, int showTopicsInterval, Randoms r) {
        ilist = documents;
        uniAlphabet = ilist.getDataAlphabet();
        biAlphabet = ((FeatureSequenceWithBigrams) ilist.get(0).getData()).getBiAlphabet();
        numTypes = uniAlphabet.size();
        numBitypes = biAlphabet.size();
        int numDocs = ilist.size();
        topics = new int[numDocs][];
        grams = new int[numDocs][];
        docTopicCounts = new int[numDocs][numTopics];
        typeNgramTopicCounts = new int[numTypes][2][numTopics];
        unitypeTopicCounts = new int[numTypes][numTopics];
        bitypeTopicCounts = new int[numBitypes][numTopics];
        tokensPerTopic = new int[numTopics];
        bitokensPerTopic = new int[numTypes][numTopics];
        tAlpha = alpha * numTopics;
        vBeta = beta * numTypes;
        vGamma = gamma * numTypes;

        // Initialize with random assignments of tokens to topics
        // and finish allocating this.topics and this.tokens
        int topic, gram, seqLen, fi;
        for (int di = 0; di < numDocs; di++) {
            FeatureSequenceWithBigrams fs = (FeatureSequenceWithBigrams) ilist.get(di).getData();
            seqLen = fs.getLength();
            numTokens += seqLen;
            topics[di] = new int[seqLen];
            grams[di] = new int[seqLen];
            // Randomly assign tokens to topics
            int prevFi = -1, prevTopic = -1;
            for (int si = 0; si < seqLen; si++) {
                // randomly sample a topic for the word at position si
                topic = r.nextInt(numTopics);
                // if a bigram is allowed at position si, then sample a gram status for it.
                gram = (fs.getBiIndexAtPosition(si) == -1 ? 0 : r.nextInt(2));
                if (gram != 0) biTokens++;
                topics[di][si] = topic;
                grams[di][si] = gram;
                docTopicCounts[di][topic]++;
                fi = fs.getIndexAtPosition(si);
                if (prevFi != -1)
                    typeNgramTopicCounts[prevFi][gram][prevTopic]++;
                if (gram == 0) {
                    unitypeTopicCounts[fi][topic]++;
                    tokensPerTopic[topic]++;
                } else {
                    bitypeTopicCounts[fs.getBiIndexAtPosition(si)][topic]++;
                    bitokensPerTopic[prevFi][topic]++;
                }
                prevFi = fi;
                prevTopic = topic;
            }
        }

        for (int iteration = 0; iteration < numIterations; iteration++) {
            long iterNow = System.currentTimeMillis();
            sampleTopicsForAllDocs(r);
            long iterDuration = System.currentTimeMillis() - iterNow;
            logger.info("iteration {} costs {}ms", iteration, iterDuration);
        }
        return getTopicSamples(displayedNum);
    }

    // One iteration of Gibbs sampling, across all documents.
    private void sampleTopicsForAllDocs(Randoms r) {
        double[] uniTopicWeights = new double[numTopics];
        double[] biTopicWeights = new double[numTopics * 2];
        // Loop over every word in the corpus
        for (int di = 0; di < topics.length; di++) {
            sampleTopicsForOneDoc((FeatureSequenceWithBigrams) ilist.get(di).getData(),
                    topics[di], grams[di], docTopicCounts[di],
                    uniTopicWeights, biTopicWeights, r);
        }
    }


    private void sampleTopicsForOneDoc(FeatureSequenceWithBigrams oneDocTokens,
                                       int[] oneDocTopics, int[] oneDocGrams,
                                       int[] oneDocTopicCounts, // indexed by topic index
                                       double[] uniTopicWeights, // length==numTopics
                                       double[] biTopicWeights, // length==numTopics*2: joint topic/gram sampling
                                       Randoms r) {
        int[] currentTypeTopicCounts;
        int[] currentBitypeTopicCounts;
        int[] previousBitokensPerTopic;
        int type, bitype, oldGram, nextGram, newGram, oldTopic, newTopic;
        double topicWeightsSum, tw;
        int docLen = oneDocTokens.getLength();
        // Iterate over the positions (words) in the document
        for (int si = 0; si < docLen; si++) {
            type = oneDocTokens.getIndexAtPosition(si);
            bitype = oneDocTokens.getBiIndexAtPosition(si);
            oldTopic = oneDocTopics[si];
            oldGram = oneDocGrams[si];
            nextGram = (si == docLen - 1) ? -1 : oneDocGrams[si + 1];
            boolean bigramPossible = (bitype != -1);
            assert (!(!bigramPossible && oldGram == 1));
            if (!bigramPossible) {
                // Remove this token from all counts
                oneDocTopicCounts[oldTopic]--;
                tokensPerTopic[oldTopic]--;
                unitypeTopicCounts[type][oldTopic]--;
                if (si != docLen - 1) {
                    typeNgramTopicCounts[type][nextGram][oldTopic]--;
                    assert (typeNgramTopicCounts[type][nextGram][oldTopic] >= 0);
                }
                assert (oneDocTopicCounts[oldTopic] >= 0);
                assert (tokensPerTopic[oldTopic] >= 0);
                assert (unitypeTopicCounts[type][oldTopic] >= 0);
                // Build a distribution over topics for this token
                Arrays.fill(uniTopicWeights, 0.0);
                topicWeightsSum = 0;
                currentTypeTopicCounts = unitypeTopicCounts[type];
                for (int ti = 0; ti < numTopics; ti++) {
                    tw = ((currentTypeTopicCounts[ti] + beta) / (tokensPerTopic[ti] + vBeta))
                            * ((oneDocTopicCounts[ti] + alpha)); // additional term is constance across all topics
                    topicWeightsSum += tw;
                    uniTopicWeights[ti] = tw;
                }
                // Sample a topic assignment from this distribution
                newTopic = r.nextDiscrete(uniTopicWeights, topicWeightsSum);
                // Put that new topic into the counts
                oneDocTopics[si] = newTopic;
                oneDocTopicCounts[newTopic]++;
                unitypeTopicCounts[type][newTopic]++;
                tokensPerTopic[newTopic]++;
                if (si != docLen - 1)
                    typeNgramTopicCounts[type][nextGram][newTopic]++;
            } else {
                // Bigram is possible
                int prevType = oneDocTokens.getIndexAtPosition(si - 1);
                int prevTopic = oneDocTopics[si - 1];
                // Remove this token from all counts
                oneDocTopicCounts[oldTopic]--;
                typeNgramTopicCounts[prevType][oldGram][prevTopic]--;
                if (si != docLen - 1)
                    typeNgramTopicCounts[type][nextGram][oldTopic]--;
                if (oldGram == 0) {
                    unitypeTopicCounts[type][oldTopic]--;
                    tokensPerTopic[oldTopic]--;
                } else {
                    bitypeTopicCounts[bitype][oldTopic]--;
                    bitokensPerTopic[prevType][oldTopic]--;
                    biTokens--;
                }
                assert (oneDocTopicCounts[oldTopic] >= 0);
                assert (typeNgramTopicCounts[prevType][oldGram][prevTopic] >= 0);
                assert (si == docLen - 1 || typeNgramTopicCounts[type][nextGram][oldTopic] >= 0);
                assert (unitypeTopicCounts[type][oldTopic] >= 0);
                assert (tokensPerTopic[oldTopic] >= 0);
                assert (bitypeTopicCounts[bitype][oldTopic] >= 0);
                assert (bitokensPerTopic[prevType][oldTopic] >= 0);
                assert (biTokens >= 0);
                // Build a joint distribution over topics and ngram-status for this token
                Arrays.fill(biTopicWeights, 0.0);
                topicWeightsSum = 0;
                currentTypeTopicCounts = unitypeTopicCounts[type];
                currentBitypeTopicCounts = bitypeTopicCounts[bitype];
                previousBitokensPerTopic = bitokensPerTopic[prevType];
                for (int ti = 0; ti < numTopics; ti++) {
                    newTopic = ti << 1; // just using this variable as an index into [ti*2+gram]
                    // The unigram outcome
                    tw = (currentTypeTopicCounts[ti] + beta) / (tokensPerTopic[ti] + vBeta)
                            * (oneDocTopicCounts[ti] + alpha)
                            * (typeNgramTopicCounts[prevType][0][prevTopic] + delta1);
                    topicWeightsSum += tw;
                    biTopicWeights[newTopic] = tw;
                    // The bigram outcome
                    newTopic++;
                    tw = (currentBitypeTopicCounts[ti] + gamma) / (previousBitokensPerTopic[ti] + vGamma)
                            * (oneDocTopicCounts[ti] + alpha)
                            * (typeNgramTopicCounts[prevType][1][prevTopic] + delta2);
                    topicWeightsSum += tw;
                    biTopicWeights[newTopic] = tw;
                }
                // Sample a topic assignment from this distribution
                newTopic = r.nextDiscrete(biTopicWeights, topicWeightsSum);
                // Put that new topic into the counts
                newGram = newTopic % 2;
                newTopic /= 2;
                // Put that new topic into the counts
                oneDocTopics[si] = newTopic;
                oneDocGrams[si] = newGram;
                oneDocTopicCounts[newTopic]++;
                typeNgramTopicCounts[prevType][newGram][prevTopic]++;
                if (si != docLen - 1)
                    typeNgramTopicCounts[type][nextGram][newTopic]++;
                if (newGram == 0) {
                    unitypeTopicCounts[type][newTopic]++;
                    tokensPerTopic[newTopic]++;
                } else {
                    bitypeTopicCounts[bitype][newTopic]++;
                    bitokensPerTopic[prevType][newTopic]++;
                    biTokens++;
                }
            }
        }
    }

    private List<StringPair> getTopicSamples(int numWords) {

        List<StringPair> lists = new ArrayList<>();
        for (int ti = 0; ti < numTopics; ti++) {
            String s1, s2;
            // Unigrams
            {
                WordProb[] wp = new WordProb[numTypes];
                for (int wi = 0; wi < numTypes; wi++)
                    wp[wi] = new WordProb(wi, (double) unitypeTopicCounts[wi][ti]);
                Arrays.sort(wp);
                int numToPrint = Math.min(wp.length, numWords);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < numToPrint; i++) {
                    Object o = uniAlphabet.lookupObject(wp[i].wi);
                    sb.append(o.toString()).append(" ");
                }
                s1 = sb.toString();
            }

            // Ngrams
            {
                AugmentableFeatureVector afv = new AugmentableFeatureVector(new Alphabet(), 10000, false);
                for (int di = 0; di < topics.length; di++) {
                    FeatureSequenceWithBigrams fs = (FeatureSequenceWithBigrams) ilist.get(di).getData();
                    for (int si = topics[di].length - 1; si >= 0; si--) {
                        if (topics[di][si] == ti && grams[di][si] == 1) {
                            String gramString = uniAlphabet.lookupObject(fs.getIndexAtPosition(si)).toString();
                            while (grams[di][si] == 1 && --si >= 0)
                                gramString = uniAlphabet.lookupObject(fs.getIndexAtPosition(si)).toString() + "_" + gramString;
                            afv.add(gramString, 1.0);
                        }
                    }
                }
                int numNgrams = afv.numLocations();
                WordProb[] wp = new WordProb[numNgrams];
                for (int loc = 0; loc < numNgrams; loc++) {
                    wp[loc] = new WordProb(afv.indexAtLocation(loc), afv.valueAtLocation(loc));
                }
                Arrays.sort(wp);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < Math.min(numNgrams, numWords); i++) {
                    Object alphabet = afv.getAlphabet().lookupObject(wp[i].wi);
                    sb.append(alphabet.toString()).append(" ");
                }
                s2 = sb.toString();
            }
            lists.add(new StringPair(s1, s2));
        }
        return lists;
    }

}
