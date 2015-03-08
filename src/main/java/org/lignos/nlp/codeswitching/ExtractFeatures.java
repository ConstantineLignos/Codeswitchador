package org.lignos.nlp.codeswitching;

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

import org.lignos.nlp.codeswitching.features.CodeswitchFeatureSet;
import org.lignos.nlp.sequence.FeatureSequence;
import org.lignos.nlp.sequence.FeatureSequenceCorpusReader;
import org.lignos.nlp.util.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;


/**
 * Extract features from a sequence of tokens.
  */
public class ExtractFeatures {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: ExtractFeatures infile outfile features");
            System.exit(1);
        }
        String inPath = args[0];
        String outPath = args[1];
        String featurePath = args[2];

        List<String> featureNames = StringUtil.fileLinesAsStringList(featurePath);

        // Create feature sets and load data
        CodeswitchFeatureSet featureSet = CodeswitchFeatureSet.createFeatureSet(featureNames);
        FeatureSequenceCorpusReader reader =
                FeatureSequenceCorpusReader.getCorpusFeatures(inPath, featureSet);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new File(outPath));
        } catch (FileNotFoundException e) {
            System.err.println("Could not open output file: " + outPath);
            System.exit(1);
        }

        for (FeatureSequence seq : reader) {
            String[] labels = seq.getLabels();
            List<List<String>> features = seq.getFeatures();
            for (int i = 0; i < seq.size(); i++) {
                String label = labels[i];
                List<String> featureList = features.get(i);
                writer.println(StringUtil.join(featureList, " ") + " " + label);
            }
            writer.println();
        }
        writer.close();
    }
}
