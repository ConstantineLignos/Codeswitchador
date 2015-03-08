#!/bin/sh -e
java -Xmx1g -jar target/Codeswitchador-0.9-SNAPSHOT-jar-with-dependencies.jar data/nd/train_clean.txt data/nd/test_clean.txt params/features_all.txt 15 test_predicted.txt
