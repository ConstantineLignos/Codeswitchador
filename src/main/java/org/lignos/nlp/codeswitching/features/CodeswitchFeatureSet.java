package org.lignos.nlp.codeswitching.features;

import org.lignos.nlp.sequence.LabelSequenceFeatureGenerator;
import org.lignos.nlp.sequence.SequenceFeatureSet;
import org.lignos.nlp.sequence.TokenSequenceFeatureGenerator;

import java.util.LinkedList;
import java.util.List;

/**
 * A feature set for codeswitching
 */
public class CodeswitchFeatureSet extends SequenceFeatureSet {

    static final String SUFFIX = "suffix";
    static final String TOKEN = "token";
    static final String LENGTH = "length";
    static final String TAG = "tag";
    static final String CHAR = "char";
    static final String TAGHIST = "taghist";

    /**
     * Create a feature set that uses the specified generators
     *
     * @param tokenGenerators token feature generators to use
     * @param labelGenerators label feature generators to use
     */
    public CodeswitchFeatureSet(List<TokenSequenceFeatureGenerator> tokenGenerators,
                                List<LabelSequenceFeatureGenerator> labelGenerators) {
        super(tokenGenerators, labelGenerators);
    }

    public static CodeswitchFeatureSet createFeatureSet(List<String> featureGeneratorNames) {
        List<TokenSequenceFeatureGenerator> tokenGenerators = new LinkedList<TokenSequenceFeatureGenerator>();
        List<LabelSequenceFeatureGenerator> labelGenerators = new LinkedList<LabelSequenceFeatureGenerator>();
        for (String name : featureGeneratorNames) {
            if (name.equals(SUFFIX)) {
                tokenGenerators.add(new SuffixFeatureGenerator(1, 4, 3));
            } else if (name.startsWith(TOKEN)) {
                int index = Integer.parseInt(name.substring(TOKEN.length()));
                tokenGenerators.add(new TokenContextFeatureGenerator(index));
            } else if (name.startsWith(TAGHIST)) { // This check must precede TAG as it's more specific
                int index = Integer.parseInt(name.substring(TAGHIST.length()));
                labelGenerators.add(new TagHistoryFeatureGenerator(index));
            } else if (name.startsWith(TAG)) {
                int index = Integer.parseInt(name.substring(TAG.length()));
                labelGenerators.add(new TagContextFeatureGenerator(index));
            } else if (name.startsWith(CHAR)) {
                int index = Integer.parseInt(name.substring(CHAR.length()));
                tokenGenerators.add(new CharacterFeatureGenerator(index));
            } else if (name.equals(LENGTH)) {
                tokenGenerators.add(new LengthFeatureGenerator());
            } else {
                throw new UnknownFeatureGeneratorException("Unknown feature generator: " + name);
            }
        }
        return new CodeswitchFeatureSet(tokenGenerators, labelGenerators);
    }
}
