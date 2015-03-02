#!/bin/sh -e
java -Xmx2g -cp target//Codeswitchador-0.9-SNAPSHOT-jar-with-dependencies.jar org.lignos.nlp.codeswitching.PerceptronCodeswitchadorEval experiments/training_size.tsv data/twothirds/test.txt perf/twothirds experiments/feature_sets.tsv 20
