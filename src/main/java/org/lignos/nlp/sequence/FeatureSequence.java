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

import java.util.LinkedList;
import java.util.List;

/**
 * A sequence of events with a feature representation.
 */
public class FeatureSequence {

    private List<List<String>> features;
    private String[] labels;

    /** Create a feature sequence from a list of lines.
     *
     * @param lines representation of the sequence of features
     * @param labeled whether the line contains labels
     */
    public FeatureSequence(List<String> lines, boolean labeled) {
        // Initialize
        features = new LinkedList<List<String>>();
        labels = labeled ? new String[lines.size()] : null;

        // Process each line
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            List<String> lineFeatures = new LinkedList<String>();
            features.add(lineFeatures);
            String[] splits = line.trim().split("\\s+");

            // Extract the label if needed
            if (labeled) {
                labels[i] = splits[splits.length - 1];
            }

            // Add each feature. If we have labels, skip the last
            // item as it's the label.
            for (int j = 0; j < (labeled ? splits.length - 1 : splits.length); j++) {
                lineFeatures.add(splits[j]);
            }
        }
    }

    public FeatureSequence(List<List<String>> features, String[] labels) {
        this.features = features;
        this.labels = labels;
    }

    public List<List<String>> getFeatures() {
        return features;
    }

    public String[] getLabels() {
        return labels;
    }

    public int size() {
        return labels.length;
    }

    /**
     * Generate a feature sequence from a sequence
     *
     * @param featureSet feature set to use
     * @return the feature sequence
     */
    public static FeatureSequence generateFeatures(Sequence seq, SequenceFeatureSet featureSet) {
        // Accumulate labels
        String[] labels = new String[seq.size()];
        for (int i = 0; i < seq.size(); i++) {
            labels[i] = seq.get(i).tag;
        }
        List<List<String>> seqFeaturesList = featureSet.generateAllSequenceFeatures(seq, labels);
        return new FeatureSequence(seqFeaturesList, labels);
    }
}
