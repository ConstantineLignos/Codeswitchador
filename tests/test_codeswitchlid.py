#!/usr/bin/env python

"""
Test basic LID functionality and the identification of code-switching.

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

import sys
import codecs

# Hackily set up the path by adding the enclosing directory
sys.path.append('..')
from codeswitchador import id_cs_model0, DefaultLID


def test():
    """Test in a simple LID scenario."""
    # Set up LID
    lidder = DefaultLID()

    # Decode input from STDIN
    data = codecs.getreader('utf_8')(sys.stdin)
    
    # Print headers
    headers = ["text", "id", "langs", "cs"] + list(lidder.langs)
    print "\t".join(headers)

    # Label each line
    for line in data:
        line = line.strip()
        langs, hits = lidder.idlangs(line.split())
        chosen_lang = lidder.pick_lang_shortlist(hits)
        cs_verdict =  id_cs_model0(langs)
        output = "\t".join([line, chosen_lang, str(cs_verdict)] + 
                           ['%s:%s' % (shorts, longs) for shorts, longs in hits])
        print codecs.decode(output, 'utf-8')


if __name__ == "__main__":
    test()
