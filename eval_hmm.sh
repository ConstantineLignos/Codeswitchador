#!/bin/sh -e
java -Xmx1g -cp target//Codeswitchador-0.9-SNAPSHOT-jar-with-dependencies.jar org.lignos.nlp.codeswitching.HMMCodeswitchadorEval experiments/training_size.tsv data/twothirds/test.txt perf/twothirds models/tweets_multilingual_eng_wordlist_v2.tsv models/tweets_multilingual_spa_wordlist_v2.tsv
