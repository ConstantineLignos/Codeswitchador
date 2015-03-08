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
import org.lignos.nlp.util.StringUtil;

import java.util.Arrays;
import java.util.LinkedList;
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
    public List<String> genLabelFeatures(String[] labels, int index) {
        List<String> features = new LinkedList<String>();
        // If labels is null, skip
        if (labels == null) {
            return features;
        }
        // Compute and check the target index
        int firstIndex = index - historySize;
        if (firstIndex >= 0 && firstIndex < labels.length) {
            String tags = StringUtil.join(Arrays.copyOfRange(labels, firstIndex, index), ",");
            features.add("TAGHIST" + historySize + ":" + tags);
        }
        return features;
    }
}
