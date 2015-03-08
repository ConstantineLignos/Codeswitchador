#!/bin/sh -e
java -Xmx1g -jar target/Codeswitchador-0.9-SNAPSHOT-jar-with-dependencies.jar data/twothirds/train.txt data/twothirds/test.txt params/features_all.txt 20 test_predicted.txt
