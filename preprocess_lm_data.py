#!/usr/bin/env python
"""Reformat the data from a study of codeswitching.

The source of the data is:
Constantine Lignos and Mitchell Marcus. Toward web-scale analysis of
codeswitching. Poster at the 87th Annual Meeting of the Linguistic
Society of America, January 5, 2013.

The source data is of the format:
word1/tag word2/tag

The output is of the same format, except the tags are changed to a
more verbose form. The source tagset consisted of one letter for the
language (e: English, s: Spanish, o: Other, n: non-linguistic) and an
optional second letter for if the token is an entity (p: Person-like
(companies, brands, and products included), t: Title (of a work such
as a song, film, or book)). This is changed to a standard ISO 639-2
language code for the languages, a dashtag used for entities, and
"unk" used for other language tokens.

The mapping of languages is as follows:
       English(e): eng
       Spanish(s): spa
         Other(o): unk
Non-linguistic(n): zxx

Entities are mapped as follows:
Person(p): pe
 Title(t): ti
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

from eval_codeswitch import split_token

TAG_DASH = '-'
LANG_MAPPING = {
    'e': 'eng',
    's': 'spa',
    'o': 'unk',
    'n': 'zxx',
}
ENTITY_MAPPING = {
    'p': 'pe',
    't': 'ti',
}


def transform_tag(tag):
    """Transform a tag from the LM format to the desired one."""
    # If the length is not one or two, the tag is invalid
    if len(tag) not in (1, 2):
        raise ValueError('Invalid tag length: {!r}'.format(tag))

    # First character of tag is always the language
    try:
        lang = LANG_MAPPING[tag[0]]
    except KeyError:
        raise ValueError('Could not map language from tag: {!r}'.format(tag))

    # Add a dash tag if needed
    if len(tag) == 2:
        # Second character is entity marker
        try:
            entity = ENTITY_MAPPING[tag[1]]
        except KeyError:
            raise ValueError('Could not map entity from tag: {!r}'.format(tag))
        return lang + TAG_DASH + entity
    else:
        return lang


def preprocess(input_path, output_path):
    """Process the file at input_path, writing to output_path."""
    input_file = codecs.open(input_path, 'U', 'utf-8')
    output_file = codecs.open(output_path, 'w', 'utf-8')

    linenum = 0
    for line in input_file:
        linenum += 1

        # Split up the line
        splits = line.split()

        # Extract and transform token and tag pairs
        token_tags = [split_token(item, False) for item in splits]
        try:
            new_token_tags = [(token, transform_tag(tag))
                              for token, tag in token_tags]
        except ValueError as err:
            print('Error reading line {}: {}'.format(linenum, err),
                  file=sys.stderr)
            continue

        # Write output
        output = ' '.join('/'.join((token, tag))
                          for token, tag in new_token_tags)
        print(output, file=output_file)


def main():
    """Parse arguments and call the preprocessor."""
    parser = argparse.ArgumentParser('Preprocess the tags from the LM study.')
    parser.add_argument('input', help='input file (UTF-8 format)')
    parser.add_argument('output', help='output file (UTF-8 format)')
    args = parser.parse_args()
    preprocess(args.input, args.output)


if __name__ == '__main__':
    main()
