package org.lignos.nlp.codeswitching.features;

import org.lignos.nlp.sequence.LabelSequenceFeatureGenerator;

import java.util.LinkedList;
import java.util.List;

/**
 * Features that use a single tag in the history.
 */
public class TagContextFeatureGenerator extends LabelSequenceFeatureGenerator {

    /** The relative index to extract a token from */
    private final int relIndex;

    /**
     * Create a generator that produces a feature for a single tag in the history
     *
     * @param relativeIndex the relative index for the position in the history, which must be negative
     */
    public TagContextFeatureGenerator(int relativeIndex) {
        if (relativeIndex >= 0) {
            throw new IllegalArgumentException("Relative index must be less than zero");
        }
        this.relIndex = relativeIndex;
    }

    @Override
    public List<String> genLabelFeatures(String[] labels, int index) {
        List<String> features = new LinkedList<String>();

        // If labels is null, skip
        if (labels == null) {
            return features;
        }

        // Compute and check the target index
        int targetIndex = index + relIndex;
        if (targetIndex >= 0 && targetIndex < labels.length) {
            String tag = labels[targetIndex];
            if (tag == null) {
                throw new IllegalArgumentException("Target label is null");
            }
            features.add("TAG" + relIndex + ":" + labels[targetIndex]);
        }
        return features;
    }
}
