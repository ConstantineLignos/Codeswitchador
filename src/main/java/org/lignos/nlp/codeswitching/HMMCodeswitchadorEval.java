package org.lignos.nlp.codeswitching;

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
            "trainfilelist testfile outputdir englishwordlist spanishwordlist";

    private final String[] trainPaths;
    private final String[] trainNames;
    private final String testPath;
    private final String outDirPath;
    private final String englishWordlistPath;
    private final String spanishWordlistPath;

    /**
     * Set up an evaluation experiment
     * @param trainPaths paths of training files
     * @param testPath path of testing file
     * @param outDirPath output directory
     * @param trainNames name of training conditions
     * @param englishWordlistPath path of English word list
     * @param spanishWordlistPath path of Spanish word list
     */
    public HMMCodeswitchadorEval(String[] trainPaths, String testPath, String outDirPath,
                                 String[] trainNames, String englishWordlistPath, String spanishWordlistPath) {
        this.trainPaths = trainPaths;
        this.trainNames = trainNames;
        this.testPath = testPath;
        this.outDirPath = outDirPath;
        this.englishWordlistPath = englishWordlistPath;
        this.spanishWordlistPath = spanishWordlistPath;
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
            float[] result = HMMCodeswitchador.evalHMM(englishWordlistPath, spanishWordlistPath,
                    trainPath, testPath);
            csv.println(features + "," + trainName + "," + result[0] + "," + result[2] + "," + result[1]);
        }
        csv.close();

        // Output elapsed time
        long elapsed = System.nanoTime() - startTime;
        System.out.println("Elapsed time: " + TimeUnit.NANOSECONDS.toSeconds(elapsed) + " seconds");
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 5) {
            System.err.println(USAGE);
            System.exit(1);
        }

        // Extract arguments
        String trainFileList = args[0];
        String testPath = args[1];
        String outDirPath = args[2];
        String englishWordlistPath = args[3];
        String spanishWordlistPath = args[4];


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
                outDirPath, trainNames, englishWordlistPath, spanishWordlistPath);
        eval.run();
    }
}
