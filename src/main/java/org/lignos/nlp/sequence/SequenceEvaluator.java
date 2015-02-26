package org.lignos.nlp.sequence;

import gnu.trove.map.hash.THashMap;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Evaluate a labeled sequence for accuracy.
 */
public class SequenceEvaluator {

    // Data to compare
    private SequenceCorpusReader goldReader;
    private SequenceCorpusReader predReader;
    private final SequenceCorpusReader trainReader;

    // Overall hit/misses
    private int hits;
    private int misses;
    private int oov;
    private int total;
    private float accuracy;
    private float oovRate;

    // Per-label
    private Map<String, Integer> labelHits;
    private Map<String, Integer> labelMisses;
    private Map<String, Integer> labelCounts;
    private Map<String, Float> labelAccuracy;
    // Vocabulary counts for computing OOV
    private Map<String, Integer> trainCounts;

    /**
     * Create a new evaluator using the specified labels.
     * @param goldReader reader for correct labels
     * @param predReader reader for incorrect labels
     * @param trainReader reader for training data, which may be null if OOV counts are not needed
     */
    public SequenceEvaluator(SequenceCorpusReader goldReader, SequenceCorpusReader predReader,
                             SequenceCorpusReader trainReader) {
        this.goldReader = goldReader;
        this.predReader = predReader;
        this.trainReader = trainReader;
        if (goldReader.size() != predReader.size()) {
            throw new RuntimeException("Gold and predicted corpora are not the same size");
        }

        // Initialize any counts
        labelHits = new THashMap<String, Integer>();
        labelMisses = new THashMap<String, Integer>();
        labelCounts = new THashMap<String, Integer>();
        labelAccuracy = new THashMap<String, Float>();
        trainCounts = new THashMap<String, Integer>();
    }

    public static SequenceEvaluator getFromPaths(String goldPath, String predPath, String trainPath) {
        SequenceCorpusReader goldReader = null;
        try {
            goldReader = new SequenceCorpusReader(goldPath, false);
        } catch (IOException e) {
            System.err.println("Could not open input file: " + goldPath);
            System.exit(1);
        }
        SequenceCorpusReader predReader = null;
        try {
            predReader = new SequenceCorpusReader(predPath, false);
        } catch (IOException e) {
            System.err.println("Could not open input file: " + predPath);
            System.exit(1);
        }
        SequenceCorpusReader trainReader = null;
        if (trainPath != null) {
            try {
                trainReader = new SequenceCorpusReader(trainPath, false);
            } catch (IOException e) {
                System.err.println("Could not open input file: " + trainPath);
                System.exit(1);
            }
        }

        return new SequenceEvaluator(goldReader, predReader, trainReader);
    }

    /**
     * Evaluate the predictions.
     * @param ignoreComment whether to ignore tokens with the comment field set
     */
    public void eval(boolean ignoreComment) {
        // Do a first pass to build the vocabulary. We lowercase on the way in
        if (trainReader != null) {
            for (Sequence seq : trainReader) {
                for (TokenState token : seq) {
                    String tokenLower = token.token.toLowerCase();
                    trainCounts.put(tokenLower, trainCounts.getOrDefault(tokenLower, 0) + 1);
                }
            }
        }

        // We verified on construction that the two corpora on the same length so we can assume that throughout.
        Iterator<Sequence> goldIter = goldReader.iterator();
        Iterator<Sequence> predIter = predReader.iterator();
        int seqCount = 0;
        while (goldIter.hasNext()) {
            // Get next sequences
            Sequence goldSeq = goldIter.next();
            Sequence predSeq = predIter.next();
            seqCount++;

            // Verify that the lengths match
            if (goldSeq.size() != predSeq.size()) {
                System.err.println("Error: sequences not same length on line " + seqCount);
                continue;
            }

            // Compare each token/tag
            for (int i = 0; i < goldSeq.size(); i++) {
                TokenState gold = goldSeq.get(i);
                TokenState pred = predSeq.get(i);
                // Skip token if needed
                if (ignoreComment && gold.comment != null) {
                    continue;
                }

                // Count OOV
                if (!trainCounts.containsKey(gold.token)) {
                    oov++;
                }

                // Overall accuracy
                if (gold.state.equals(pred.state)) {
                    hits++;
                    labelHits.put(gold.state, labelHits.getOrDefault(gold.state, 0) + 1);
                } else {
                    misses++;
                    labelMisses.put(gold.state, labelMisses.getOrDefault(gold.state, 0) + 1);
                }
            }
        }
        // Update accuracy
        total = hits + misses;
        accuracy = (float) hits / total;
        oovRate = (float) oov /total;
        // TODO: In theory this could miss a class if it had only misses and no hits
        for(String label : labelHits.keySet()) {
            int labelCount = labelHits.get(label) + labelMisses.get(label);
            labelCounts.put(label, labelCount);
            labelAccuracy.put(label, (float) labelHits.get(label) / labelCount);
        }
    }

    /** Print the results of evaluation */
    public void printResults() {
        System.out.println("Accuracy: " + accuracy + " (" + hits + "/" + total + ")");
        System.out.println();
        System.out.println("Class accuracies:");
        for (String label : labelAccuracy.keySet()) {
            System.out.println(label + ": " + labelAccuracy.get(label));
        }
        System.out.println();
        System.out.println("Data balance:");
        for (String label : labelAccuracy.keySet()) {
            int labelCount = labelCounts.get(label);
            System.out.println(label + ": " + (float) labelCount / total + " (" + labelCount + "/" + total + ")");
        }
    }

    public int getHits() {
        return hits;
    }

    public int getMisses() {
        return misses;
    }

    public int getTotal() {
        return total;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public float getOovRate() {
        return oovRate;
    }
}
