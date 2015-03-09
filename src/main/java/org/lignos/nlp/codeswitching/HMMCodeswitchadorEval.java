package org.lignos.nlp.codeswitching;

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

import org.lignos.nlp.util.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Evaluate the performance of the HMM model.
 */
public class HMMCodeswitchadorEval {

    private static final String USAGE = "Usage: HMM CodeswitchadorEval " +
            "trainfilelist testfile outputdir lang1 wordlist1 lang2 wordlist2";

    private final String[] trainPaths;
    private final String[] trainNames;
    private final String testPath;
    private final String outDirPath;
    private final String lang1;
    private final String wordlist1Path;
    private final String lang2;
    private final String wordlist2Path;

    /**
     * Set up an evaluation experiment
     * @param trainPaths paths of training files
     * @param testPath path of testing file
     * @param outDirPath output directory
     * @param trainNames name of training conditions
     * @param wordlist1Path path of English word list
     * @param wordlist2Path path of Spanish word list
     */
    public HMMCodeswitchadorEval(String[] trainPaths, String testPath, String outDirPath,
                                 String[] trainNames, String lang1, String wordlist1Path,
                                 String lang2, String wordlist2Path) {
        this.trainPaths = trainPaths;
        this.trainNames = trainNames;
        this.testPath = testPath;
        this.outDirPath = outDirPath;
        this.lang1 = lang1;
        this.wordlist1Path = wordlist1Path;
        this.lang2 = lang2;
        this.wordlist2Path = wordlist2Path;
    }

    /**
     * Perform the evaluation
     */
    private void run() {
        long startTime = System.nanoTime();

        // Open an output CSV
        String csvPath = Paths.get(outDirPath, "perf_hmm.csv").toString();
        PrintWriter csv = null;
        try {
            // Make the parent directories as needed
            File csvFile = new File(csvPath);
            File parent = csvFile.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            csv = new PrintWriter(csvFile);
        } catch (FileNotFoundException e) {
            System.err.println("Could not open input file: " + csvPath);
            System.exit(1);
        }


        // Write header
        csv.println("Features,Name,Accuracy,OOV,OOVAccuracy");
        String features = "HMMBigram";

        // Run each
        // Start each task
        for (int i = 0; i < trainPaths.length; i++) {
            String trainPath = trainPaths[i];
            String trainName = trainNames[i];
            float[] result = HMMCodeswitchador.evalHMM(lang1, wordlist1Path, lang2, wordlist2Path,
                    trainPath, testPath);
            csv.println(features + "," + trainName + "," + result[0] + "," + result[2] + "," + result[1]);
        }
        csv.close();

        // Output elapsed time
        long elapsed = System.nanoTime() - startTime;
        System.out.println("Elapsed time: " + TimeUnit.NANOSECONDS.toSeconds(elapsed) + " seconds");
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 7) {
            System.err.println(USAGE);
            System.exit(1);
        }

        // Extract arguments
        String trainFileList = args[0];
        String testPath = args[1];
        String outDirPath = args[2];
        String lang1 = args[3];
        String lang1WordlistPath = args[4];
        String lang2 = args[5];
        String lang2WordlistPath = args[6];

        // Read in training paths
        List<String> trainFileLines = StringUtil.fileLinesAsStringList(trainFileList);
        String[] trainPaths = new String[trainFileLines.size()];
        String[] trainNames = new String[trainFileLines.size()];
        int i = 0;
        for (String line : trainFileLines) {
            String[] splits = line.split("\\s+");
            trainNames[i] = splits[0];
            trainPaths[i] = splits[1];
            i++;
        }

        System.out.println("Training names: " + Arrays.toString(trainNames));
        System.out.println("Training on files: " + Arrays.toString(trainPaths));
        System.out.println("Testing on files: " + testPath);
        System.out.println("Writing to: " + outDirPath);
        System.out.println();

        // Evaluate
        HMMCodeswitchadorEval eval = new HMMCodeswitchadorEval(trainPaths, testPath,
                outDirPath, trainNames, lang1, lang1WordlistPath, lang2, lang2WordlistPath);
        eval.run();
    }
}
