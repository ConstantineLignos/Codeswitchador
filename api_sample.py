#!/usr/bin/env python
"""
Example of using Codeswitchador's API.

Constantine Lignos, November 2012

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
import argparse

from codeswitchador.lid_constants import MULTIPLE_LANGS
from codeswitchador.wordlistlid import (MODEL1_5, default_lidder, LOW_METHOD_UNK, UNK_METHOD_LEFT)
from tools.scalereader import JERBOA_NOTAG


def label_tokens(tokens, lidder, model, lowmethod, unkmethod, tags=None):
    """Perform LID for the given tokens.

    Returns a tuple of:
    lid - The dominant language of the tweet.
    codeswitched - A boolean indicating whether codeswitching is present.
    token_langs - The language label for each token.
    """
    # Put in dummy tags if needed
    if not tags:
        tags = [JERBOA_NOTAG] * len(tokens)
    # Always lowercase tokens, as our models use lowercase
    tokens_lower = [token.lower() for token in tokens]

    lid, langspresent, hits, ratios, token_langs, unk_rate, codeswitched = \
        (lidder.idlangs(tokens_lower, lowmethod, unkmethod, tags) if model == MODEL1_5 else
         lidder.idlangs(tokens_lower))

    output_lang = lid if not codeswitched else MULTIPLE_LANGS

    return (lid, codeswitched, token_langs)


def main():
    """Demonstrate using the codeswitchador API."""
    parser = argparse.ArgumentParser(description=main.__doc__)
    parser.add_argument('configfile', help='configuration file for the lidder')
    args = parser.parse_args()
    config = args.configfile

    # Model version, here's the latest
    model = MODEL1_5
    # Labeling method of low confidence tokens
    lowmethod = LOW_METHOD_UNK
    # Labeling method for unknown tokens
    unkmethod = UNK_METHOD_LEFT

    lidder = default_lidder(model, config)

    tokens = ["Yo", "quiero", "go", "to", "the", "store", "en", "el", "centro"]
    result = label_tokens(tokens, lidder, model, lowmethod, unkmethod)
    print result


if __name__ == "__main__":
    main()
