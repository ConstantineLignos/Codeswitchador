Codeswitchador was developed as a part of the SCALE 2012 summer workshop at the
Johns Hopkins Human Language Technology Center of Excellence.


Runnable shell scripts
=======================
* make_ratiolist.sh: qsub-able wrapper for freqratio.py
* make_wordlist.sh: qsub-able wrapper for wordlist.py

Runnable Python scripts
========================
* api_sample.py: A sample of using the codeswitchador API.
* eval_codeswitch.py: Evaluate the performance of codeswitching models 0, 1.0, 1.5.
* freqratio.py: Create a frequency ratio list from two wordlists.
* wordlist.py: Create a wordlist from a corpus.

Libraries
==========
* codeswitchador.py: Support for codeswitching detection.
* lid_constants.py: Constants used by many files.
* lidlists.py: Wordlists and paths used by idiotLID and wordlist-based models.
* scalereader.py: Support for reading from common SCALE file formats (e.g., Jerboa output).
* wordlistlid.py: Wordlist-based LID/CS models.

The Basics
==========
Most common things you'll need to do:

1. Create wordlists. See:
   * tools/make_eng_wordlist.sh
   * tools/make_spa_wordlist.sh

TODO: More things here!

License
=======
Codeswitchador is distributed under the Apache License version 2.0. See
LICENSE.txt for more information.
