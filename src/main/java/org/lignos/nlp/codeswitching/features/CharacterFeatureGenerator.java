package org.lignos.nlp.codeswitching.features;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.lignos.nlp.sequence.TokenSequenceFeatureGenerator;
import org.lignos.nlp.sequence.Sequence;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Generate character n-gram features
 */
public class CharacterFeatureGenerator extends TokenSequenceFeatureGenerator {

    private final int size;
    private final THashMap<String, List<String>> cache;

    /**
     * Generate character n-gram features
     *
     * @param size size of the character n-grams to produce
     */
    public CharacterFeatureGenerator(int size) {
        this.size = size;
        cache = new THashMap<String, List<String>>();
    }

    @Override
    public List<String> genTokenFeatures(Sequence seq, int index) {
        // Return cached features if available
        String token = seq.get(index).token.toLowerCase();
        List<String> features = cache.get(token);
        if (features != null) {
            return features;
        }

        // Otherwise, generate new ones
        features = new LinkedList<String>();
        // Because we can easily repeat character sequences, we use a set to filter
        Set<String> ngrams = new THashSet<String>();
        for (int i = 0; i + size <= token.length(); i++) {
            ngrams.add("CHAR" + size + ":" + token.substring(i, i + size));
        }
        features.addAll(ngrams);

        // Store in cache
        cache.put(token, features);

        return features;
    }
}
