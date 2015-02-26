package org.lignos.nlp.codeswitching.features;

/**
 * Exception to be thrown if an unknown feature generator is requested.
 */
public class UnknownFeatureGeneratorException extends RuntimeException {
    public UnknownFeatureGeneratorException(String msg) {super(msg);}
}
