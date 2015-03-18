#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""Reformat the data from a study of codeswitching.

The source of the data is:
D. Nguyen, A.S. Dogruöz. Word Level Language Identification in Online
Multilingual Communication. EMNLP 2013.
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

from __future__ import print_function
import sys
import codecs
import argparse

TAG_DELIM = '/'
TAG_MAP = {
    'tr': 'tur',
    'nl': 'nld',
    'skip': 'zxx'
}


def preprocess(input_path, output_path):
    """Process the file at input_path, writing to output_path."""
    input_file = codecs.open(input_path, 'U', 'utf-8')
    output_file = codecs.open(output_path, 'w', 'utf-8')

    tokens = []
    tags = []
    for line in input_file:
        line = line.strip()

        # Write out any tokens/tags if the line is blank
        if not line and tokens:
            writeline(tokens, tags, output_file)
            tags = []
            tokens = []
            continue

        # Split up the line
        try:
            _, tag, token = line.split('\t')
        except ValueError:
            print('Could not parse line: {!r}'.format(line), file=sys.stderr)

        # Add to the buffer. Replace any spaces in the token with
        # _. All instances of this appear to be formatting/link tokens
        tags.append(TAG_MAP[tag])
        tokens.append(token.replace(' ', '_'))

    # Write out any remnants
    if tokens:
        writeline(tokens, tags, output_file)


def writeline(tokens, tags, outfile):
    """Write a line of the specified tokens/tags to outfile."""
    if len(tokens) != len(tags):
        raise ValueError('Tokens and tags not of matching length: '
                         + str(tokens) + ' ' + str(tags))

    # Join each token and tag together
    out_tokens = [token + TAG_DELIM + tag for token, tag in zip(tokens, tags)]
    print(' '.join(out_tokens), file=outfile)


def main():
    """Parse arguments and call the preprocessor."""
    parser = argparse.ArgumentParser('Transform data from the ND study format '
                                     'to standard tagging format.')
    parser.add_argument('input', help='input file (UTF-8 format)')
    parser.add_argument('output', help='output file (UTF-8 format)')
    args = parser.parse_args()
    preprocess(args.input, args.output)


if __name__ == '__main__':
    main()
