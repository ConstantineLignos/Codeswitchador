package org.lignos.nlp.sequence;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Read features organized into sequences from a file.
 */
public class FeatureSequenceCorpusReader implements Iterable<FeatureSequence> {

    private LinkedList<FeatureSequence> sequences;

    public FeatureSequenceCorpusReader(LinkedList<FeatureSequence> sequences) {
        this.sequences = sequences;
    }

    public FeatureSequenceCorpusReader(String path, boolean labeled) throws IOException{
        sequences = new LinkedList<FeatureSequence>();
        loadCorpus(path, labeled);
    }

    public static FeatureSequenceCorpusReader getCorpusFeatures(String path, SequenceFeatureSet featureSet) {
        SequenceCorpusReader reader = null;
        try {
            reader = new SequenceCorpusReader(path, false);
        } catch (IOException err) {
            System.err.println("Could not open input file: " + path);
            System.exit(1);
        }

        LinkedList<FeatureSequence> featureSequences = new LinkedList<FeatureSequence>();
        for (Sequence seq : reader) {
                featureSequences.add(FeatureSequence.generateFeatures(seq, featureSet));
        }
        return new FeatureSequenceCorpusReader(featureSequences);
    }

    private void loadCorpus(String path, boolean labeled) throws IOException {
        BufferedReader lineReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
        String line;
        int lineNum = 0;
        List<String> currSequence = new LinkedList<String>();
        while ((line = lineReader.readLine()) != null) {
            lineNum++;

            // If the line is empty, try to process the current sequence
            if (line.trim().isEmpty()) {
                // Skip if currSequence is empty
                if (!currSequence.isEmpty()) {
                    sequences.add(new FeatureSequence(currSequence, labeled));
                    currSequence = new LinkedList<String>();
                }
            } else {
                // Otherwise, add the line
                currSequence.add(line.trim());
            }
        }
        // Add any leftover sequences
        if (!currSequence.isEmpty()) {
            sequences.add(new FeatureSequence(currSequence, labeled));
        }

        // Clean up
        lineReader.close();
    }

    @Override
    public Iterator<FeatureSequence> iterator() {
        return sequences.iterator();
    }
}
