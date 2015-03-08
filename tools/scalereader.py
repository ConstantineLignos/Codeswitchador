"""
Utility functions for reading file formats used in SCALE 2012

Constantine Lignos, June 2012

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

JERBOA_NOTAG = "null"


class JerboaTokenReader(object):
    """
    Reads from the full output of the Jerboa tokenizer.
    """

    def __init__(self, tokenizedfile):
        self.file = tokenizedfile

    def __iter__(self):
        return self

    def next(self):
        # Get tokens as long as there are any
        try:
            orig_text = self.file.next()
            tokens = self.file.next().split()
            tags = self.file.next().split()
            indices = self.file.next().split()
            return (zip(tokens, tags, indices), orig_text)
        except StopIteration:
            raise
