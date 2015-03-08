#!/bin/sh -eu
DATA=data/nd
python preprocess_nd_data.py $DATA/dev.txt $DATA/dev_clean.txt
python preprocess_nd_data.py $DATA/train.txt $DATA/train_clean.txt
python preprocess_nd_data.py $DATA/test.txt $DATA/test_clean.txt
