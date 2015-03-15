package org.lignos.nlp.sequence;

/**
 * Copyright 2015 Constantine Lignos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.lignos.nlp.codeswitching.Constants;

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
        TokenSequenceCorpusReader reader = null;
        try {
            reader = new TokenSequenceCorpusReader(path, Constants.IGNORE_TAGS);
        } catch (IOException err) {
            System.err.println("Could not open input file: " + path);
            System.exit(1);
        }

        LinkedList<FeatureSequence> featureSequences = new LinkedList<FeatureSequence>();
        for (TokenSequence seq : reader) {
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
