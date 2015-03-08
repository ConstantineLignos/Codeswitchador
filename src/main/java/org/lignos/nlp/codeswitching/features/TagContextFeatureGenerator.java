package org.lignos.nlp.codeswitching.features;

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
