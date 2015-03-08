package org.lignos.nlp.util;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * String utility methods.
 */
public class StringUtil {
    /**
     * Join a collection of strings using a delimiter, like Python's join
     * @param list Collection of strings to join
     * @param delim delimiter
     * @return a string containing the items joined by a delimiter
     */
    static public String join(Collection<String> list, String delim) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : list) {
            if (first)
                first = false;
            else
                sb.append(delim);
            sb.append(item);
        }
        return sb.toString();
    }

    /**
     * Join an array of strings using a delimiter, like Python's join
     * @param strings array of strings to join
     * @param delim delimiter
     * @return a string containing the items joined by a delimiter
     */
    static public String join(String[] strings, String delim) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : strings) {
            if (first)
                first = false;
            else
                sb.append(delim);
            sb.append(item);
        }
        return sb.toString();
    }

    /**
     * Put all lines of a file into a list
     * @param path the path to the file
     * @return a list containing with one file line in each element
     */
    public static List<String> fileLinesAsStringList(String path) {
        // Set up feature generation
        File featureFile = new File(path);
        List<String> featureNames = new LinkedList<String>();
        Scanner featureScanner = null;
        try {
            featureScanner = new Scanner(featureFile);
        } catch (FileNotFoundException err) {
            System.err.println("Could not open feature file: " + path);
            System.exit(1);
        }
        while (featureScanner.hasNextLine()) {
            featureNames.add(featureScanner.nextLine());
        }
        return featureNames;
    }
}
