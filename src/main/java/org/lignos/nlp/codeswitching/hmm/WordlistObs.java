package org.lignos.nlp.codeswitching.hmm;

import be.ac.ulg.montefiore.run.jahmm.Opdf;

import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WordlistObs implements Opdf<TokenObservation> {
	private static final long serialVersionUID = 1L;
	private TokenCounts words;
	// There's one set of observations for all wordlists, so the same word in multiple languages
	// is still the same observation
	private static Map<String, TokenObservation> observations;
	private static final String UNKNOWN = "UNK";
	private static TokenObservation unknownWord;

	public WordlistObs(TokenCounts tokenCounts) {
		words = tokenCounts;
		// Create an observation for every word
		observations = new HashMap<String, TokenObservation>();
		for (String w : tokenCounts.tokenSet()) {
			if (!observations.containsKey(w)) {
				observations.put(w, new TokenObservation(w));
			}
		}
		// Create an observation for unknown words
		unknownWord = new TokenObservation(UNKNOWN);
		tokenCounts.incrementCount(UNKNOWN, 1);
	}

	public static TokenObservation getObs(String word) {
		TokenObservation obs = observations.get(word.toLowerCase());
		return obs != null ? obs : unknownWord;
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
