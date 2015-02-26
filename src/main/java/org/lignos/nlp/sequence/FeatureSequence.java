package org.lignos.nlp.sequence;

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
            labels[i] = seq.get(i).state;
        }
        List<List<String>> seqFeaturesList = featureSet.generateAllSequenceFeatures(seq, labels);
        return new FeatureSequence(seqFeaturesList, labels);
    }
}
