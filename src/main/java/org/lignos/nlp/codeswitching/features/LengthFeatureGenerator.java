package org.lignos.nlp.codeswitching.features;

import org.lignos.nlp.sequence.TokenSequenceFeatureGenerator;
import org.lignos.nlp.sequence.Sequence;

import java.util.LinkedList;
import java.util.List;

/**
 * Generate suffix features
 */
public class LengthFeatureGenerator extends TokenSequenceFeatureGenerator {

    /**
     * Generate length features
     */

    @Override
    public List<String> genTokenFeatures(Sequence seq, int index) {
        List<String> features = new LinkedList<String>();
        features.add("LENGTH:" + seq.get(index).token.length());
        return features;
    }
}
