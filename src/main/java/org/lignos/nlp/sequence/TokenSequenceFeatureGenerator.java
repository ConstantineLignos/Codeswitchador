package org.lignos.nlp.sequence;

import java.util.List;

/**
 * Generates features based on token sequences using only the token themselves.
 */
public abstract class TokenSequenceFeatureGenerator {

    /**
     * Generate features based on tokens from a position in a sequence. For efficiency, the feature
     * list is modified in-place. While the tokens in the sequence may have labels, these labels
     * are not to be used for feature generation.  Instantiate {@link LabelSequenceFeatureGenerator
     * LabelSequenceFeatureGenerator} instead to generate features dependent on the label history.
     *
     * @param seq the input sequence
     * @param index the index in the sequence to use for feature extraction
     * @param features a list of features to add to
     */
    public abstract void addTokenFeatures(Sequence seq, int index, List<String> features);
}
