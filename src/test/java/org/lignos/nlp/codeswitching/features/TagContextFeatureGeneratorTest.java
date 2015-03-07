package org.lignos.nlp.codeswitching.features;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.lignos.nlp.sequence.Sequence;

import java.util.LinkedList;
import java.util.List;

/**
* Test StateContextFeatureGenerator.
*/
public class TagContextFeatureGeneratorTest {

    private List<String> features;

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
    * Test that previous states can be used as features.
    */
    @Test
    public void testGenerateMiddle() throws Exception {
        Sequence seq = new Sequence("a/a b/b c/c", false);
        String[] labels = seq.getLabels();

        TagContextFeatureGenerator gen1 = new TagContextFeatureGenerator(-1);

        features = gen1.genLabelFeatures(labels, 1);
        assertArrayEquals(new String[]{"TAG-1:a"}, features.toArray());

        features = gen1.genLabelFeatures(labels, 2);
        assertArrayEquals(new String[]{"TAG-1:b"}, features.toArray());

        TagContextFeatureGenerator gen2 = new TagContextFeatureGenerator(-2);

        features = gen2.genLabelFeatures(labels, 2);
        assertArrayEquals(new String[] {"TAG-2:a"}, features.toArray());
    }

    /**
     * Test that no features are generated at the start of the sequence.
     */
    @Test
    public void testGenerateStart() throws Exception {
        Sequence seq = new Sequence("a/a b/b c/c", false);
        String[] labels = seq.getLabels();

        TagContextFeatureGenerator gen1 = new TagContextFeatureGenerator(-1);

        features = gen1.genLabelFeatures(labels, 0);
        assertTrue(features.isEmpty());

        TagContextFeatureGenerator gen2 = new TagContextFeatureGenerator(-2);

        features = gen2.genLabelFeatures(labels, 0);
        assertTrue(features.isEmpty());

        features = gen2.genLabelFeatures(labels, 1);
        assertTrue(features.isEmpty());
    }
}
