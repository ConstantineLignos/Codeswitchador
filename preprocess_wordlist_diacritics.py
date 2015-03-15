#!/usr/bin/env python
"""Map entries in a wordlist to their non-diacritic forms."""

# Copyright 2015 Constantine Lignos
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

from __future__ import print_function
import sys
import codecs
import argparse
from collections import Counter

from unidecode import unidecode


def preprocess(input_path, output_path):
    """Process the file at input_path, writing to output_path."""
    input_file = codecs.open(input_path, 'U', 'utf-8')
    output_file = open(output_path, 'w')

    counts = Counter()
    linenum = 0
    for line in input_file:
        linenum += 1

        # Split up the line
        token, count = line.split()

        # Count if the token survived cleaning. To maintain
        # token-ness, any spaces created in the cleaning are replaced.
        clean_token = unidecode(token).replace(' ', '_')
        if clean_token:
            counts[clean_token] += int(count)

    # Write output
    for token, count in counts.most_common():
        print(token + '\t' + str(count), file=output_file)


def main():
    """Parse arguments and call the preprocessor."""
    parser = argparse.ArgumentParser(
        'Preprocess a wordlist by removing diacritics')
    parser.add_argument('input', help='input file (UTF-8 format)')
    parser.add_argument('output', help='output file (ascii format)')
    args = parser.parse_args()
    preprocess(args.input, args.output)


if __name__ == '__main__':
    main()
