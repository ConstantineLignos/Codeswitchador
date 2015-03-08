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

import java.util.List;

/**
 * Generates features based on label sequences.
 */
public abstract class LabelSequenceFeatureGenerator {

    /**
     * Generate features based on labels from a position in a sequence. Instantiate
     * {@link TokenSequenceFeatureGenerator TokenSequenceFeatureGenerator} instead to generate
     * features dependent on the label history.
     *
     * @param labels the labels, which must be defined up to index - 1
     * @param index the index in the sequence to use for feature extraction
     */
    public abstract List<String> genLabelFeatures(String[] labels, int index);
}
