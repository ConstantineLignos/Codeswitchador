#!/usr/bin/env python
"""
Perf benchmarking for wordlistlid.

Constantine Lignos, July 2012

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
import timeit
from functools import partial

from wordlistlid import *
from lidlists import RATIOLIST_DEFAULT_CONFIG

NRUNS = 10000

TOKENS = ['the', 'of', 'perro', 'dog', 'me', 'man', 'el', 'la', 'awesomesauce'] * 2
lidder = DefaultTwoListLID()

baseline = timeit.timeit(partial(lidder.idlangs, TOKENS), number=NRUNS)
print "Baseline:", baseline

lidder = RatioListLID.create_from_config(RATIOLIST_DEFAULT_CONFIG)
new = timeit.timeit(partial(lidder.idlangs, TOKENS), number=NRUNS)
print "New:", new

print "New/Baseline:", new/baseline
