package org.lignos.nlp.codeswitching.features;

import gnu.trove.set.hash.THashSet;
import org.lignos.nlp.sequence.TokenSequenceFeatureGenerator;
import org.lignos.nlp.sequence.Sequence;

import java.util.List;
import java.util.Set;

/**
 * Generate character n-gram features
 */
public class CharacterFeatureGenerator extends TokenSequenceFeatureGenerator {

    // TODO: Cache features

    private final int size;

    /**
     * Generate character n-gram features
     *
     * @param size size of the character n-grams to produce
     */
    public CharacterFeatureGenerator(int size) {
        this.size = size;
    }

    @Override
    public void addTokenFeatures(Sequence seq, int index, List<String> features) {
        // Because we can easily repeat character sequences, we use a set to filter
        Set<String> ngrams = new THashSet<String>();
        String token = seq.get(index).token.toLowerCase();
        for (int i = 0; i + size <= token.length(); i++) {
            ngrams.add("CHAR" + size + ":" + token.substring(i, i + size));
        }
        features.addAll(ngrams);
    }
}
