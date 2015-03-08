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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Provide a reader for a corpus of sequences.
 */
public class SequenceCorpusReader implements Iterable<Sequence> {

    protected List<Sequence> sequences;

    /**
     * Create a new reader for the given path
     * @param path the path to read data from
     * @param keepPunc whether to keep punctuation tokens in the stream
     */
    public SequenceCorpusReader(String path, boolean keepPunc) throws IOException {
        sequences = new LinkedList<Sequence>();
        loadCorpus(path, keepPunc);
    }

    protected void loadCorpus(String path, boolean keepPunc) throws IOException {
        BufferedReader lineReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
        String line;
        int lineNum = 0;
        while ((line = lineReader.readLine()) != null) {
            lineNum++;
            try {
                sequences.add(new Sequence(line, keepPunc));
            }
            catch (MalformedSequenceException e) {
                System.err.println("Bad utterance at line " + lineNum);
                System.err.println("Line: " + line);
                System.err.println("Reason: " + e.getMessage());
            }
        }
        lineReader.close();
    }

    public int size() {
        return sequences.size();
    }

    @Override
    public Iterator<Sequence> iterator() {
        return sequences.iterator();
    }
}
