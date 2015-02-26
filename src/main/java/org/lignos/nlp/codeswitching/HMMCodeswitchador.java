package org.lignos.nlp.codeswitching;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import gnu.trove.map.hash.THashMap;
import org.lignos.nlp.codeswitching.hmm.*;
import org.lignos.nlp.sequence.Sequence;
import org.lignos.nlp.sequence.TokenState;

import java.io.IOException;
import java.util.*;

public class HMMCodeswitchador {
    // TODO: Make the names of states not constants
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
	private static void makeHMM(String englishWordlistPath, String spanishWordlistPath,
			String trainPath, String testPath) throws IOException {
		// Load up training data
		TrainingCorpus train = new TrainingCorpus(trainPath, false);

        // Load wordlist probabilities
        TokenCounts englishCounts = new TokenCounts(englishWordlistPath);
        TokenCounts spanishCounts = new TokenCounts(spanishWordlistPath);
        // Map states to their emissions pdfs
        Map<String, TokenCounts> emissionsMap = new THashMap<String, TokenCounts>();
        // The order of these must match the order in STATES
        emissionsMap.put(STATES[0], englishCounts);
        emissionsMap.put(STATES[1], spanishCounts);

        // Estimate state probabilities and also freshen emissions based on the training data
        StateProbabilities stateProbs = train.computeStateProbabilities(STATES, NONSTATES, emissionsMap);

        // Create PDFs from the TokenCounts
        List<WordlistObs> pdfs = new ArrayList<WordlistObs>();
        WordlistObs english = new WordlistObs(englishCounts);
        WordlistObs spanish = new WordlistObs(spanishCounts);
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
		Corpus test = new Corpus(testPath, false);
		// Count it up
		int hits = 0;
		int misses = 0;
		for (Sequence utt : test) {
			// Get observations for every word
			List<TokenObservation> uttObs = new LinkedList<TokenObservation>();
			for (TokenState ts : utt) {
                uttObs.add(WordlistObs.getObs(ts.token));
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
                        if (predState.equals(goldState)) {
                            hits += 1;
                        } else {
                            misses += 1;
                        }
                    }
				}
			}
		}
		System.out.println("Hits: " + hits);
		System.out.println("Misses: " + misses);
		System.out.println("Accuracy: " + hits / ((float) hits + misses));
	}

    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            System.err.println("Usage: HMMCodeswitchador englishwordlist spanishwordlist trainpath testpath");
            System.exit(1);
        }
        makeHMM(args[0], args[1], args[2], args[3]);
    }
}

