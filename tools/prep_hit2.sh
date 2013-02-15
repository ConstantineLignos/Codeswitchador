#!/bin/sh
# This script is an example for prepping data out of Mechanical Turk.
# Unless you have the data, this won't make much sense.
DATA='../data'
HIT=$DATA/results_of_name_titles_edits.tsv
REMOVED=$DATA/cs_hit2.tsv
CUT=$DATA/cs_hit2.txt
FILTERED=$DATA/cs_hit2_clean.txt
CS=$DATA/cs_hit2_clean_cs.txt
python remove_annotation_edits.py < $HIT > $REMOVED
# Extract tweet and remove header
cut -f 3 < $REMOVED | tail -n +2 > $CUT
python filter_labels.py < $CUT > $FILTERED
python filter_cs.py < $FILTERED > $CS
