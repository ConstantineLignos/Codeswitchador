#!/bin/sh -e
java -Xmx1g -cp target//Codeswitchador-0.9-SNAPSHOT-jar-with-dependencies.jar org.lignos.nlp.codeswitching.HMMCodeswitchadorEval experiments/training_size_nodia.tsv data/twothirds_nodia/test.txt perf/twothirds_nodia eng models/tweets_multilingual_eng_wordlist_v2_nodia.tsv spa models/tweets_multilingual_spa_wordlist_v2_nodia.tsv
