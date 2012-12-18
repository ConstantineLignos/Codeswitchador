#!/usr/bin/env python
"""
Example of using Codeswitchador's API.

Constantine Lignos, November 2012

"""

# Copyright (c) 2012, Constantine Lignos
# All rights reserved.
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
#
# 1. Redistributions of source code must retain the above copyright
# notice, this list of conditions and the following disclaimer.
#
# 2. Redistributions in binary form must reproduce the above copyright
#   notice, this list of conditions and the following disclaimer in
#   the documentation and/or other materials provided with the
#   distribution.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
# FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
# COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
# INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
# BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
# LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
# ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.

from __future__ import division
import sys
import argparse

from lid_constants import MULTIPLE_LANGS
from wordlistlid import (MODEL1_5, default_lidder, LOW_METHOD_UNK, UNK_METHOD_LEFT)
from scalereader import JERBOA_NOTAG


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
