package org.lignos.nlp.codeswitching.features;

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

    private List<String> list;
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

        list = new LinkedList<String>();
        gen1.addTokenFeatures(seq, 0, list);
        assertArrayEquals(new String[] {"CHAR1:a"}, list.toArray());

        // Note that because the ordering isn't guaranteed to be preserved, we have to test sorted.
        list = new LinkedList<String>();
        gen1.addTokenFeatures(seq, 1, list);
        result = list.toArray(new String[0]);
        Arrays.sort(result);
        assertArrayEquals(new String[]{"CHAR1:a", "CHAR1:b"}, result);

        list = new LinkedList<String>();
        gen1.addTokenFeatures(seq, 2, list);
        result = list.toArray(new String[0]);
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

        list = new LinkedList<String>();
        gen2.addTokenFeatures(seq, 0, list);
        result = list.toArray(new String[0]);
        Arrays.sort(result);
        assertArrayEquals(new String[]{"CHAR2:ab", "CHAR2:bc", "CHAR2:cd"}, result);
    }


}
