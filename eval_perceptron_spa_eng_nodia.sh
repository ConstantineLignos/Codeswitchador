#!/bin/sh -e
java -Xmx2g -cp target/Codeswitchador-0.9-SNAPSHOT-jar-with-dependencies.jar org.lignos.nlp.codeswitching.PerceptronCodeswitchadorEval experiments/training_size_nodia.tsv data/twothirds_nodia/test.txt perf/twothirds_nodia experiments/feature_sets.tsv 20
