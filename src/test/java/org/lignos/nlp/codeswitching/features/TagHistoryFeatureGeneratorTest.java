package org.lignos.nlp.codeswitching.features;

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

    private List<String> list;

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

        list = new LinkedList<String>();
        gen1.addLabelFeatures(labels, 1, list);
        assertArrayEquals(new String[]{"TAGHIST1:a"}, list.toArray());

        list = new LinkedList<String>();
        gen1.addLabelFeatures(labels, 2, list);
        assertArrayEquals(new String[]{"TAGHIST1:b"}, list.toArray());

        TagHistoryFeatureGenerator gen2 = new TagHistoryFeatureGenerator(2);

        list = new LinkedList<String>();
        gen2.addLabelFeatures(labels, 2, list);
        assertArrayEquals(new String[] {"TAGHIST2:a,b"}, list.toArray());
    }

    /**
     * Test that no features are generated at the start of the sequence.
     */
    @Test
    public void testGenerateStart() throws Exception {
        Sequence seq = new Sequence("a/a b/b c/c", false);
        String[] labels = seq.getLabels();

        TagHistoryFeatureGenerator gen1 = new TagHistoryFeatureGenerator(1);

        list = new LinkedList<String>();
        gen1.addLabelFeatures(labels, 0, list);
        assertTrue(list.isEmpty());

        TagHistoryFeatureGenerator gen2 = new TagHistoryFeatureGenerator(2);

        list = new LinkedList<String>();
        gen2.addLabelFeatures(labels, 0, list);
        assertTrue(list.isEmpty());

        list = new LinkedList<String>();
        gen2.addLabelFeatures(labels, 1, list);
        assertTrue(list.isEmpty());
    }
}
