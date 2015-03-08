#!/usr/bin/env python
"""
Provides tools for computing frequency ratios between wordlists.

Constantine Lignos, June 2012

"""

# Copyright 2012-2015 Constantine Lignos
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from __future__ import division
import sys
import codecs
import argparse
from math import log
from operator import itemgetter

from wordlist import load_counts


def _logprobratio(prob1, prob2):
    """Compute log probability ratio of prob1/prob2."""
    return log(prob1) / log(prob2)


def output_ratios(ratios, counter1, counter2, outfile):
    """Output pre-sorted (item, count) pairs to stdout."""
    for word, word_ratio in ratios:
        print >> outfile, u"{0}\t{1:.3f}\t{2}\t{3}".format(word, word_ratio, counter1[word], counter2[word])


def _lidstone_smooth(prob, smoothing, observations, outcomes):
    """Return a lidstone smoothed probability estimate."""
    return (prob + smoothing) / (observations + (smoothing * outcomes))


def _ratio(counter1, counter2, smoothing, min_freq=0):
    """
    Compute the logprobratio of the items in two counters.

    @ param counter1 Counter for numerator of ratio
    @ param counter2 Counter for denominator of ratio
    @ param smoothing Value for Lidstone smoothing to be applied over the word distribution
    @ param min_freq Minimum frequency for an item to included.
    """
    # Ratios dict to be returned
    ratios = {}

    # Set up for smoothing
    # The number of outcomes is the number of unique words across the two wordlists
    all_words = set(counter1)
    all_words.update(counter2)
    n_outcomes = len(all_words)
    # Total tokens per wordlist
    counter1_obs = sum(counter1.values())
    counter2_obs = sum(counter2.values())

    # First loop over counter1. You could conceiveably combine these loops into one
    # that first runs on counter1 then counter2, but then you'd have to keep track
    # of which one is the numerator, which makes for more work
    for item, count1 in counter1.items():
        # Filter below min_freq
        if count1 < min_freq:
            continue

        # Compute ratio
        count2 = counter2[item]
        ratios[item] = _logprobratio(_lidstone_smooth(count1, smoothing, counter1_obs, n_outcomes),
                                     _lidstone_smooth(count2, smoothing, counter2_obs, n_outcomes))

    # Rinse and repeat for counter2
    for item, count2 in counter2.items():
        # Skip items already counted and filter below min_freq
        if count2 < min_freq or item in ratios:
            continue

        # Compute ratio
        count1 = counter1[item]
        ratios[item] = _logprobratio(_lidstone_smooth(count1, smoothing, counter1_obs, n_outcomes),
                                     _lidstone_smooth(count2, smoothing, counter2_obs, n_outcomes))

    return ratios


def wordlist_ratio(wordlistfile1, wordlistfile2, smoothing, min_freq):
    """Return the word ratio and word counters for two wordlist files."""
    # Do the counting
    counts1 = load_counts(wordlistfile1)
    counts2 = load_counts(wordlistfile2)
    return (_ratio(counts1, counts2, smoothing, min_freq), counts1, counts2)


def prune_ratios(ratios, bad_words):
    """Remove an interable of bad_words from the ratio list."""
    for word in bad_words:
        ratios.pop(word, None)


def main():
    """Create a frequency ratio list from two data files."""
    parser = argparse.ArgumentParser(description=main.__doc__)
    parser.add_argument('smooth', type=float, help='amount of Lidstone smoothing to apply')
    parser.add_argument('mincount', type=int, help='minimum count in either language for a word')
    parser.add_argument('wordlist1', help='first wordlist file, in UTF-8 format')
    parser.add_argument('wordlist2', help='second wordlist file, in UTF-8 format')
    args = parser.parse_args()

    # Get file or stdin input as utf-8
    infile1 = codecs.open(args.wordlist1, 'Ur', 'utf_8')
    infile2 = codecs.open(args.wordlist2, 'Ur', 'utf_8')

    # Compute ratio
    ratios, counts1, counts2 = wordlist_ratio(infile1, infile2, args.smooth, args.mincount)

    # Output
    output_ratios(sorted(ratios.items(), key=itemgetter(1)), counts1, counts2,
                  codecs.getwriter('utf-8')(sys.stdout))


if __name__ == "__main__":
    main()
