package org.lignos.nlp.codeswitching.features;

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

    private List<String> list;

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
        Sequence seq = new Sequence("a/a b/a c/a", false);
        TokenContextFeatureGenerator gen = new TokenContextFeatureGenerator(0);
        // First token
        list = new LinkedList<String>();
        gen.addTokenFeatures(seq, 0, list);
        assertArrayEquals(new String[]{"TOK0:a"}, list.toArray());

        // Middle token
        list = new LinkedList<String>();
        gen.addTokenFeatures(seq, 1, list);
        assertArrayEquals(new String[]{"TOK0:b"}, list.toArray());

        // End token
        list = new LinkedList<String>();
        gen.addTokenFeatures(seq, 2, list);
        assertArrayEquals(new String[]{"TOK0:c"}, list.toArray());
    }

    /**
     * Test generating the previous token in the middle of a sequence.
    */
    @Test
    public void testGeneratePrevMiddle() throws Exception {
        Sequence seq = new Sequence("a/a b/a c/a", false);
        TokenContextFeatureGenerator gen = new TokenContextFeatureGenerator(-1);
        list = new LinkedList<String>();
        gen.addTokenFeatures(seq, 1, list);
        assertArrayEquals(new String[]{"TOK-1:a"}, list.toArray());
    }

    /**
     * Test generating the previous token at the beginning of a sequence.
     */
    @Test
    public void testGeneratePrevStart() throws Exception {
        Sequence seq = new Sequence("a/a b/a c/a", false);
        TokenContextFeatureGenerator gen = new TokenContextFeatureGenerator(-1);
        list = new LinkedList<String>();
        gen.addTokenFeatures(seq, 0, list);
        assertArrayEquals(new String[]{}, list.toArray());
    }

    /**
     * Test generating the next token in the middle of a sequence.
     */
    @Test
    public void testGenerateNextMiddle() throws Exception {
        Sequence seq = new Sequence("a/a b/a c/a", false);
        TokenContextFeatureGenerator gen = new TokenContextFeatureGenerator(1);
        list = new LinkedList<String>();
        gen.addTokenFeatures(seq, 1, list);
        assertArrayEquals(new String[]{"TOK1:c"}, list.toArray());
    }

    /**
     * Test generating the previous token at the beginning of a sequence.
     */
    @Test
    public void testGenerateNextEnd() throws Exception {
        Sequence seq = new Sequence("a/a b/a c/a", false);
        TokenContextFeatureGenerator gen = new TokenContextFeatureGenerator(1);
        list = new LinkedList<String>();
        gen.addTokenFeatures(seq, 2, list);
        assertArrayEquals(new String[]{}, list.toArray());
    }
}
