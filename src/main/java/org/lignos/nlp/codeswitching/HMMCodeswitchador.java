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

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import gnu.trove.map.hash.THashMap;
import org.lignos.nlp.codeswitching.hmm.*;
import org.lignos.nlp.sequence.Sequence;
import org.lignos.nlp.sequence.TokenState;

import java.io.IOException;
import java.util.*;

public class HMMCodeswitchador {
    // TODO: Make the names of states not constants and generalize constructor
	private static final String[] STATES = {"e", "s"};
	private static final String[] NONSTATES = {"n", "o"};

	/**
	 * Make an HMM for code switching.
	 * @param englishWordlistPath Path to the English wordlist.
	 * @param spanishWordlistPath Path to the Spanish wordlist.
     * @param trainPath Training data path
     * @param testPath Test data path
	 * @throws IOException When one of the wordlists or corpora can't be read.
	 */
	public static float[] evalHMM(String englishWordlistPath, String spanishWordlistPath,
                                  String trainPath, String testPath) {
		// Load up training data
        System.out.println("Loading training data...");
        TrainingCorpus train = null;
        try {
            train = new TrainingCorpus(trainPath, false);
        } catch (IOException e) {
            System.err.println("Could not open input file: " + trainPath);
            System.exit(1);
        }

        // Load wordlist probabilities
        TokenCounts englishCounts = null;
        try {
            englishCounts = new TokenCounts(englishWordlistPath);
        } catch (IOException e) {
            System.err.println("Could not open input file: " + englishWordlistPath);
            System.exit(1);
        }
        TokenCounts spanishCounts = null;
        try {
            spanishCounts = new TokenCounts(spanishWordlistPath);
        } catch (IOException e) {
            System.err.println("Could not open input file: " + spanishWordlistPath);
            System.exit(1);
        }
        // Map states to their emissions pdfs
        Map<String, TokenCounts> emissionsMap = new THashMap<String, TokenCounts>();
        // The order of these must match the order in STATES
        emissionsMap.put(STATES[0], englishCounts);
        emissionsMap.put(STATES[1], spanishCounts);

        // Estimate state probabilities and also update emissions based on the training data
        System.out.println("Estimating parameters...");
        StateProbabilities stateProbs = train.computeStateProbabilities(STATES, NONSTATES, emissionsMap);

        // Create PDFs from the TokenCounts
        List<WordlistObs> pdfs = new ArrayList<WordlistObs>();
        Map<String, TokenObservation> observations = new THashMap<String, TokenObservation>();
        WordlistObs english = new WordlistObs(englishCounts, observations);
        WordlistObs spanish = new WordlistObs(spanishCounts, observations);
        // The order of these must match the order in STATES
        pdfs.add(english);
        pdfs.add(spanish);

		// Uninformative initializations available if you want to test them
		//double[] pi = {.5, .5};
        //double[][] a = new double[][] {{0.5, 0.5}, {0.5, 0.5}};

        double[] pi = stateProbs.pi;
        double[][] a = stateProbs.a;
		Hmm<TokenObservation> h = new Hmm<TokenObservation>(pi, a, pdfs);
		System.out.println(h);

        // Make a set of nonStates for fast checking
        Set<String> nonStates = new HashSet<String>(Arrays.asList(NONSTATES));

        // Label the test corpus
        System.out.println("Evaluating...");
        Corpus test = null;
        try {
            test = new Corpus(testPath, false);
        } catch (IOException e) {
            System.err.println("Could not open input file: " + testPath);
            System.exit(1);
        }
        // Count it up
		int hits = 0;
		int misses = 0;
        int oovHits = 0;
        int oovMisses = 0;
		for (Sequence utt : test) {
			// Get observations for every word
			List<TokenObservation> uttObs = new LinkedList<TokenObservation>();
			for (TokenState ts : utt) {
                // Since the observation map is shared between labels, it doesn't matter where we get the observation
                uttObs.add(english.getObs(ts.token));
			}

			// Get the state sequence and get it back into string
			int[] predStates = h.mostLikelyStateSequence(uttObs);
			String[] stateNames = new String[predStates.length];
			for (int i = 0; i < predStates.length; i++) {
                TokenState goldToken = utt.get(i);

				// Use the non states if they were in the input
                if (nonStates.contains(goldToken.state)) {
                    stateNames[i] = goldToken.state;
				}
				// Otherwise, compare the states
				else {
					String predState = STATES[predStates[i]];
                    String goldState = goldToken.state;
                    // Skip evaluation if the original token has a comment marked
                    if (goldToken.comment == null) {
                        // Normal scoring
                        if (predState.equals(goldState)) {
                            hits += 1;
                        } else {
                            misses += 1;
                        }
                        // If it was OOV, record that
                        // Since the observation map is shared between labels, it doesn't matter where we get the observation
                        if (english.isOov(goldToken.token)) {
                            if (predState.equals(goldState)) {
                                oovHits += 1;
                            } else {
                                oovMisses += 1;
                            }
                        }
                    }
				}
			}
		}
        float accuracy = hits / ((float) hits + misses);
        float oovRate = (oovHits + oovMisses) / ((float) hits + misses);
        float oovAccuracy = oovHits / ((float) oovHits + oovMisses);
		System.out.println("Hits: " + hits);
		System.out.println("Misses: " + misses);
        System.out.println("Accuracy: " + accuracy);
        System.out.println();
        System.out.println("OOV rate: " + oovRate);
        System.out.println("OOV Accuracy: " + oovAccuracy);
        return new float[] {accuracy, oovAccuracy, oovRate};
	}

    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Usage: HMMCodeswitchador englishwordlist spanishwordlist trainpath testpath");
            System.exit(1);
        }
        evalHMM(args[0], args[1], args[2], args[3]);
    }
}

