package org.lignos.nlp.codeswitching.hmm;

public class StateProbabilities {
	public final double[] pi;
	public final double[][] a;

	public StateProbabilities(double[] pi, double[][] a) {
		this.pi = pi;
		this.a = a;
	}
}
