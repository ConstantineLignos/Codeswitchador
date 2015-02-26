package org.lignos.nlp.sequence;

import java.util.List;

/**
 * Generates features based on label sequences.
 */
public abstract class LabelSequenceFeatureGenerator {

    /**
     * Generate features based on labels from a position in a sequence. For efficiency, the feature
     * list is modified in-place. Instantiate {@link TokenSequenceFeatureGenerator
     * TokenSequenceFeatureGenerator} instead to generate features dependent on the label history.
     *
     * @param labels the labels, which must be defined up to index - 1
     * @param index the index in the sequence to use for feature extraction
     * @param features a list of features to add to
     */
    public abstract void addLabelFeatures(String[] labels, int index, List<String> features);
}
