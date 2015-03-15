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
import org.lignos.nlp.sequence.TokenSequence;
import org.lignos.nlp.sequence.TokenSequenceCorpusReader;
import org.lignos.nlp.sequence.TokenTag;

import java.io.IOException;
import java.util.*;

public class HMMCodeswitchador {

	/**
	 * Make an HMM for code switching.
     * @param lang1 Tag for language 1
     * @param wordlist1Path Path to the wordlist for language 1
     * @param lang2 Tag for language 2
     * @param wordlist2Path Path to the wordlist for language 2
     * @param trainPath Training data path
     * @param testPath Test data path
	 * @throws IOException if one of the wordlists or corpora cannot be read.
	 */
	public static float[] evalHMM(String lang1, String wordlist1Path,
                                  String lang2, String wordlist2Path,
                                  String trainPath, String testPath) {
        // Set up language tags as states
        String[] states = {lang1, lang2};

		// Load up training data
        System.out.println("Loading training data...");
        TrainingCorpus train = null;
        try {
            train = new TrainingCorpus(trainPath, Constants.IGNORE_TAGS);
        } catch (IOException e) {
            System.err.println("Could not open input file: " + trainPath);
            System.exit(1);
        }

        // Load wordlist probabilities
        TokenCounts lang1Counts = null;
        try {
            lang1Counts = new TokenCounts(wordlist1Path);
        } catch (IOException e) {
            System.err.println("Could not open input file: " + wordlist1Path);
            System.exit(1);
        }
        TokenCounts lang2Counts = null;
        try {
            lang2Counts = new TokenCounts(wordlist2Path);
        } catch (IOException e) {
            System.err.println("Could not open input file: " + wordlist2Path);
            System.exit(1);
        }
        // Map states to their emissions pdfs
        Map<String, TokenCounts> emissionsMap = new THashMap<String, TokenCounts>();
        // The order of these must match the order in states
        emissionsMap.put(lang1, lang1Counts);
        emissionsMap.put(lang2, lang2Counts);

        // Estimate state probabilities and also update emissions based on the training data
        System.out.println("Estimating parameters...");
        StateProbabilities stateProbs = train.computeStateProbabilities(states, Constants.IGNORE_TAGS, emissionsMap);

        // Create PDFs from the TokenCounts
        List<WordlistObs> pdfs = new ArrayList<WordlistObs>();
        Map<String, TokenObservation> observations = new THashMap<String, TokenObservation>();
        WordlistObs english = new WordlistObs(lang1Counts, observations);
        WordlistObs spanish = new WordlistObs(lang2Counts, observations);
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
        Set<String> nonStates = new HashSet<String>(Arrays.asList(Constants.IGNORE_TAGS));

        // Label the test corpus
        System.out.println("Evaluating...");
        TokenSequenceCorpusReader test = null;
        try {
            test = new TokenSequenceCorpusReader(testPath, Constants.IGNORE_TAGS);
        } catch (IOException e) {
            System.err.println("Could not open input file: " + testPath);
            System.exit(1);
        }
        // Count it up
		int hits = 0;
		int misses = 0;
        int oovHits = 0;
        int oovMisses = 0;
		for (TokenSequence utt : test) {
			// Get observations for every word
			List<TokenObservation> uttObs = new LinkedList<TokenObservation>();
			for (TokenTag ts : utt) {
                // Since the observation map is shared between labels, it doesn't matter where we get the observation
                uttObs.add(english.getObs(ts.token));
			}

			// Get the state sequence and get it back into string
			int[] predStates = h.mostLikelyStateSequence(uttObs);
			String[] stateNames = new String[predStates.length];
			for (int i = 0; i < predStates.length; i++) {
                TokenTag goldToken = utt.get(i);

				// Use the non states if they were in the input
                if (nonStates.contains(goldToken.tag)) {
                    stateNames[i] = goldToken.tag;
				}
				// Otherwise, compare the states
				else {
					String predState = states[predStates[i]];
                    String goldState = goldToken.tag;
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
        if (args.length != 6) {
            System.err.println("Usage: HMMCodeswitchador lang1 wordlist1 lang2 wordlist2 trainpath testpath");
            System.exit(1);
        }
        evalHMM(args[0], args[1], args[2], args[3], args[4], args[5]);
    }
}

