#!/usr/bin/env python
"""
Remove edit explanations from the adjudicated corpus

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
import re
import codecs


EDIT_RE = re.compile(r"\|\|Edits.+$")

output = codecs.getwriter('utf_8')(sys.stdout)
for line in codecs.getreader('utf_8')(sys.stdin):
    print >> output, EDIT_RE.sub('', line.rstrip())
