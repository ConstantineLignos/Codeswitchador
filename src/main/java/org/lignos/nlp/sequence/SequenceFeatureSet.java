package org.lignos.nlp.sequence;

/**
 * Copyright 2015 Constantine Lignos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.LinkedList;
import java.util.List;

/**
 * A set of features to be used for feature generation.
 */
public class SequenceFeatureSet {

    /** Generators that only look at tokens */
    private final List<TokenSequenceFeatureGenerator> tokenGenerators;
    /** The individual feature tokenGenerators */
    private final List<LabelSequenceFeatureGenerator> labelGenerators;

    /**
     * Create a feature set that uses the specified tokenGenerators
     *
     * @param tokenGenerators the feature tokenGenerators to use
     */
    public SequenceFeatureSet(List<TokenSequenceFeatureGenerator> tokenGenerators,
                              List<LabelSequenceFeatureGenerator> labelGenerators) {
        this.tokenGenerators = tokenGenerators;
        this.labelGenerators = labelGenerators;
    }

    /**
     * Generate features at a position in a sequence using all feature tokenGenerators
     *
     * @param seq the input sequence
     * @param index the index in the sequence to use for feature extraction
     * @return the generated features
     */
    public List<String> generateTokenFeatures(TokenSequence seq, int index) {
        List<String> features = new LinkedList<String>();
        for (TokenSequenceFeatureGenerator generator : tokenGenerators) {
            features.addAll(generator.genTokenFeatures(seq, index));
        }
        return features;
    }

    /**
     * Generate features at a position in a sequence using all feature tokenGenerators
     *
     * @param labels the labels, which must be non-null up to index - 1
     * @param index the index in the sequence to use for feature extraction
     * @return the generated features
     */
    public List<String> generateLabelFeatures(String[] labels, int index) {
        List<String> features = new LinkedList<String>();
        for (LabelSequenceFeatureGenerator generator : labelGenerators) {
            features.addAll(generator.genLabelFeatures(labels, index));
        }
        return features;
    }

    /**
     * Generate features at a position in a sequence using all feature tokenGenerators
     *
     * @param seq the input sequence
     * @param labels the labels, which must be non-null up to index - 1
     * @param index the index in the sequence to use for feature extraction
     * @return the generated features
     */
    public List<String> generateAllFeatures(TokenSequence seq, String[] labels, int index) {
        List<String> features = new LinkedList<String>();
        for (TokenSequenceFeatureGenerator generator : tokenGenerators) {
            features.addAll(generator.genTokenFeatures(seq, index));
        }
        for (LabelSequenceFeatureGenerator generator : labelGenerators) {
            features.addAll(generator.genLabelFeatures(labels, index));
        }
        return features;
    }

    /**
     * Generate feature for every position in the sequence.
     * @param seq the input sequence
     * @param labels the labels, which must all be non-null
     * @return the generated features
     */
    public List<List<String>> generateAllSequenceFeatures(TokenSequence seq, String[] labels) {
        List<List<String>> output = new LinkedList<List<String>>();
        for (int i = 0; i < seq.size(); i++) {
            output.add(generateAllFeatures(seq, labels, i));
        }
        return output;
    }

    /**
     * Return whether this feature set has any token features.
     * @return whether there are any token features
     */
    public boolean hasTokenFeatures() {
        return tokenGenerators != null && !tokenGenerators.isEmpty();
    }

    /**
     * Return whether this feature set has any label features.
     * @return whether there are any label features
     */
    public boolean hasLabelFeatures() {
        return labelGenerators != null && !labelGenerators.isEmpty();
    }
}
