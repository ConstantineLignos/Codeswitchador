#!/usr/bin/env python
"""
Check the tags in a corpus.

Constantine Lignos
February 2013

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

import sys
import codecs

# Hack to allow import of split_token, forces this to be run from subdir.
sys.path.append('..')
from eval_codeswitch import split_token


GOOD_TAGS = set(['e', 's', 'n'])


linenum = 0
for line in codecs.getreader('utf_8')(sys.stdin):
    linenum += 1
    line = line.rstrip()
    try:
        _, tags = zip(*[split_token(token) for token in line.split()])
    except ValueError as err:
        print err
        print "From line {}: {!r}".format(linenum, line)
    
    for tag in tags:
        if tag not in GOOD_TAGS:
            print "Bad tag:", tag
            print "From line {}: {!r}".format(linenum, line)
