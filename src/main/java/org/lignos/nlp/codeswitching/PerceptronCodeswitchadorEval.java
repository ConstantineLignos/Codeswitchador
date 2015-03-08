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

import org.lignos.nlp.sequence.SequenceEvaluator;
import org.lignos.nlp.util.StringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Evaluate PerceptronCodeswitchador in varying feature and training sets.
 */
public class PerceptronCodeswitchadorEval {

    private static final String USAGE = "Usage: PerceptronCodeswitchadorEval " +
        "trainfilelist testfile outputdir featurepathlist iterations";

    private final String[] trainPaths;
    private final String[] trainNames;
    private final String testPath;
    private final String outDirPath;
    private final String[] featurePaths;
    private final String[] featureNames;
    private final int iterations;

    public PerceptronCodeswitchadorEval(String[] trainPaths, String testPath, String outDirPath,
                                        String[] featurePaths, int iterations, String[] trainNames,
                                        String[] featureNames) {
        this.trainPaths = trainPaths;
        this.trainNames = trainNames;
        this.testPath = testPath;
        this.outDirPath = outDirPath;
        this.featurePaths = featurePaths;
        this.featureNames = featureNames;
        this.iterations = iterations;
    }

    private void run() {
        long startTime = System.nanoTime();

        // Open an output CSV
        String csvPath = Paths.get(outDirPath, "perf_perceptron.csv").toString();
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

        // Output list
        List<String> outputLines = Collections.synchronizedList(new LinkedList<String>());

        // Get the number of cores and start a thread pool. We save one core to avoid slamming the
        // machine.
        int cores = Runtime.getRuntime().availableProcessors() - 1;
        System.out.println("Number of parallel workers: " + cores);
        ExecutorService pool = Executors.newFixedThreadPool(cores);

        // Start each task
        for (int i = 0; i < featurePaths.length; i++) {
            String featurePath = featurePaths[i];
            String featureName = featureNames[i];
            for (int j = 0; j < trainPaths.length; j++) {
                String trainPath = trainPaths[j];
                String trainName = trainNames[j];
                pool.submit(new EvalRunner(trainPath, trainName, featurePath, featureName, outputLines));
            }
        }

        // Shut down the pool and wait (forever) until everything is done
        pool.shutdown();
        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            // Write the output
            csv.println("Features,Name,Accuracy,OOV,OOVAccuracy");
            for (String line : outputLines) {
                csv.println(line);
            }
        }
        catch (InterruptedException e) {
            System.err.println("Execution interrupted!");
            System.exit(1);
        }
        finally {
            csv.close();
        }

        // Output elapsed time
        long elapsed = System.nanoTime() - startTime;
        System.out.println("Elapsed time: " + TimeUnit.NANOSECONDS.toSeconds(elapsed) + " seconds");
    }

    public class EvalRunner implements Runnable {

        private String trainPath;
        private String trainName;
        private String featurePath;
        private String featureName;
        private final List<String> output;

        public EvalRunner(String trainPath, String trainName, String featurePath,
                          String featureName, List<String> outputLines) {
            this.trainPath = trainPath;
            this.trainName = trainName;
            this.featurePath = featurePath;
            this.featureName = featureName;
            this.output = outputLines;
        }

        @Override
        public void run() {
            // Set output path
            String outPath = Paths.get(outDirPath, featureName, "test_" + trainName + ".txt").toString();

            // Train
            PerceptronCodeswitchador cs = PerceptronCodeswitchador.getFromPaths(
                    trainPath, testPath, featurePath);
            // Test
            cs.run(iterations, outPath);

            // Evaluate
            SequenceEvaluator eval = SequenceEvaluator.getFromPaths(testPath, outPath, trainPath);
            eval.eval(true);
            eval.printResults();
            System.out.println();

            output.add(featureName + "," + trainName + "," + eval.getAccuracy() + "," +
                    eval.getOovRate() + "," + eval.getOovAccuracy());
        }
    }

    public static void main(String[] args) {
        if (args.length < 5) {
            System.err.println(USAGE);
            System.exit(1);
        }

        // Extract arguments
        String trainFileList = args[0];
        String testPath = args[1];
        String outDirPath = args[2];
        String featurePathList = args[3];
        int iterations = Integer.parseInt(args[4]);

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

        // Read in feature paths and convert to array
        List<String> featureFileLines = StringUtil.fileLinesAsStringList(featurePathList);
        String[] featurePaths = new String[featureFileLines.size()];
        String[] featureNames = new String[featureFileLines.size()];
        i = 0;
        for (String line : featureFileLines) {
            String[] splits = line.split("\\s+");
            featureNames[i] = splits[0];
            featurePaths[i] = splits[1];
            i++;
        }

        System.out.println("Training names: " + Arrays.toString(trainNames));
        System.out.println("Training on files: " + Arrays.toString(trainPaths));
        System.out.println("Testing on files: " + testPath);
        System.out.println("Writing to: " + outDirPath);
        System.out.println("Feature files: " + Arrays.toString(featurePaths));
        System.out.println("Iterations: " + iterations);
        System.out.println();

        // Evaluate
        PerceptronCodeswitchadorEval eval = new PerceptronCodeswitchadorEval(trainPaths, testPath,
                outDirPath, featurePaths, iterations, trainNames, featureNames);
        eval.run();
    }
}
