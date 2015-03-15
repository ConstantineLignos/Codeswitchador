#!/usr/bin/env python
"""Remove all diacritics from the tokens of a corpus."""

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

from unidecode import unidecode

from eval_codeswitch import split_token


def preprocess(input_path, output_path):
    """Process the file at input_path, writing to output_path."""
    input_file = codecs.open(input_path, 'U', 'utf-8')
    output_file = open(output_path, 'w')

    linenum = 0
    for line in input_file:
        linenum += 1

        # Split up the line
        splits = line.split()

        # Extract and transform token and tag pairs
        token_tags = [split_token(item, False) for item in splits]
        try:
            # To ensure that it is still a single token, we need to
            # replace any spaces created.
            new_token_tags = [(unidecode(token).replace(' ', '_'), tag)
                              for token, tag in token_tags]
        except ValueError as err:
            print('Error reading line {}: {}'.format(linenum, err),
                  file=sys.stderr)
            continue

        # Write output
        output = ' '.join('/'.join((token, tag))
                          for token, tag in new_token_tags if token)
        print(output, file=output_file)


def main():
    """Parse arguments and call the preprocessor."""
    parser = argparse.ArgumentParser(
        'Preprocess a tagged file by removing diacritics.')
    parser.add_argument('input', help='input file (UTF-8 format)')
    parser.add_argument('output', help='output file (ascii format)')
    args = parser.parse_args()
    preprocess(args.input, args.output)


if __name__ == '__main__':
    main()
