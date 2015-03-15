#!/bin/sh -e
java -Xmx8g -jar target/Codeswitchador-0.9-SNAPSHOT-jar-with-dependencies.jar tokenization/split/train.txt tokenization/split/test.txt params/features_all_notagcontext.txt 20 test_predicted.txt
