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
import org.lignos.nlp.sequence.Sequence;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Test CharacterFeatureGenerator.
 *
 */
public class CharacterFeatureGeneratorTest {

    private List<String> features;
    private String[] result;

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Test single characters
     */
    @Test
    public void testAddFeatures1() throws Exception {
        Sequence seq = new Sequence("a/a ab/a abc/a", false);
        CharacterFeatureGenerator gen1 = new CharacterFeatureGenerator(1);

        features = gen1.genTokenFeatures(seq, 0);
        assertArrayEquals(new String[]{"CHAR1:a"}, features.toArray());

        // Note that because the ordering isn't guaranteed to be preserved, we have to test sorted.
        features = gen1.genTokenFeatures(seq, 1);
        result = features.toArray(new String[0]);
        Arrays.sort(result);
        assertArrayEquals(new String[]{"CHAR1:a", "CHAR1:b"}, result);

        features = gen1.genTokenFeatures(seq, 2);
        result = features.toArray(new String[0]);
        Arrays.sort(result);
        assertArrayEquals(new String[] {"CHAR1:a", "CHAR1:b", "CHAR1:c"}, result);
    }

    /**
     * Test multiple characters
     */
    @Test
    public void testAddFeatures2() throws Exception {
        Sequence seq = new Sequence("abcd/a", false);
        CharacterFeatureGenerator gen2 = new CharacterFeatureGenerator(2);

        features = gen2.genTokenFeatures(seq, 0);
        result = features.toArray(new String[0]);
        Arrays.sort(result);
        assertArrayEquals(new String[]{"CHAR2:ab", "CHAR2:bc", "CHAR2:cd"}, result);
    }


}
