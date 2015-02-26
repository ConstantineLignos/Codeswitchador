package org.lignos.nlp.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Utilities for working with collections
 */
public class CollectionsUtil {

    /**
     * Create a linked list concatenating the elements of two collections.
     * @param coll1 the first collection
     * @param coll2 the second collection
     * @param <T> type of collection elements
     * @return a linked list contained the concatenated elements of both collections
     */
    public static <T> LinkedList<T> combineToList(Collection<T> coll1, Collection<T> coll2) {
        LinkedList<T> output = new LinkedList<T>();
        output.addAll(coll1);
        output.addAll(coll2);
        return output;
    }

    /**
     * Get the key corresponding to the maximum value in the map.
     * @param map the map
     * @param <T> key type
     * @param <U> value type, which must be comparable
     * @return the key corresponding to the maximum value
     */
    public static <T, U extends Number & Comparable<U>> T getKeyWithMaxValue(Map<T, U> map) {
        T maxKey = null;
        U maxValue = null;
        for(T key : map.keySet()) {
            U value = map.get(key);
            if (maxValue == null || value.compareTo(maxValue) > 0) {
                maxKey = key;
                maxValue = value;
            }
        }
        return maxKey;
    }
}
