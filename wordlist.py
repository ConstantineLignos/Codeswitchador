#!/usr/bin/env python
"""
Provides tools for creating wordlists

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

import sys
import codecs
import argparse
from collections import Counter
from string import punctuation

from tools.scalereader import JerboaTokenReader, JERBOA_NOTAG

PUNC = set(punctuation)


def _good_token(token):
    """
    Return whether a token is worth putting in a wordlist.

    Return False if the token:
    1. Starts with punctuation
    2. Is a digit
    """
    return not(token[0] in PUNC or token.isdigit())


def _get_tokens(infile, jerboa=False):
    """Yield lowercased tokens from a file worthy of appearing in a wordlist."""
    if jerboa:
        # Read in the tokens from Jerboa
        for tokens, _ in JerboaTokenReader(infile):
            for token, tag, _ in tokens:
                # Skip tokens we don't like
                if tag == JERBOA_NOTAG and _good_token(token):
                    yield token.lower()
    else:
        # Just split each line as simple tokenization
        for line in infile:
            for token in line.split():
                if _good_token(token):
                    yield token.lower()


def make_counter(infile, jerboa):
    """Make a counter from the tokens of a file."""
    return Counter(_get_tokens(infile, jerboa))


def load_counts(infile):
    """Load counts from a file."""
    # Because of the way Counters are initialized, the obvious list comprehension
    # can't be used here
    counts = Counter()
    for line in infile:
        word, count = line.split("\t")
        counts[word] = int(count)

    return counts


def output_counts(counts, outfile):
    """Output pre-sorted (item, count) pairs to stdout."""
    for word, count in counts:
        print >> outfile, u"{0}\t{1}".format(word, count)


def main():
    """Create a wordlist from a data file."""
    parser = argparse.ArgumentParser(description=main.__doc__)
    parser.add_argument('file', nargs='?', help='input file in UTF-8 format, or omit for stdin')
    parser.add_argument('-j', '--jerboa', action='store_true',
                        help='input is Jerboa full tokenizer output')
    args = parser.parse_args()

    # Get file or stdin input as utf-8
    infile = (codecs.open(args.file, 'Ur', 'utf_8') if args.file else
              codecs.getreader('utf-8')(sys.stdin))

    # Do the counting
    counts = make_counter(infile, args.jerboa)

    # Output
    output_counts(counts.most_common(), codecs.getwriter('utf-8')(sys.stdout))


if __name__ == "__main__":
    main()
