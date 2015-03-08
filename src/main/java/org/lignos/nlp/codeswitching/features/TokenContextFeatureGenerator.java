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
