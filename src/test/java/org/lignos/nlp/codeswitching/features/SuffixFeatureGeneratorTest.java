package org.lignos.nlp.codeswitching.features;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.lignos.nlp.sequence.Sequence;

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
        Sequence seq = new Sequence("a/a aa/a aaa/a", false);
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
        Sequence seq = new Sequence("aaab/a aaabc/a aaabcd/a aaabcde/a", false);
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
