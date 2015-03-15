package org.lignos.nlp.sequence;

/**
 * Copyright 2012-2015 Constantine Lignos
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

import gnu.trove.set.hash.THashSet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Provide a reader for a corpus of token sequences.
 */
public class TokenSequenceCorpusReader implements Iterable<TokenSequence> {

    protected List<TokenSequence> sequences;

    /**
     * Create a new reader for the given path
     * @param path the path to read data from
     * @param ignoreTags tags marking tokens to be skipped over
     */
    public TokenSequenceCorpusReader(String path, String[] ignoreTags) throws IOException {
        sequences = new LinkedList<TokenSequence>();
        loadCorpus(path, ignoreTags);
    }

    protected void loadCorpus(String path, String[] ignoreTags) throws IOException {
        // Make the ignore tags into a set for fast checking
        Set<String> ignoreTagsSet = new THashSet<String>(Arrays.asList(ignoreTags));

        // Read in each sequence from the file
        BufferedReader lineReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
        String line;
        int lineNum = 0;
        while ((line = lineReader.readLine()) != null) {
            lineNum++;
            try {
                sequences.add(new TokenSequence(line, ignoreTagsSet));
            }
            catch (MalformedSequenceException e) {
                System.err.println("Bad utterance at line " + lineNum + " of file "  + path);
                System.err.println("Line contents: '" + line + "'");
                System.err.println("Reason: " + e.getMessage());
            }
        }
        lineReader.close();
    }

    public int size() {
        return sequences.size();
    }

    @Override
    public Iterator<TokenSequence> iterator() {
        return sequences.iterator();
    }
}
