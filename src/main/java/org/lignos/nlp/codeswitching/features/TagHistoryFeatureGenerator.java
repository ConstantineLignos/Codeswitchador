package org.lignos.nlp.codeswitching.features;

import org.lignos.nlp.sequence.LabelSequenceFeatureGenerator;
import org.lignos.nlp.util.StringUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Features for the previous n tags, all tags together.
 */
public class TagHistoryFeatureGenerator extends LabelSequenceFeatureGenerator {

    /** The relative index to extract a the series of tokens from */
    private final int historySize;

    /**
     * Create a generator that produces a single feature for the last historySize tags
     *
     * @param historySize the number of tags to create a history feature for, e.g., 2 = trigram model
     */
    public TagHistoryFeatureGenerator(int historySize) {
        if (historySize < 1) {
            throw new IllegalArgumentException("Relative index must be 1 or larger");
        }
        this.historySize = historySize;
    }

    @Override
    public void addLabelFeatures(String[] labels, int index, List<String> features) {
        // If labels is null, skip
        if (labels == null) {
            return;
        }
        // Compute and check the target index
        int firstIndex = index - historySize;
        if (firstIndex >= 0 && firstIndex < labels.length) {
            String tags = StringUtil.join(Arrays.copyOfRange(labels, firstIndex, index), ",");
            features.add("TAGHIST" + historySize + ":" + tags);
        }
    }
}
