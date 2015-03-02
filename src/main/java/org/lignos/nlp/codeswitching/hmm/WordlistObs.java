package org.lignos.nlp.codeswitching.hmm;

import be.ac.ulg.montefiore.run.jahmm.Opdf;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WordlistObs implements Opdf<TokenObservation> {
	private TokenCounts words;
	// There's one set of observations for all wordlists, so the same word in multiple languages
	// is still the same observation
	private Map<String, TokenObservation> observations;

    // The singleton unknown token
    private static final String UNKNOWN = "UNK";
	private static TokenObservation unknownWord;

    /**
     * Create a new emission distribution using token counts. All emission distributions should share a single
     * observation
     *
     * @param tokenCounts
     * @param observations
     */
	public WordlistObs(TokenCounts tokenCounts, Map<String, TokenObservation> observations) {
		words = tokenCounts;
		// Create an observation for every word
		this.observations = observations;
		for (String w : tokenCounts.tokenSet()) {
			if (!observations.containsKey(w)) {
				observations.put(w, new TokenObservation(w));
			}
		}
		// Create an observation for unknown words
		unknownWord = new TokenObservation(UNKNOWN);
		tokenCounts.incrementCount(UNKNOWN, 1);
	}

    /**
     * Get the observation associated with a token
     * @param word the token
     * @return the observation
     */
	public TokenObservation getObs(String word) {
		TokenObservation obs = observations.get(word.toLowerCase());
		return obs != null ? obs : unknownWord;
	}


    /**
     * Return whether this token is out-of-vocabulary
     * @param token the token
     * @return true if it is out of vocabulary
     */
    public boolean isOov(String token) {
        return observations.get(token.toLowerCase()) == null;
    }

	@Override
	public void fit(TokenObservation... arg0) {
		throw new RuntimeException();
	}


	@Override
	public void fit(TokenObservation[] arg0, double[] arg1) {
		throw new RuntimeException();
	}


	@Override
	public TokenObservation generate() {
		throw new RuntimeException();
	}

	@Override
	public double probability(TokenObservation obs) {
		return words.getFreq(obs.toString());
	}

	@Override
	public String toString(NumberFormat arg0) {
		return toString();
	}

	@Override
	public WordlistObs clone(){
		throw new RuntimeException();
	}

	@Override
	public void fit(Collection<? extends TokenObservation> arg0) {
		throw new RuntimeException();
	}

	@Override
	public void fit(Collection<? extends TokenObservation> arg0, double[] arg1) {
		throw new RuntimeException();
	}

	@Override
	public String toString() {
		return ("Wordlist: " + words.nTypes() + " types, " + words.nTokens() + " tokens, " + observations.size() +
				" observations");
	}

}
