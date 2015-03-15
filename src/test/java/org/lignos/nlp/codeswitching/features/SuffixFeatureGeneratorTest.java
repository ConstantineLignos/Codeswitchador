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

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.lignos.nlp.sequence.TokenSequence;

import java.util.List;

/**
* Test SuffixFeatureGenerator.
 *
*/
public class SuffixFeatureGeneratorTest {

    private List<String> features;

    @Before
    public void before() throws Exception {

    }

    @After
    public void after() throws Exception {
    }

    /**
     * Test that minimum stem length is respected.
     *
     * @throws Exception
     */
    @Test
    public void testGenerateMinStem() throws Exception {
        TokenSequence seq = new TokenSequence("a/a aa/a aaa/a", null);
        SuffixFeatureGenerator gen = new SuffixFeatureGenerator(1, 4, 3);

        // Too short for any features
        features = gen.genTokenFeatures(seq, 0);
        assertTrue(features.isEmpty());

        features = gen.genTokenFeatures(seq, 1);
        assertTrue(features.isEmpty());

        features = gen.genTokenFeatures(seq, 2);
        assertTrue(features.isEmpty());
    }

    /**
     * Test that the proper range of suffixes is produced.
     *
     * @throws Exception
     */
    @Test
    public void testGenerateSuffixRange() throws Exception {
        TokenSequence seq = new TokenSequence("aaab/a aaabc/a aaabcd/a aaabcde/a", null);
        SuffixFeatureGenerator gen = new SuffixFeatureGenerator(1, 4, 3);

        features = gen.genTokenFeatures(seq, 0);
        assertArrayEquals(new String[]{"SUFFIX:b"}, features.toArray());

        features = gen.genTokenFeatures(seq, 1);
        assertArrayEquals(new String[]{"SUFFIX:c", "SUFFIX:bc"}, features.toArray());

        features = gen.genTokenFeatures(seq, 2);
        assertArrayEquals(new String[]{"SUFFIX:d", "SUFFIX:cd", "SUFFIX:bcd"}, features.toArray());

        features = gen.genTokenFeatures(seq, 3);
        assertArrayEquals(new String[]{"SUFFIX:e", "SUFFIX:de", "SUFFIX:cde", "SUFFIX:bcde"},
                features.toArray());
    }
}
