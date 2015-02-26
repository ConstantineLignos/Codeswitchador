package org.lignos.nlp.codeswitching.features;

import org.lignos.nlp.sequence.TokenSequenceFeatureGenerator;
import org.lignos.nlp.sequence.Sequence;

import java.util.List;

/**
 * Generate suffix features
 */
public class LengthFeatureGenerator extends TokenSequenceFeatureGenerator {

    /**
     * Generate length features
     */

    @Override
    public void addTokenFeatures(Sequence seq, int index, List<String> features) {
        features.add("LENGTH:" + seq.get(index).token.length());
    }
}
