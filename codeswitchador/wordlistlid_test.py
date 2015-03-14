#!/usr/bin/env python
"""Tests for wordlistlid.

Constantine Lignos, July 2012

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

import os
import sys
import unittest

from wordlistlid import *

# Tests values for ratio list lid
RATIOS = {'the': .1, 'of': .2, 'dog': .5, 'man': .6, 'iphone': .8, 'me': 1.2,
          'perro': 1.4, 'zapatos': 1.5, 'la': 1.8, 'el': 1.9} 
TOKENS = ['the', 'of', 'perro', 'dog', 'me', 'man', 'el', 'la', 'awesomesauce', '.']
LOW_RATIO = .8
HIGH_RATIO = 1.2
LANG1 = "eng"
LANG2 = "spa"
LANG1_MIN = 2
LANG2_MIN = 4
LANG1_MAX_UNK_RATE = .25
LANG2_MAX_UNK_RATE = .20
CS_MAX_UNK_RATE = .10


ONE_ENG = ['the']
TWO_ENG = ['the', 'of']
THREE_ENG = ['the', 'of', 'dog']
FOUR_ENG = ['the', 'of', 'dog', 'man']
ONE_SPA = ['la']
TWO_SPA = ['la', 'el']
THREE_SPA = ['la', 'el', 'perro']
FOUR_SPA = ['la', 'el', 'perro', 'zapatos']
JUNK = ['asdkjhadjknqaionasdas', 'lilajdomqomdnnasdasdasdacg', 'adjklasdmadascbvas']
BANNED_WORDS = ['facebook', 'fb']

class TestNonLID(unittest.TestCase):
    """Test non_lid."""

    def test_allpunc(self):
        """Return True if the token is all punctuation."""
        self.assertTrue(non_lid('.'))
        self.assertTrue(non_lid('#!'))
        self.assertTrue(non_lid('...'))
        self.assertTrue(non_lid("'"))

    def test_somepunc(self):
        """Return False if some chars but not all are punctuation."""
        self.assertFalse(non_lid("you're"))
        self.assertFalse(non_lid("runnin'"))
        self.assertFalse(non_lid("running"))

    def test_allddigit(self):
        """Return True if a token is all digits/hex."""
        self.assertTrue(non_lid('1'))
        self.assertTrue(non_lid('0.1'))
        self.assertTrue(non_lid('1.0'))
        self.assertTrue(non_lid('10'))
        self.assertTrue(non_lid('.10'))

    def test_somedigit(self):
        """Return False if a token just has some digits."""
        self.assertFalse(non_lid("f1ght"))
        self.assertFalse(non_lid("7g"))
        self.assertFalse(non_lid("ac"))
        self.assertFalse(non_lid("b3d"))

    def test_normal(self):
        """Check that pretty normal tokens are okay."""
        self.assertFalse(non_lid("never"))
        self.assertFalse(non_lid("Gonna"))
        self.assertFalse(non_lid("GIVE"))
        self.assertFalse(non_lid("yOU"))
        self.assertFalse(non_lid("up"))


class TestRatioListLID(unittest.TestCase):
    """Test RatioListLID."""
    
    def setUp(self):
        self.lidder = RatioListLID(RATIOS, LANG1, LANG2, LOW_RATIO, HIGH_RATIO, 
                                   LANG1_MIN, LANG2_MIN, LANG1_MAX_UNK_RATE,
                                   LANG2_MAX_UNK_RATE, CS_MAX_UNK_RATE)

    def test_pick_lang(self):
        # Lang goes to argmax
        self.assertEqual(self.lidder._pick_lang([1, 0]), LANG1)
        self.assertEqual(self.lidder._pick_lang([110, 7]), LANG1)
        self.assertEqual(self.lidder._pick_lang([0, 1]), LANG2)
        self.assertEqual(self.lidder._pick_lang([54, 55]), LANG2)
        # Ties go to the first lang
        self.assertEqual(self.lidder._pick_lang([777, 777]), LANG1)

    def test_idlangs_hits(self):
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(TOKENS)
        self.assertEqual(hits[0], 4)
        self.assertEqual(hits[1], 3)
        self.assertEqual(hits[2], 2)
        
    def test_idlangs_ratios(self):
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(TOKENS)
        self.assertEqual(ratios, [RATIOS.get(token, 1.0) for token in TOKENS])

    def test_idlangs_langs(self):
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(TOKENS)
        self.assertEqual(langs, [LANG1, LANG1, LANG2, LANG1, UNKNOWN_LANG, 
                                 LANG1, LANG2, LANG2, UNKNOWN_LANG, None])

    def test_idlangs_lid(self):
        # LID goes to the max which is present
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(TWO_ENG + FOUR_SPA)
        self.assertEqual(lid, LANG2)
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(FOUR_ENG + THREE_SPA)
        self.assertEqual(lid, LANG1)
        # Ties go to LANG1
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(FOUR_SPA + FOUR_ENG)
        self.assertEqual(lid, LANG1)
        # But unknown if there's nothing
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(JUNK)
        self.assertEqual(lid, UNKNOWN_LANG)

    def test_idlangs_lid_unkrate(self):
        # Unkown LID if both langs below threshold
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(ONE_ENG + TWO_SPA)
        self.assertEqual(lid, UNKNOWN_LANG)
        # The lower number of hits can still win if it's the only one above threshold
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(TWO_ENG + THREE_SPA)
        self.assertEqual(lid, LANG1)
        
    def test_idlangs_langspresent(self):
        # We add in irrelevant data to each test to ensure robustness, but it's the first
        # item in each sum that matters.
        # Below LANG1 threshold
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(ONE_ENG + JUNK + THREE_SPA)
        self.assertFalse(bool(langspresent[0]))
        # Above LANG1 threshold
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(TWO_ENG + TWO_SPA)
        self.assertTrue(bool(langspresent[0]))
        # Below LANG2 threshold
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(TWO_SPA + ONE_ENG)
        self.assertFalse(bool(langspresent[1]))
        # Above LANG2 threshold
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(FOUR_SPA + TWO_ENG + JUNK[:1])
        self.assertTrue(bool(langspresent[1]))
        # Just garbage
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(JUNK)
        self.assertFalse(any(langspresent))

    # You can skip this test for fast testing:
    # @unittest.skip("Disabled for fast testing")
    def test_create_from_default_config(self):
        # For now, just ensure that nothing blows up on defaults
        from lidlists import RATIOLIST_DEFAULT_CONFIG
        RatioListLID.create_from_config(RATIOLIST_DEFAULT_CONFIG)
        
    def test_langs_from_config(self):
        from lidlists import RATIOLIST_DEFAULT_CONFIG
        self.assertEqual(RatioListLID.langs_from_config(RATIOLIST_DEFAULT_CONFIG), (LANG1, LANG2))

    def test_lang1_max_unk_rate(self):
        """Test the unknown rate limit for lang1."""
        # Below the limit is okay
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(THREE_ENG)
        self.assertEqual(lid, LANG1)
        # At the limit still passes
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(THREE_ENG + JUNK[:1])
        self.assertEqual(lid, LANG1)
        # Above the limit fails
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(THREE_SPA + JUNK[:2])
        self.assertEqual(lid, UNKNOWN_LANG)

    def test_lang2_max_unk_rate(self):
        """Test the unknown rate limit for lang2."""
        # Below the limit is okay
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(FOUR_SPA)
        self.assertEqual(lid, LANG2)
        # At the limit still passes
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(FOUR_SPA + JUNK[:1])
        self.assertEqual(lid, LANG2)
        # Above the limit fails
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(FOUR_SPA + JUNK[:2])
        self.assertEqual(lid, UNKNOWN_LANG)

    def test_cs(self):
        """Test the unknown rate limit for CS."""
        # Both languages above threshold --> codeswitching
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(THREE_ENG + FOUR_SPA)
        self.assertTrue(cs)
        # Only one lang above, not
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(THREE_ENG + THREE_SPA)
        self.assertFalse(cs)
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(ONE_ENG + FOUR_SPA)
        self.assertFalse(cs)
        # Junk below limit, okay
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(FOUR_ENG + FOUR_SPA + FOUR_SPA[:1] + JUNK[:1])
        self.assertTrue(cs)
        # Junk at limit, okay
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(FOUR_ENG + FOUR_ENG[:1] + FOUR_SPA + JUNK[:1])
        self.assertTrue(cs)        
        # Junk above limit, not
        lid, langspresent, hits, ratios, langs, unk_rate, cs = self.lidder.idlangs(FOUR_ENG + FOUR_SPA + JUNK[:1])
        self.assertFalse(cs)


    def test_ignore_words(self):
        """Test that specified words are ignored."""
        # Make sure these are banned
        lidder = RatioListLID.create_from_config('testdata/testparams.cfg')
        lid, langspresent, hits, ratios, langs, unk_rate, cs = lidder.idlangs(BANNED_WORDS)
        self.assertEqual(langs, [UNKNOWN_LANG for _ in BANNED_WORDS])
        # Make sure other things aren't
        tokens = ['the', 'it', 'was']
        lid, langspresent, hits, ratios, langs, unk_rate, cs = lidder.idlangs(tokens)
        self.assertEqual(langs, [LANG1 for _ in tokens])
        tokens = ['el', 'esto', 'era']
        lid, langspresent, hits, ratios, langs, unk_rate, cs = lidder.idlangs(tokens)
        self.assertEqual(langs, [LANG2 for _ in tokens])
        

if __name__ == '__main__':
    unittest.main()
