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

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.lignos.nlp.sequence.Sequence;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test TagHistoryFeatureGenerator
 */

public class TagHistoryFeatureGeneratorTest {

    private List<String> features;

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Test that correct features are generated in the middle of a sequence.
     */
    @Test
    public void testGenerateMiddle() throws Exception {
        Sequence seq = new Sequence("a/a b/b c/c", false);
        String[] labels = seq.getLabels();

        TagHistoryFeatureGenerator gen1 = new TagHistoryFeatureGenerator(1);

        features = gen1.genLabelFeatures(labels, 1);
        assertArrayEquals(new String[]{"TAGHIST1:a"}, features.toArray());

        features = gen1.genLabelFeatures(labels, 2);
        assertArrayEquals(new String[]{"TAGHIST1:b"}, features.toArray());

        TagHistoryFeatureGenerator gen2 = new TagHistoryFeatureGenerator(2);

        features = gen2.genLabelFeatures(labels, 2);
        assertArrayEquals(new String[] {"TAGHIST2:a,b"}, features.toArray());
    }

    /**
     * Test that no features are generated at the start of the sequence.
     */
    @Test
    public void testGenerateStart() throws Exception {
        Sequence seq = new Sequence("a/a b/b c/c", false);
        String[] labels = seq.getLabels();

        TagHistoryFeatureGenerator gen1 = new TagHistoryFeatureGenerator(1);

        features = gen1.genLabelFeatures(labels, 0);
        assertTrue(features.isEmpty());

        TagHistoryFeatureGenerator gen2 = new TagHistoryFeatureGenerator(2);

        features = gen2.genLabelFeatures(labels, 0);
        assertTrue(features.isEmpty());

        features = gen2.genLabelFeatures(labels, 1);
        assertTrue(features.isEmpty());
    }
}
