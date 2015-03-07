package org.lignos.nlp.codeswitching.features;

import org.lignos.nlp.sequence.TokenSequenceFeatureGenerator;
import org.lignos.nlp.sequence.Sequence;

import java.util.LinkedList;
import java.util.List;

/**
 * Generate suffix features
 */
public class SuffixFeatureGenerator extends TokenSequenceFeatureGenerator {

    private final int minSuffixLength;
    private final int maxSuffixLength;
    private final int minStemLength;

    /**
     * Generate suffix features
     *
     * @param minSuffixLength the minimum suffix length
     * @param maxSuffixLength the maximum suffix length
     * @param minStemLength the minimum length of the stem after suffixes are removed
     */
    public SuffixFeatureGenerator(int minSuffixLength, int maxSuffixLength, int minStemLength) {
        this.minSuffixLength = minSuffixLength;
        this.maxSuffixLength = maxSuffixLength;
        this.minStemLength = minStemLength;
    }

    @Override
    public List<String> genTokenFeatures(Sequence seq, int index) {
        List<String> features = new LinkedList<String>();
        String token = seq.get(index).token;
        for (int i = minSuffixLength; i <= maxSuffixLength; i++) {
            int startIdx = token.length() - i;
            // Stop if we would cut into the stem
            if (startIdx <= (minStemLength - 1)) {
                break;
            }
            else {
                features.add("SUFFIX:" + token.toLowerCase().substring(startIdx, token.length()));
            }
        }
        return features;
    }
}
