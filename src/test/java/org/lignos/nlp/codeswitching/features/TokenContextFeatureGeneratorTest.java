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

import java.util.LinkedList;
import java.util.List;

/**
* Test TokenContextFeatureGenerator.
*/
public class TokenContextFeatureGeneratorTest {

    private List<String> features;

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Test generating the current token in the beginning, middle, and end of a sequence.
     */
    @Test
    public void testGenerateCurrent() throws Exception {
        Sequence seq = new Sequence("a/a b/a c/a", null);
        TokenContextFeatureGenerator gen = new TokenContextFeatureGenerator(0);
        // First token
        features = gen.genTokenFeatures(seq, 0);
        assertArrayEquals(new String[]{"TOK0:a"}, features.toArray());

        // Middle token
        features = gen.genTokenFeatures(seq, 1);
        assertArrayEquals(new String[]{"TOK0:b"}, features.toArray());

        // End token
        features = gen.genTokenFeatures(seq, 2);
        assertArrayEquals(new String[]{"TOK0:c"}, features.toArray());
    }

    /**
     * Test generating the previous token in the middle of a sequence.
    */
    @Test
    public void testGeneratePrevMiddle() throws Exception {
        Sequence seq = new Sequence("a/a b/a c/a", null);
        TokenContextFeatureGenerator gen = new TokenContextFeatureGenerator(-1);
        features = gen.genTokenFeatures(seq, 1);
        assertArrayEquals(new String[]{"TOK-1:a"}, features.toArray());
    }

    /**
     * Test generating the previous token at the beginning of a sequence.
     */
    @Test
    public void testGeneratePrevStart() throws Exception {
        Sequence seq = new Sequence("a/a b/a c/a", null);
        TokenContextFeatureGenerator gen = new TokenContextFeatureGenerator(-1);
        features = gen.genTokenFeatures(seq, 0);
        assertArrayEquals(new String[]{}, features.toArray());
    }

    /**
     * Test generating the next token in the middle of a sequence.
     */
    @Test
    public void testGenerateNextMiddle() throws Exception {
        Sequence seq = new Sequence("a/a b/a c/a", null);
        TokenContextFeatureGenerator gen = new TokenContextFeatureGenerator(1);
        features = gen.genTokenFeatures(seq, 1);
        assertArrayEquals(new String[]{"TOK1:c"}, features.toArray());
    }

    /**
     * Test generating the previous token at the beginning of a sequence.
     */
    @Test
    public void testGenerateNextEnd() throws Exception {
        Sequence seq = new Sequence("a/a b/a c/a", null);
        TokenContextFeatureGenerator gen = new TokenContextFeatureGenerator(1);
        features = gen.genTokenFeatures(seq, 2);
        assertArrayEquals(new String[]{}, features.toArray());
    }
}
