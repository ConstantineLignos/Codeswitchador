"""
A simple approach to Language IDentification (LID).

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
import codecs
import re
from random import choice, seed
from string import punctuation
from ConfigParser import RawConfigParser, NoOptionError

import numpy

from lid_constants import UNKNOWN_LANG, NO_LANG, ENGLISH, SPANISH
from lidlists import (SPANISH_TOP32, SPANISH_32PLUS, ENGLISH_TOP32, ENGLISH_32PLUS)
from scalereader import JERBOA_NOTAG
from freqratio import wordlist_ratio, prune_ratios
from codeswitchador import cs_langspresent

# Seed for reproducibility
seed(0)

# Names of our models
MODEL0 = '0.1'
MODEL1 = '1.0'
MODEL1_5 = '1.5'

# Methods for dealing with low conf and unknown words
LOW_METHOD_MLE = "mle"
LOW_METHOD_RANDOM = "random"
LOW_METHOD_UNK = "unk"
LOW_METHODS = (LOW_METHOD_MLE, LOW_METHOD_RANDOM, LOW_METHOD_UNK)
UNK_METHOD_RANDOM = 'random'
UNK_METHOD_LEFT = 'left'
UNK_METHOD_RIGHT = 'right'
UNK_METHODS = (UNK_METHOD_RANDOM, UNK_METHOD_LEFT, UNK_METHOD_RIGHT)
BEST_LOW_METHOD = LOW_METHOD_UNK
BEST_UNK_METHOD = UNK_METHOD_LEFT

# Constants for filtering data
PUNC = set(punctuation)
BAD_TOKENS = set(['rt'])
ALLOWED_TAG_PREFIXES = set(['ja', 'ha'])

class TwoListLID(object):
    """
    Provides language identification using two wordlists per language.
    """

    def __init__(self, lang_wordlists):
        """
        Set up for language identification using the given (lang_name, shortlist, longlist) tuples.

        @param lang_wordlists sequence of (lang_name, shortlist, longlist) tuples.
        """
        # Force wordlists to be sets
        self.langs, self.shorts, self.longs = zip(*[(lang, set(shortlist), set(longlist)) 
                                                    for lang, shortlist, longlist in lang_wordlists])
        # For speed, precompute number of langs
        self.nlangs = len(self.langs)

    def idlangs(self, tokens):
        """
        Return whether a language is present and the counts from each wordlist.

        @param tokens sequence of tokens to process
        @returns array of booleans for language presence and tuples of wordlist scores

        The ordering of return vectors will match the order of languages given
        at initialization.
        """
        # Create scores
        short_scores = numpy.zeros(self.nlangs, dtype=numpy.int)
        long_scores = numpy.zeros(self.nlangs, dtype=numpy.int)

        # Count each language
        for token in tokens:
            for idx in range(self.nlangs):
                if token in self.shorts[idx]:
                    short_scores[idx] += 1
                if token in self.longs[idx]:
                    long_scores[idx] += 1

        # Decide whether each language is there
        langspresent = [(short_scores[idx] > 1 or 
                         (short_scores[idx] == 1 and long_scores[idx] > 0))
                        for idx in range(self.nlangs)]

        # Codeswitching verdict
        cs = cs_langspresent(langspresent)

        # Give the number of hits in the wordlists
        hits = zip(short_scores, long_scores)
        lid = self._pick_lang(hits)
        return (lid, langspresent, hits, cs)

    def _pick_lang(self, hits):
        """
        Pick the best language from the hits resulting from idlangs using the shortlists.

        In case of ties, the first language in self.langs will be the winner.
        """
        shortlist_hits, _ = zip(*hits)
        if any(shortlist_hits):
            return self.langs[numpy.argmax(shortlist_hits)]
        else:
            return UNKNOWN_LANG


class DefaultTwoListLID(TwoListLID):
    """
    A class for doing LID using some boring, untested defaults.
    """
    # Order matters, because the first one is the default in ties
    LANGS = (SPANISH, ENGLISH)

    def __init__(self):
        super(DefaultTwoListLID, self).__init__(((SPANISH, SPANISH_TOP32, SPANISH_32PLUS), 
                                          (ENGLISH, ENGLISH_TOP32, ENGLISH_32PLUS)))

class RatioListLID(object):
    """Provides LID using a list of frequency ratios for words in two languages."""

    UNK_WORD_RATIO = 1.0
    _CONFIG_MODEL = 'model'

    def __init__(self, ratiodict, lang1, lang2, low_ratio, high_ratio, lang1_min, lang2_min, 
                 lang1_max_unk_rate, lang2_max_unk_rate, cs_max_unk_rate):
        """Set up for language ID using a ratiodict and 

        @param ratiodict dict of word: ratio pairs
        @param lang1 name to give the language whose words have low ratios
        @param lang2 name to give the language whose words have high ratios
        """
        self._ratios = ratiodict
        self.langs = (lang1, lang2, UNKNOWN_LANG)
        self.low_ratio = low_ratio
        self.high_ratio = high_ratio
        self.present_mins = (lang1_min, lang2_min)
        self.lang1_max_unk_rate = lang1_max_unk_rate
        self.lang2_max_unk_rate = lang2_max_unk_rate
        self.cs_max_unk_rate = cs_max_unk_rate

    def _ratio_lang(self, ratio):
        """
        Return the language for a given ratio.
        @param low_ratio ratio below which words need to be to count as lang1
        @param high_ratio ratio above which words need to be to count as lang2
        """
        if ratio < self.low_ratio:
            return self.langs[0]
        elif ratio > self.high_ratio:
            return self.langs[1]
        else:
            return UNKNOWN_LANG

    def idlangs(self, tokens):
        """
        Return whether a language is present and the counts from each wordlist.
        @param tokens: tokens to identify
        """
        # Per-token ratios and langs
        ratios = [self._ratios.get(token, RatioListLID.UNK_WORD_RATIO) for token in tokens]
        langs = [self._ratio_lang(ratio) if not non_lid(token) else None
                 for ratio, token in zip(ratios, tokens)]

        # Count hits, making a copy with no UNKNOWN_LANG as well
        hits = [langs.count(lang) for lang in self.langs]
        known_lang_hits = hits[:-1]
        unknown_hits = hits[-1]
        
        hitcount = sum(hits)
        unk_rate = unknown_hits / hitcount if hitcount else 1.0
        langspresent = [(langhits >= present_min) 
                        for langhits, present_min in zip(known_lang_hits, self.present_mins)]

        # Zero out langspresent based on unknown rate
        langspresent[0] = langspresent[0] and (unk_rate <= self.lang1_max_unk_rate)
        langspresent[1] = langspresent[1] and (unk_rate <= self.lang2_max_unk_rate)
    
        # If we're under the acceptable unknown rate, we can have codeswitching
        cs = cs_langspresent(langspresent) if (unk_rate <= self.cs_max_unk_rate) else False
        
        # Compute LID based on the greatest number of hits that passed thresholds
        lid = self._pick_lang([hit if present else 0 
                               for hit, present in zip(known_lang_hits, langspresent)])
        
        return (lid, langspresent, hits, ratios, langs, unk_rate, cs)

    def _pick_lang(self, hits):
        """
        Pick the best language from the hits resulting from idlangs using the shortlists.

        In case of ties, the first language in self.langs will be the winner.
        """
        if any(hits):
            return self.langs[numpy.argmax(hits)]
        else:
            return UNKNOWN_LANG

    @classmethod
    def create_from_config(cls, path):
        """Return a RatioListLID class initialized from a config file.""" 
        # Parse configuration from the file
        config = RawConfigParser()
        result = config.read(path)
        if not result:
            raise IOError("Couldn't parse config file: %s" % path)

        # Read in configuration values
        lang1 = config.get(cls._CONFIG_MODEL, "lang1")
        lang2 = config.get(cls._CONFIG_MODEL, "lang2")
        high_ratio = config.getfloat(cls._CONFIG_MODEL, "high_ratio")
        low_ratio = config.getfloat(cls._CONFIG_MODEL, "low_ratio")
        wordlist1_path = config.get(cls._CONFIG_MODEL, "wordlist1")
        wordlist2_path = config.get(cls._CONFIG_MODEL, "wordlist2")
        smoothing = config.getfloat(cls._CONFIG_MODEL, "smoothing")
        min_freq = config.getint(cls._CONFIG_MODEL, "min_freq")
        lang1_min = config.getint(cls._CONFIG_MODEL, "lang1_min")
        lang2_min = config.getint(cls._CONFIG_MODEL, "lang2_min")
        lang1_max_unk_rate = config.getfloat(cls._CONFIG_MODEL, "lang1_max_unk_rate")
        lang2_max_unk_rate = config.getfloat(cls._CONFIG_MODEL, "lang2_max_unk_rate")
        cs_max_unk_rate = config.getfloat(cls._CONFIG_MODEL, "cs_max_unk_rate")
        try:
            ignorelist = config.get(cls._CONFIG_MODEL, "ignorelist")
        except NoOptionError:
            ignorelist = None

        # Create the structures needed for the LIDder and return it
        wordlist1 = codecs.open(wordlist1_path, 'Ur', 'utf_8')
        wordlist2 = codecs.open(wordlist2_path, 'Ur', 'utf_8')
        ratios, unused1, unused2 = wordlist_ratio(wordlist1, wordlist2, smoothing, min_freq)
    
        # Remove words from the ignorelist
        if ignorelist:
            bad_words = set(line.strip() for line in codecs.open(ignorelist, 'Ur', 'utf_8'))
            prune_ratios(ratios, bad_words)

        return cls(ratios, lang1, lang2, low_ratio, high_ratio, lang1_min, lang2_min, 
                   lang1_max_unk_rate, lang2_max_unk_rate, cs_max_unk_rate)

    @classmethod
    def langs_from_config(cls, path):
        """Return (lang1, lang2) from a config file."""
        config = RawConfigParser()
        result = config.read(path)
        if not result:
            raise IOError("Couldn't parse config file: %s" % path)
        return (config.get(cls._CONFIG_MODEL, "lang1"), config.get(cls._CONFIG_MODEL, "lang2"))


class AttachingRatioListLID(RatioListLID):
    """Version of RatioListLID that applies unk word attachment as a part of LID/CS."""

    def idlangs(self, tokens, lowmethod, unkmethod, tags=None):
        """
        Return whether a language is present and the counts from each wordlist.
        @param tokens: tokens to identify
        @param tags: optional Jerboa tags for the tokens
        """
        # Per-token ratios and langs
        ratios = [self._ratios.get(token, RatioListLID.UNK_WORD_RATIO) for token in tokens]
        langs = [self._ratio_lang(ratio) if not non_lid(token) else None
                 for ratio, token in zip(ratios, tokens)]

        # Put in dummy tags if needed
        if not tags:
            tags = [JERBOA_NOTAG] * len(tokens)

        # Choose langs for
        langs = [choose_lang(token, lang, self.langs, tag, ratio, lowmethod, unkmethod, False)
                     for token, tag, lang, ratio in zip(tokens, tags, langs, ratios)]

        # Clean out any remaining unknowns
        if None in langs:
            langs = choose_unk_lang(langs, unkmethod)

        # Count hits, making a copy with no UNKNOWN_LANG as well
        hits = [langs.count(lang) for lang in self.langs]
        known_lang_hits = hits[:-1]
        unknown_hits = hits[-1]
        
        hitcount = sum(hits)
        unk_rate = unknown_hits / hitcount if hitcount else 1.0
        langspresent = [(langhits >= present_min) 
                        for langhits, present_min in zip(known_lang_hits, self.present_mins)]

        # Zero out langspresent based on unknown rate
        langspresent[0] = langspresent[0] and (unk_rate <= self.lang1_max_unk_rate)
        langspresent[1] = langspresent[1] and (unk_rate <= self.lang2_max_unk_rate)
    
        # If we're under the acceptable unknown rate, we can have codeswitching
        cs = cs_langspresent(langspresent) if (unk_rate <= self.cs_max_unk_rate) else False
        
        # Compute LID based on the greatest number of hits that passed thresholds
        lid = self._pick_lang([hit if present else 0 
                               for hit, present in zip(known_lang_hits, langspresent)])
        
        return (lid, langspresent, hits, ratios, langs, unk_rate, cs)


def default_lidder(model_name, model_config=None):
    """Return the default lidder for a given model name."""
    # Set up the LIDder
    if model_name == MODEL0:
        return DefaultTwoListLID()
    else:
        assert model_config, "Must specify model_config for models with parameters"
        if model_name == MODEL1:
            return RatioListLID.create_from_config(model_config)
        elif model_name == MODEL1_5:
            return AttachingRatioListLID.create_from_config(model_config)
    raise ValueError("Unknown model: %s" % model_name)


ALL_DIGIT_MATCHER = re.compile('^[\.0-9]+$')
def non_lid(token, tag=JERBOA_NOTAG):
    """Return whether a token's LID should be ignored."""
    return (all([char in PUNC for char in token]) or 
            ALL_DIGIT_MATCHER.match(token) or
            (tag != JERBOA_NOTAG and tag[:2] not in ALLOWED_TAG_PREFIXES) or
            token in BAD_TOKENS)


def choose_lang(token, lang, all_langs, tag, ratio, lowmethod, unkmethod, label_all):
    """Choose what language to assign a token."""
    if not label_all and non_lid(token, tag):
        # Not a linguistic token
        return NO_LANG
    elif ratio == RatioListLID.UNK_WORD_RATIO:
        # Unknown word
        if unkmethod == UNK_METHOD_RANDOM:
            return choice(all_langs)
        else:
            # This will have to be selected later in context
            return None
    elif lang == UNKNOWN_LANG:
        # Low confidence word, but not unknown
        if lowmethod == LOW_METHOD_RANDOM:
            return choice(all_langs)
        elif lowmethod == LOW_METHOD_UNK:
            # Leave it for the unknown filter, unless the unknown method is just random
            if unkmethod == UNK_METHOD_RANDOM:
                return choice(all_langs)
            else:
                return None
        else:
            if ratio < 1.0:
                return all_langs[0]
            else:
                return all_langs[1]
    else:
        return lang


def choose_unk_lang(langs, unkmethod):
    """Replace any remaining unknown langs by rule."""
    if unkmethod not in (UNK_METHOD_RIGHT, UNK_METHOD_LEFT):
        raise ValueError("Should not be choosing UNK langs when the choice is random.")

    # This code is written to go left to right, taking the lang from the left. To go
    # from the right, we reverse the list. Either way, we're working on a copy
    langs = langs[::-1] if unkmethod == UNK_METHOD_RIGHT else langs[:]
    while True:
        # Find an unk lang if there is one
        try:
            unk_idx = langs.index(None)
        except ValueError:
            break

        # Make an ordered list of which tokens it would be a good idea to
        # take a lang from. Get everything from the left first going backwards, 
        # and then everything from the right. If the index is zero, just go right.
        next_langs = (langs[unk_idx - 1::-1] + langs[unk_idx + 1:]) if unk_idx else langs[unk_idx + 1:]

        for lang in next_langs:
            if lang not in (None, NO_LANG):
                langs[unk_idx] = lang
                break
        else:
            # Give up and leave it as unknown
            langs[unk_idx] = UNKNOWN_LANG
            continue

        # Ensure we won't loop forever
        assert(langs[unk_idx] is not None)

    # Reverse again if needed
    return langs[::-1] if unkmethod == UNK_METHOD_RIGHT else langs
