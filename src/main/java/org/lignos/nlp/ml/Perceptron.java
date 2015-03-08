package org.lignos.nlp.ml;

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

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.lignos.nlp.sequence.SequenceFeatureSet;
import org.lignos.nlp.util.CollectionsUtil;
import org.lignos.nlp.sequence.Sequence;
import org.lignos.nlp.sequence.SequenceCorpusReader;

import java.io.PrintWriter;
import java.util.*;

/**
 * A simple Perceptron implementation. Supports averaging and greedy or Viterbi decoding.
 */
public class Perceptron {

    /**
     * Store a map of features under each class encountered in training.
     * Outer map: key: class, value: feature weight map
     * Inner maps: key: feature, value: weight
     * In order to make the weights the same type between averaged and non-averaged, we have to
     * use double where there would otherwise be an integer/long.
     */
    private Map<String, Map<String, Double>> weights;
    private Map<String, Map<String, Integer>> lastUpdates;
    private Map<String, Map<String, Double>> totalWeights;
    private Map<String, Map<String, Double>> finalWeights;
    /** Set of all labels in the training data. */
    private Set<String> allLabels;

    /** Whether to use greedy decoding instead of Viterbi */
    private final boolean greedy;
    /** The feature set to use */
    private final SequenceFeatureSet featureSet;
    /** Whether to use averaged weights */
    private final boolean averaged;

    /**
     * Create a new instance with the specified decoding method and feature set.
     *
     * @param featureSet the feature set to use
     * @param greedyDecode whether to use greedy decoding instead of Viterbi
     * @param averaged whether to use averaged weights for predictions
     */
    public Perceptron(SequenceFeatureSet featureSet, boolean greedyDecode, boolean averaged) {
        this.featureSet = featureSet;
        this.greedy = greedyDecode;
        this.averaged = averaged;
    }

    /** Train the perceptron on the given data. Averaged weights are never used during training.
     *
     * @param iterations number of iterations in training
     * @param reader data to use for training
     */
    public float[] train(int iterations, SequenceCorpusReader reader, boolean debug) {
        // Initialize
        weights = new THashMap<String, Map<String, Double>>();
        totalWeights = new THashMap<String, Map<String, Double>>();
        lastUpdates = new THashMap<String, Map<String, Integer>>();
        // During training, these are set to be the same. At the end of training, new final weights
        // are computed based on whether averaging is in use.
        finalWeights = weights;
        allLabels = new THashSet<String>();
        // Number of sequences seen during training
        int examples = 0;
        // Accuracy for each iteration
        float[] accuracies = new float[iterations];

        // Training iterations
        for (int i = 0; i < iterations; i++) {
            // Count hits/misses
            int hits = 0;
            int misses = 0;

            // Sequences
            for (Sequence seq : reader) {
                // Count each sequence
                examples += 1;
                // Get labels and features
                String[] labels = seq.getLabels();
                if (labels == null) {
                    throw new RuntimeException("Cannot train on unlabeled data");
                }

                // Initialize data structures for any new labels
                for (String label : labels) {
                    if (!allLabels.contains(label)) {
                        allLabels.add(label);
                        weights.put(label, new THashMap<String, Double>());
                        totalWeights.put(label, new THashMap<String, Double>());
                        lastUpdates.put(label, new THashMap<String, Integer>());
                    }
                }

                // Predict and update
                String[] predictions = predict(seq);

                // Update based on the contents
                update(predictions, labels, seq, examples, debug);

                // Count accuracy
                for (int j = 0; j < labels.length; j++) {
                    if (labels[j].equals(predictions[j])) {
                        hits += 1;
                    } else {
                        misses += 1;
                    }
                }
            }
            // Store accuracy
            accuracies[i] = (float) hits / (hits + misses);
        }

        // Complete totals
        for (String label : weights.keySet()) {
            Map<String, Double> labelWeights = weights.get(label);
            Map<String, Double> labelTotals = totalWeights.get(label);
            Map<String, Integer> labelUpdates = lastUpdates.get(label);

            for (String feature : labelWeights.keySet()) {
                double currentWeight = labelWeights.getOrDefault(feature, 0.0);
                // Add to averaged total using the current value. Can skip
                // update if the weight was zero.
                if (currentWeight != 0) {
                    double lastTotal = labelTotals.getOrDefault(feature, 0.0);
                    int lastUpdate = labelUpdates.get(feature);
                    int timeDelta = examples - lastUpdate;
                    labelTotals.put(feature, lastTotal + timeDelta * currentWeight);
                }
                // Not strictly necessary to update this as it will never be read, but it's
                // useful to be sure that all averages are up to date at end of training.
                labelUpdates.put(feature, examples);
            }
        }

        // Compute final weights
        finalWeights = new THashMap<String, Map<String, Double>>();
        for (String label : allLabels) {
            Map<String, Double> featureWeights = weights.get(label);
            Map<String, Double> totalFeatureWeights = totalWeights.get(label);
            // Create final weights for this label
            Map<String, Double> finalFeatureWeights = new THashMap<String, Double>();
            finalWeights.put(label, finalFeatureWeights);
            // Store final weights
            for (String feature : totalFeatureWeights.keySet()) {
                // Average or just carry over existing weights
                double weight = averaged ?
                        totalFeatureWeights.get(feature) / (double) examples :
                        featureWeights.get(feature);
                finalFeatureWeights.put(feature, weight);
            }
        }

        return accuracies;
    }

    /** Test the perceptron on the given data.
     *
     * @param reader data to use for training
     * @param output destination for writing output data, may be null if no output is needed
     */
    public float test(SequenceCorpusReader reader, PrintWriter output) {
        // Count hits/misses
        int hits = 0;
        int misses = 0;

        // Sequences
        for (Sequence seq : reader) {
            // Get labels and features
            String[] labels = seq.getLabels();

            // Predict
            String[] predictions = predict(seq);

            // Write output if needed
            if (output != null) {
                for (int i = 0; i < seq.size(); i++) {
                    output.write(seq.get(i).token + "/" + predictions[i]);
                    // Write a space if we're not at the end
                    if (i < seq.size() - 1) {
                        output.write(" ");
                    }
                }
                output.println();
            }

            // Count accuracy
            if (labels != null) {
                for (int j = 0; j < labels.length; j++) {
                    if (labels[j].equals(predictions[j])) {
                        hits += 1;
                    } else {
                        misses += 1;
                    }
                }
            }
        }

        return (float) hits / (hits + misses);
    }

    /**
     * Update model based on current predictions. Update is performed exactly as specified in
     * Collins 2002.
     *
     * @param predictions predicted labels
     * @param labels correct labels
     * @param seq sequence to predict
     * @param timestamp training timestamp
     * @param debug whether to output debug information
     */
    private void update(String[] predictions, String[] labels, Sequence seq,
                        int timestamp, boolean debug) {
        if (debug && !Arrays.equals(predictions, labels)) {
            System.out.println("Pred: " + Arrays.toString(predictions));
            System.out.println("Gold: " + Arrays.toString(labels));
        }
        // Structure of maps is like weights
        List<List<String>> predFeatureList = featureSet.generateAllSequenceFeatures(seq, predictions);
        List<List<String>> goldFeatureList = featureSet.generateAllSequenceFeatures(seq, labels);
        Map<String, Map<String, Integer>> predCounts = countFeatures(predictions, predFeatureList);
        Map<String, Map<String, Integer>> goldCounts = countFeatures(labels, goldFeatureList);

        // Update counts based on the union of (label, feature) pairs
        for (String label : allLabels) {
            Map<String, Integer> predLabelCounts = predCounts.get(label);
            Map<String, Integer> goldLabelCounts = goldCounts.get(label);
            Map<String, Double> labelWeights = weights.get(label);
            Map<String, Double> labelTotals = totalWeights.get(label);
            Map<String, Integer> labelUpdates = lastUpdates.get(label);
            // Skip if both are null. This occurs if a label appears in neither
            // the gold or predicted labels.
            if (predLabelCounts == null && goldLabelCounts == null) {
                continue;
            }

            Set<String> allFeatures = new THashSet<String>();
            // Defend against a label not appearing in pred/gold
            if (predLabelCounts != null) {allFeatures.addAll(predLabelCounts.keySet());}
            if (goldLabelCounts != null) {allFeatures.addAll(goldLabelCounts.keySet());}
            // Compare the counts for each feature
            for (String feature : allFeatures) {
                // Defend against label not being in either gold/predictions
                int predCount = predLabelCounts != null ? predLabelCounts.getOrDefault(feature, 0) : 0;
                int goldCount = goldLabelCounts != null ? goldLabelCounts.getOrDefault(feature, 0) : 0;
                // Update on difference
                if (predCount != goldCount) {
                    int delta = goldCount - predCount;
                    double currentWeight = labelWeights.getOrDefault(feature, 0.0);

                    // Add to averaged total using the current value. Can skip
                    // update if the weight was zero.
                    if (currentWeight != 0) {
                        // The feature may not be in the totals if this is the second update
                        // ever for the feature
                        double lastTotal = labelTotals.getOrDefault(feature, 0.0);
                        int lastUpdate = labelUpdates.get(feature);
                        int timeDelta = timestamp - lastUpdate;
                        labelTotals.put(feature, lastTotal + timeDelta * currentWeight);
                    }
                    // Mark current timestamp as last update
                    labelUpdates.put(feature, timestamp);

                    // Update the weight
                    labelWeights.put(feature, currentWeight + delta);
                    if (debug) {
                        System.out.println("Update: " + feature + " (" + label + ") " +
                                currentWeight + " " + delta);
                    }
                }
            }
        }
    }

    /**
     * Predict the labels for the current sequence. If we are training, averaged weights will
     * never be used.
     * @param seq the sequence to predict for
     * @return the predicted labels
     */
    private String[] predict(Sequence seq) {
        return greedy ? greedyPredict(seq) : viterbiPredict(seq);
    }

    private String[] viterbiPredict(Sequence seq) {
        // If we're in training, never use averaged weights
        int length = seq.size();

        // Path backpointers
        Map<String, String[]> path = new THashMap<String, String[]>();
        for (String label : allLabels) {
            path.put(label, new String[length]);
        }

        // State scores
        List<Map<String, Double>> labelScores = new ArrayList<Map<String, Double>>(length);

        // Score sequence
        for (int i = 0; i < length; i++) {
            // Create a map from states to scores at this position
            Map<String, Double> currScores = new THashMap<String, Double>();
            labelScores.add(i, currScores);

            // Get the token features at this position. These are not dependent on any hypothesized
            // labeling, so they only need to be computed once.
            List<String> tokenFeatures = featureSet.generateTokenFeatures(seq, i);

            // New path which will replace the old
            Map<String, String[]> newPath = new THashMap<String, String[]>();
            for (String label : allLabels) {
                // Score and path update
                if (i == 0) {
                    // Base case
                    // If needed, generate any label features based on the (empty) label history
                    List<String> features = tokenFeatures;
                    if (featureSet.hasLabelFeatures()) {
                        List<String> labelFeatures = featureSet.generateLabelFeatures(null, i);
                        features = CollectionsUtil.combineToList(tokenFeatures, labelFeatures);
                    }

                    // Score and update best scores for this label
                    double score = score(label, features);
                    currScores.put(label, score);

                    // Create the base case path for this label
                    String[] labelPath = new String[length];
                    labelPath[i] = label;
                    newPath.put(label, labelPath);
                } else {
                    // Inductive case
                    // Get previous scores and create map between previous labels and new scores
                    // based on those labels
                    Map<String, Double> prevScores = labelScores.get(i - 1);
                    Map<String, Double> lastLabelScores = new THashMap<String, Double>();

                    // Find the best previous label for this label at this location
                    for (String lastLabel : allLabels) {
                        // Labels so far: best path to this label
                        String[] labels = path.get(lastLabel);

                        // If needed, generate label features for the best path history and merge
                        List<String> features = tokenFeatures;
                        if (featureSet.hasLabelFeatures()) {
                            List<String> labelFeatures = featureSet.generateLabelFeatures(labels, i);
                            features = CollectionsUtil.combineToList(tokenFeatures, labelFeatures);
                        }

                        // Score and update best scores for this label
                        double score = score(label, features);
                        lastLabelScores.put(lastLabel, prevScores.get(lastLabel) + score);
                    }
                    // Find the previous label that leads to the best current score
                    String bestPrevLabel = CollectionsUtil.getKeyWithMaxValue(lastLabelScores);
                    double bestPrevScore = lastLabelScores.get(bestPrevLabel);

                    // Verify that the backpointer is valid and we are not overwriting
                    String[] bestPath = Arrays.copyOf(path.get(bestPrevLabel), length);
                    if (bestPath[i] != null || bestPath[i - 1] == null) {
                        throw new RuntimeException("Corrupt path in decoding");
                    }

                    // Update the path
                    bestPath[i] = label;
                    newPath.put(label, bestPath);
                    currScores.put(label, bestPrevScore);
                }
            }
            // Update path
            path = newPath;
        }

        // Choose the best path
        double maxScore = Double.NEGATIVE_INFINITY;
        String maxLabel = "";
        for (String label : allLabels) {
            double labelScore = labelScores.get(length - 1).get(label);
            if (labelScore > maxScore) {
                maxScore = labelScore;
                maxLabel = label;
            }
        }
        if (maxLabel.isEmpty()) {
            throw new RuntimeException("Predict produced an empty label");
        }

        // Return the path for the best label
        return path.get(maxLabel);
    }

    /**
     * Score a label at the specified point in the sequence
     * @param label the (hypothesized) label to use for scoring
     * @param features the features to use
     * @return the score for <features, label, prevLabels>
     */
    private double score(String label, List<String> features) {
        Map<String, Double> labelWeights = finalWeights.get(label);
        double score = 0;
        for (String feature : features) {
            score += labelWeights.getOrDefault(feature, 0.0);
        }
        return score;
    }

    private String[] greedyPredict(Sequence seq) {
        String[] predictions = new String[seq.size()];
        // Loop over tokens
        for (int i = 0; i < seq.size(); i++) {
            // Initialize
            double bestScore = Double.NEGATIVE_INFINITY;
            String bestLabel = "";

            // Get all features for this position
            List<String> features = featureSet.generateAllFeatures(seq, predictions, i);

            // Do a greedy decode checking all labels
            for (String label : allLabels) {
                // Compute score for this label at this position
                double score = score(label, features);

                // Update score if better
                if (score > bestScore) {
                    bestLabel = label;
                    bestScore = score;
                }
            }
            if (bestLabel.isEmpty()) {
                throw new RuntimeException("Predict produced an empty label");
            }
            // Store prediction
            predictions[i] = bestLabel;
        }
        return predictions;
    }

    /** Count the occurrences of (label, feature) pairs in a feature sequence.
     *
     * @param labels all labels
     * @param features the feature sequence
     * @return a nested map of the structure (label -> (feature -> count))
     */
    private static Map<String, Map<String, Integer>> countFeatures(
            String[] labels, List<List<String>> features) {
        Map<String, Map<String, Integer>> counts = new THashMap<String, Map<String, Integer>>();
        // Count each (label, feature) pair
        for (int i = 0; i < labels.length; i++) {
            // Get the label counts, creating a new map if needed
            String label = labels[i];
            if (!counts.containsKey(label)) {
                counts.put(label, new THashMap<String, Integer>());
            }
            Map<String, Integer> labelCounts = counts.get(label);

            // Count each feature
            for (String feature : features.get(i)) {
                // Get count
                int count = labelCounts.getOrDefault(feature, 0);
                // Add to count
                labelCounts.put(feature, count + 1);
            }
        }
        return counts;
    }
}
