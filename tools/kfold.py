#!/usr/bin/env python
"""Split a data set into k folds."""

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
import random


def kfolds(inpath, n_folds):
    """Split a file into n_folds train/test folds."""
    # Read in all lines at once and shuffle
    lines = codecs.open(inpath, 'rU', 'utf_8').readlines()
    random.seed(0)
    random.shuffle(lines)

    # Open output files
    assert '.' in inpath, "Filenames given need to be in name.extension format"
    dot_idx = inpath.rfind('.')
    base, ext = inpath[:dot_idx], inpath[dot_idx + 1:]
    train_paths = ['%s_train_%d.%s' % (base, i, ext) for i in range(n_folds)]
    test_paths = ['%s_test_%d.%s' % (base, i, ext) for i in range(n_folds)]
    train_outs = [codecs.open(train_path, 'w', 'utf-8') for train_path in train_paths]
    test_outs = [codecs.open(test_path, 'w', 'utf-8') for test_path in test_paths]
    test_idx = 0
    for line in lines:
        test_idx = (test_idx + 1) % n_folds
        # Write it to test for its index
        print >> test_outs[test_idx], line.rstrip()
        # Write it to train for all other files
        for train_idx, train_file in enumerate(train_outs):
            if train_idx != test_idx:
                print >> train_file, line.rstrip()

    # Close up
    for train_file in train_outs:
        train_file.close()
    for test_file in test_outs:
        test_file.close()

    return (test_paths, train_paths)


def main():
    """Call the splitter on command line args."""
    kfolds(sys.argv[1], int(sys.argv[2]))


if __name__ == "__main__":
    main()
