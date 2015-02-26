package org.lignos.nlp.codeswitching;

import org.lignos.nlp.sequence.SequenceEvaluator;

/**
 * Evaluate predicted labels.
 */
public class EvalOutput {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: EvalOutput gold predicted");
            System.exit(1);
        }
        String goldPath = args[0];
        String predPath = args[1];

        SequenceEvaluator eval = SequenceEvaluator.getFromPaths(goldPath, predPath, null);
        eval.eval(true);
        eval.printResults();
    }
}
