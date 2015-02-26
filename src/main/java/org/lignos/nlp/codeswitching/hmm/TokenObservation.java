package org.lignos.nlp.codeswitching.hmm;

import be.ac.ulg.montefiore.run.jahmm.Observation;

import java.text.NumberFormat;

/**
 * Represent the observation of an individual token.
 */
public class TokenObservation extends Observation {
	/** The string representation of the token. */
    public final String token;

	public TokenObservation(String token) {
		this.token = token;
	}

	@Override
	public String toString(NumberFormat arg0) {
		return toString();
	}

	@Override
	public String toString() {
		return token;
	}
}
