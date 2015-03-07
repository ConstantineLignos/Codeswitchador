package org.lignos.nlp.codeswitching.features;

import org.lignos.nlp.sequence.TokenSequenceFeatureGenerator;
import org.lignos.nlp.sequence.Sequence;

import java.util.LinkedList;
import java.util.List;

/**
 * Features for token context.
 */
public class TokenContextFeatureGenerator extends TokenSequenceFeatureGenerator {

    /** The relative index to extract a token from */
    private final int relIndex;

    public TokenContextFeatureGenerator(int relativeIndex) {
        this.relIndex = relativeIndex;
    }

    @Override
    public List<String> genTokenFeatures(Sequence seq, int index) {
        List<String> features = new LinkedList<String>();

        int targetIndex = index + relIndex;
        if (targetIndex >= 0 && targetIndex < seq.size()) {
            features.add("TOK" + relIndex + ":" + seq.get(targetIndex).token.toLowerCase());
        }
        return features;
    }
}
