package org.lignos.nlp.codeswitching.features;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.lignos.nlp.sequence.Sequence;

import java.util.LinkedList;
import java.util.List;

/**
* Test SuffixFeatureGenerator.
 *
*/
public class SuffixFeatureGeneratorTest {

    private List<String> list;

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
        Sequence seq = new Sequence("a/a aa/a aaa/a", false);
        SuffixFeatureGenerator gen = new SuffixFeatureGenerator(1, 4, 3);

        // Too short for any features
        list = new LinkedList<String>();
        gen.addTokenFeatures(seq, 0, list);
        assertTrue(list.isEmpty());

        list = new LinkedList<String>();
        gen.addTokenFeatures(seq, 1, list);
        assertTrue(list.isEmpty());

        list = new LinkedList<String>();
        gen.addTokenFeatures(seq, 2, list);
        assertTrue(list.isEmpty());
    }

    /**
     * Test that the proper range of suffixes is produced.
     *
     * @throws Exception
     */
    @Test
    public void testGenerateSuffixRange() throws Exception {
        Sequence seq = new Sequence("aaab/a aaabc/a aaabcd/a aaabcde/a", false);
        SuffixFeatureGenerator gen = new SuffixFeatureGenerator(1, 4, 3);

        list = new LinkedList<String>();
        gen.addTokenFeatures(seq, 0, list);
        assertArrayEquals(new String[]{"SUFFIX:b"}, list.toArray());

        list = new LinkedList<String>();
        gen.addTokenFeatures(seq, 1, list);
        assertArrayEquals(new String[]{"SUFFIX:c", "SUFFIX:bc"}, list.toArray());

        list = new LinkedList<String>();
        gen.addTokenFeatures(seq, 2, list);
        assertArrayEquals(new String[]{"SUFFIX:d", "SUFFIX:cd", "SUFFIX:bcd"}, list.toArray());

        list = new LinkedList<String>();
        gen.addTokenFeatures(seq, 3, list);
        assertArrayEquals(new String[]{"SUFFIX:e", "SUFFIX:de", "SUFFIX:cde", "SUFFIX:bcde"},
                list.toArray());
    }
}
