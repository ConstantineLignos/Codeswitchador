package org.lignos.nlp.sequence;

import java.util.List;

/**
 * Generates features based on label sequences.
 */
public abstract class LabelSequenceFeatureGenerator {

    /**
     * Generate features based on labels from a position in a sequence. Instantiate
     * {@link TokenSequenceFeatureGenerator TokenSequenceFeatureGenerator} instead to generate
     * features dependent on the label history.
     *
     * @param labels the labels, which must be defined up to index - 1
     * @param index the index in the sequence to use for feature extraction
     */
    public abstract List<String> genLabelFeatures(String[] labels, int index);
}
