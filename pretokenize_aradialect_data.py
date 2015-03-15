#!/usr/bin/env python
"""Do a pre-tokenization pass for Arabic twitter data.

TODO: Give data source tested on
"""

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

EXCLUDE_CHARS = ['@', '#']

def exclude_token(token):
    """Return whether a token should be excluded."""
    return token.startswith('http') or any(char in token for char in EXCLUDE_CHARS)


def preprocess(input_path, output_path):
    """Process the file at input_path, writing to output_path."""
    input_file = codecs.open(input_path, 'U', 'utf-8')
    output_file = codecs.open(output_path, 'w', 'utf-8')

    linenum = 0
    for line in input_file:
        linenum += 1

        # Split up the line
        tokens = line.split()

        # Remove any tokens
        clean_tokens = [token for token in tokens if not exclude_token(token)]

        # Write it out
        if clean_tokens:
            print(' '.join(clean_tokens), file=output_file)


def main():
    """Parse arguments and call the preprocessor."""
    parser = argparse.ArgumentParser('Prepare Arabic twitter data for tokenization.')
    parser.add_argument('input', help='input file (UTF-8 format)')
    parser.add_argument('output', help='output file (UTF-8 format)')
    args = parser.parse_args()
    preprocess(args.input, args.output)


if __name__ == '__main__':
    main()
