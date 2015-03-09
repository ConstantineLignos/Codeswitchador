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


import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Test Sequence.
 *
 */
public class SequenceTest {

    /**
     * Test simple tags
     */
    @Test
    public void testBasicTags() throws Exception {
        Sequence seq = new Sequence("a/d ab/de abc/def", null);
        assertArrayEquals(new String[]{"d", "de", "def"}, seq.getLabels());
    }

    /**
     * Test simple tokens
     */
    @Test
    public void testBasicTokens() throws Exception {
        Sequence seq = new Sequence("a/a ab/b abc/c", null);
        assertEquals(seq.get(0).token, "a");
        assertEquals(seq.get(1).token, "ab");
        assertEquals(seq.get(2).token, "abc");
    }

    /**
     * Test dash tags
     */
    @Test
    public void testDashTags() throws Exception {
        Sequence seq = new Sequence("a/d-g ab/de-gh abc/def-ghi", null);
        assertArrayEquals(new String[]{"d", "de", "def"}, seq.getLabels());
        assertEquals(seq.get(0).comment, "g");
        assertEquals(seq.get(1).comment, "gh");
        assertEquals(seq.get(2).comment, "ghi");
        assertEquals(seq.get(0).token, "a");
        assertEquals(seq.get(1).token, "ab");
        assertEquals(seq.get(2).token, "abc");
    }

    /**
     * Test multiple slashes
     */
    @Test
    public void testMultiSlash() throws Exception {
        Sequence seq = new Sequence("a/w/d ab/x/de abc/y/def", null);
        assertArrayEquals(new String[]{"d", "de", "def"}, seq.getLabels());
        assertEquals(seq.get(0).token, "a/w");
        assertEquals(seq.get(1).token, "ab/x");
        assertEquals(seq.get(2).token, "abc/y");
    }
}
