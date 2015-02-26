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

    private List<String> list;

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

        list = new LinkedList<String>();
        gen1.addLabelFeatures(labels, 1, list);
        assertArrayEquals(new String[]{"TAG-1:a"}, list.toArray());

        list = new LinkedList<String>();
        gen1.addLabelFeatures(labels, 2, list);
        assertArrayEquals(new String[]{"TAG-1:b"}, list.toArray());

        TagContextFeatureGenerator gen2 = new TagContextFeatureGenerator(-2);

        list = new LinkedList<String>();
        gen2.addLabelFeatures(labels, 2, list);
        assertArrayEquals(new String[] {"TAG-2:a"}, list.toArray());
    }

    /**
     * Test that no features are generated at the start of the sequence.
     */
    @Test
    public void testGenerateStart() throws Exception {
        Sequence seq = new Sequence("a/a b/b c/c", false);
        String[] labels = seq.getLabels();

        TagContextFeatureGenerator gen1 = new TagContextFeatureGenerator(-1);

        list = new LinkedList<String>();
        gen1.addLabelFeatures(labels, 0, list);
        assertTrue(list.isEmpty());

        TagContextFeatureGenerator gen2 = new TagContextFeatureGenerator(-2);

        list = new LinkedList<String>();
        gen2.addLabelFeatures(labels, 0, list);
        assertTrue(list.isEmpty());

        list = new LinkedList<String>();
        gen2.addLabelFeatures(labels, 1, list);
        assertTrue(list.isEmpty());
    }
}
