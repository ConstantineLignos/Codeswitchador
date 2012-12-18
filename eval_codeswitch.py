#!/usr/bin/env python
"""
Annotates data in a file for codeswitching

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
import sys
import codecs
import argparse
import re
from itertools import product, chain

from csvunicode import UnicodeDictReader
from metrics import Accuracy, SDMetrics
from tools.kfold import kfolds
from lidlists import RATIOLIST_DEFAULT_CONFIG
from lid_constants import NO_LANG, ENGLISH, SPANISH, MULTIPLE_LANGS, ENTITY_TAGS, UNKNOWN_LANG
from wordlistlid import (choose_lang, LOW_METHODS, UNK_METHODS, MODEL0, MODEL1, MODEL1_5, 
                         default_lidder, LOW_METHOD_MLE, UNK_METHOD_LEFT)
from scalereader import JerboaTokenReader, JERBOA_NOTAG

# Supported file formats
FORMAT_JERBOA = "jerboa"
FORMAT_PLAIN = "plain"
FORMAT_LOG = "log"
FORMATS = (FORMAT_JERBOA, FORMAT_PLAIN, FORMAT_LOG)

# Constants from the log TSV format
FIELD_TWEET = "tokens"
FIELD_GOLDLID = "goldlid"
FIELD_PREDLID = "lidcs"
LANG_ABBREVIATIONS = {'e': ENGLISH, 's': SPANISH, 'o': None, 'n': None}
GOLDLID_ABBREVIATIONS = {'e': ENGLISH, 's': SPANISH, 'c': MULTIPLE_LANGS, 'cs': MULTIPLE_LANGS, 
                         MULTIPLE_LANGS: MULTIPLE_LANGS, ENGLISH: ENGLISH, SPANISH: SPANISH, 
                         UNKNOWN_LANG: UNKNOWN_LANG, 'u': UNKNOWN_LANG, 'unk': UNKNOWN_LANG}
CSVALS = {'c': True, 'e': False, 's': False}
VALID_CS_LANGS = set(lang for lang, val in LANG_ABBREVIATIONS.items() if val)

# Supported models
SUPPORTED_MODELS = (MODEL0, MODEL1, MODEL1_5)

ENTITY_CHARS_RE = re.compile('[' + ''.join(ENTITY_TAGS) + ']')
def split_token(tokentag, remove_entities=True):
    """Split a token into (token, tag)."""
    # Since slashes aren't escaped, we use the rightmost slash
    lastslash = tokentag.rfind('/')
    if lastslash == -1:
        raise ValueError('No token/tag seperator: %r' % tokentag)
    token = tokentag[:lastslash]
    tag = tokentag[lastslash + 1:]
    if not tag or not token:
        raise ValueError("Bad token/tag: %r" % tokentag)
    if remove_entities and ENTITY_CHARS_RE.search(tag):
        # You can't do deletion on unicode strings with a translation table, alas
        tag = ENTITY_CHARS_RE.sub('', tag)
        if len(tag) != 1:
            raise ValueError("Bad tag %r from %r." % (tag, tokentag))
    return (token, tag)


class AnnotatedTweetReader(object):
    """Reads in tweets annotated for LID and codeswitching."""

    def __init__(self, infile):
        self._csv = UnicodeDictReader(infile, delimiter='\t')
        
    def __iter__(self):
        return self

    def next(self):
        """Return relevant fields from the next row of the CSV."""
        row = self._csv.next()
        goldlid = GOLDLID_ABBREVIATIONS[row[FIELD_GOLDLID]] if row[FIELD_GOLDLID] else None
        tokens = row[FIELD_TWEET].split()
        lidcs = row[FIELD_PREDLID]
        return tokens, lidcs, goldlid


def _valid_langs_set(langs):
    """Return the set number of valid langs in a sequence of langs."""
    valid_langs = set(LANG_ABBREVIATIONS[lang] for lang in langs)
    # Remove None, as it represents invalid langs
    valid_langs.discard(None)
    return valid_langs


def _tokens_tags_langs(infile, mode, annotated):
    """Get tokens/tags from the specified type of file."""
    if mode == FORMAT_JERBOA:
        assert not annotated, "Jerboa format cannot have annotations."
        for token_infos, _ in JerboaTokenReader(infile):
            # Sometimes lines appear to have no tokens. It's unclear why.
            if not token_infos:
                continue
            tokens, tags, _ = zip(*token_infos)
            yield (tokens, tags, None, None, None)
    elif mode == FORMAT_LOG:
        for tokens, lidcs, goldlid in AnnotatedTweetReader(infile):
            yield (tokens, None, None, lidcs, goldlid)
    elif mode == FORMAT_PLAIN:
        for line in infile:
            tokens = line.strip().split()
            if annotated:
                tokens, gold_langs = zip(*[split_token(token) for token in tokens])
                yield (tokens, None, gold_langs, None, None)
            else:
                yield (tokens, None, None, None, None)


def annotate(model, inpath, outfile, show_ratio, lowmethod, unkmethod, informat, annotated, n_folds,
             quiet=False):
    """Annotate tokens in the file."""
    assert(informat == FORMAT_LOG or model in SUPPORTED_MODELS)
    verbose = False
    # Fire up a lidder if we're not just reading from log
    if informat != FORMAT_LOG:
        lidder = default_lidder(model, RATIOLIST_DEFAULT_CONFIG)

    # Force model1 to use MLE for the low method and UNK_METHOD to left, so we're sure no randomness
    # gets applied
    if model == MODEL1:
        lowmethod = LOW_METHOD_MLE
        unkmethod = UNK_METHOD_LEFT

    # Set the flag for whether we're evaluating only message LID, not token-by-token
    token_eval = not(informat == FORMAT_LOG or model == MODEL0)

    # Evaluators
    token_acc = Accuracy()
    # For when unk is allowed
    all_lid_acc = Accuracy()
    twoway_lid_acc = Accuracy()
    cs_perf = SDMetrics()
    # When unk is excluded
    nounk_all_lid_acc = Accuracy()
    nounk_twoway_lid_acc = Accuracy()
    nounk_cs_perf = SDMetrics()
    # Code switch points
    cs_boundaries = SDMetrics()

    if n_folds:
        train_paths, test_paths = kfolds(inpath, n_folds)
        infiles = [_open_infile(inpath, informat) for inpath in test_paths]
    else:
        infiles =[_open_infile(inpath, informat)]

    fold_accuracies = []
    for infile in infiles:
        # To match the SVM_HMM evaluation, we have a special token accuracy that's reset every fold
        fold_token_acc = Accuracy()
        for tokens, tags, gold_langs, lid, gold_lid in _tokens_tags_langs(infile, informat, annotated): 
            # Put in dummy tags if needed
            if not tags:
                tags = [JERBOA_NOTAG] * len(tokens)
            tokens_lower = [token.lower() for token in tokens]

            # We label all tokens only if it's annotated, as the annotations will later
            # wipe out anything we shouldn't have labeled
            # TODO: This is a little wonky as labeled bad tokens can affect the lid/cs
            # decision, but for model 1.0 this doesn't actually matter as they aren't
            # in the wordlist
            if informat == FORMAT_LOG:
                # Skip lines with no gold annotation
                if not gold_lid:
                    continue
                # Don't label anything, just use what we got from the log file
                verdict = lid == MULTIPLE_LANGS
            elif model == MODEL0:
                lid, langspresent, hits, verdict = lidder.idlangs(tokens_lower)
                ratios = out_langs = unk_rate = None
            else:
                lid, langspresent, hits, ratios, out_langs, unk_rate, verdict = \
                    (lidder.idlangs(tokens_lower, lowmethod, unkmethod, tags) if model == MODEL1_5 else
                     lidder.idlangs(tokens_lower))

            output_lang = lid if not verdict else MULTIPLE_LANGS

            # Token labeling
            if token_eval:
                # For model 1.0, apply MLE
                if model == MODEL1:
                    out_langs = [choose_lang(token, lang, lidder.langs, tag, ratio, lowmethod, 
                                             unkmethod, False)
                         for token, tag, lang, ratio in zip(tokens_lower, tags, out_langs, ratios)]


                # Carry over NO_LANG labels from the gold standard
                if gold_langs:
                    out_langs = [out_lang if gold_lang != NO_LANG else NO_LANG
                                 for out_lang, gold_lang in zip(out_langs, gold_langs)]

                # Truncate to one char
                out_langs = [lang[0] if lang else UNKNOWN_LANG[0] for lang in out_langs]

                # Output tokens
                if not quiet:
                    out_tokens = ([(token, "{0:1.3f}".format(ratio)) for token, ratio in zip(tokens, ratios)] 
                                  if show_ratio else
                                  zip(tokens, out_langs))
                    print >> outfile, " ".join(["/".join(token_pair) for token_pair in out_tokens])

            # If it isn't annotated, skip over scoring and go to the next tokens
            if not annotated:
                continue

            # Scoring!
            # First, tokens
            if token_eval:
                # Individual tokens
                for pred_lang, gold_lang, token in zip(out_langs, gold_langs, tokens_lower):
                    if gold_lang not in (NO_LANG, 'o') and pred_lang != NO_LANG:
                        token_acc.score(pred_lang, gold_lang, token.lower())
                        fold_token_acc.score(pred_lang, gold_lang, token.lower())

                # Codeswitch points
                last_pred_lang = None
                last_gold_lang = None
                last_token = None
                for pred_lang, gold_lang, token in zip(out_langs, gold_langs, tokens_lower):
                    # Skip non-linguistic tokens
                    if gold_lang not in VALID_CS_LANGS:
                        continue

                    # Score if we have a valid last token
                    if last_gold_lang is not None:
                        # True label is whenever the language changes, but don't predict codeswitching
                        # if one of the langs was unknown. Since the label's been truncated, we take
                        # the first char of UNKNOWN_LANG.
                        pred_cs = (pred_lang != UNKNOWN_LANG[0] and last_pred_lang != UNKNOWN_LANG[0] and 
                                   pred_lang != last_pred_lang)
                        gold_cs = gold_lang != last_gold_lang
                        cs_boundaries.score(pred_cs, gold_cs, (last_token, token))

                    # Update last langs/token
                    last_pred_lang = pred_lang
                    last_gold_lang = gold_lang
                    last_token = token

            # Next, messages
            # Compute a gold_lid if we don't know it already
            if not gold_lid:
                gold_valid_langs = _valid_langs_set(gold_langs)
                gold_lid = list(gold_valid_langs)[0] if len(gold_valid_langs) == 1 else MULTIPLE_LANGS

            if gold_lid != MULTIPLE_LANGS:
                # One lang means we should check lid accuracy
                twoway_lid_acc.score(output_lang, gold_lid)
                cs_perf.score(verdict, False)
            else:
                # Multiple langs means we should check for codeswitching
                cs_perf.score(verdict, True)

            # Always record all-way LID
            all_lid_acc.score(output_lang, gold_lid)

            # Repeat not unk
            if gold_lid != UNKNOWN_LANG:
                if gold_lid != MULTIPLE_LANGS:
                    # One lang means we should check lid accuracy
                    nounk_twoway_lid_acc.score(output_lang, gold_lid)
                    nounk_cs_perf.score(verdict, False)
                else:
                    # Multiple langs means we should check for codeswitching
                    nounk_cs_perf.score(verdict, True)

                # Always record all-way LID
                nounk_all_lid_acc.score(output_lang, gold_lid)
        
        # Track fold accuracy
        fold_accuracies.append(fold_token_acc.accuracy)

    if annotated:
        output = sys.stderr
        print >> output, '*' * 10 + "All data evaluation" + '*' * 10
        print >> output, "All message LID:"
        print >> output, all_lid_acc
        print >> output, all_lid_acc.confusion_matrix()
        print >> output

        print >> output, "Non-codeswitched message LID:"
        print >> output, twoway_lid_acc
        print >> output, twoway_lid_acc.confusion_matrix()
        print >> output

        print >> output, "Message CS:"
        print >> output, cs_perf
        print >> output, cs_perf.confusion_matrix()
        print >> output

        print >> output, '*' * 10 + "No unknown lang data evaluation" + '*' * 10
        print >> output, "All message LID:"
        print >> output, nounk_all_lid_acc
        print >> output, nounk_all_lid_acc.confusion_matrix()
        print >> output

        print >> output, "Non-codeswitched message LID:"
        print >> output, nounk_twoway_lid_acc
        print >> output, nounk_twoway_lid_acc.confusion_matrix()
        print >> output

        print >> output, "Message CS:"
        print >> output, nounk_cs_perf
        print >> output, nounk_cs_perf.confusion_matrix()
        print >> output

        if token_eval:
            print >> output, '*' * 10 + "Token by token evaluation" + '*' * 10
            print >> output, "Token-by-token LID:"
            print >> output, "Low method:", lowmethod
            if model != MODEL1: # Model 1 doesn't actually do unk attachment
                print >> output, "Unk method:", unkmethod
            print >> output, token_acc
            print >> output, token_acc.confusion_matrix()
            print >> output
            print >> output, "Codeswitching boundaries:"
            print >> output, cs_boundaries
            print >> output, cs_boundaries.confusion_matrix()
            print >> output
            if not quiet and verbose:
                for gold, subdict in token_acc.confusion.items():
                    for pred, errors in subdict.items():
                        if gold == pred or not errors:
                            continue
                        print >> output, '*' * 40
                        print >> output, "Gold:", gold, "Pred:", pred
                        for error in sorted(set(errors)):
                            print >> output, error
                        print >> output

            # Report average fold accuracy if needed
            if len(fold_accuracies) > 1:
                mean_accuracy = sum(fold_accuracies) / len(fold_accuracies)
                print >> output, "Fold token accuracies: " + ", ".join("%.4f" % acc for acc in fold_accuracies)
                print >> output, "Mean token accuracy across folds: %.4f" % mean_accuracy

    return (all_lid_acc, twoway_lid_acc, cs_perf, nounk_all_lid_acc, nounk_twoway_lid_acc,
            nounk_cs_perf, token_acc)


def _open_infile(path, informat):
    """Return the correct input for the filename and format specified."""
    if informat == FORMAT_PLAIN:
        return codecs.open(path, 'Ur', 'utf_8') 
    else:
        return open(path, 'Ur') 


def main():
    """Annotate a file with codeswitching information."""
    parser = argparse.ArgumentParser(description=main.__doc__)
    parser.add_argument('-f', '--folds', metavar='nfolds', type=int, help='number of cross-validation folds')
    parser.add_argument('-r', '--ratio', action='store_true', help='show ratio information only')
    parser.add_argument('-s', '--sweep', action='store_true', help='sweep parameters')
    parser.add_argument('-a', '--annotated', action='store_true', help='input is already annotated. '
                        'Needs to be specified for the plain format; log is assumed to be '
                        'annotated while Jerboa is assumed to not be.')
    parser.add_argument('format', choices=FORMATS,
                        help="format of the input. Plain: Lines of tokens in word/tag format, "
                        "Log: TSV logfile from the output of process_codeswitch with annotation, "
                        "Jerboa: Jerboa full output")
    parser.add_argument('file', help='input file in UTF-8 format')
    parser.add_argument('model', nargs='?', choices=SUPPORTED_MODELS,
                        help='model version to use')
    parser.add_argument('lowmethod', choices=LOW_METHODS, nargs='?',
                        help='method for guessing labels of low confidence words.  Must be specified for Model 1.5 and cannot be specified for other models.')
    parser.add_argument('unkmethod', choices=UNK_METHODS, nargs='?',
                        help='method for guessing labels of unknown words. Must be specified for Model 1.5 and cannot be specified for other models.')
    args = parser.parse_args()

    # Require plain format for folds
    if args.folds and args.format != FORMAT_PLAIN:
        print "Error: Cross validation can only be used with plain files."
        parser.print_usage()
        sys.exit(2)

    # Force annotated if the format is log, force not if Jerboa
    if args.format == FORMAT_LOG:
        args.annotated = True
    elif args.format == FORMAT_JERBOA:
        args.annotated = False

    # Sweep arguments are different
    if not args.sweep:
        # Require model for non log
        if args.format == FORMAT_LOG:
            if args.model:
                print "Error: Model cannot be specified if the format is 'log.'"
                parser.print_usage()
                sys.exit(2)
        elif not args.model:
            print "Error: Model must be specified if the format is not 'log.'"
            parser.print_usage()
            sys.exit(2)

        # Make sure methods are given for model 1.5 and not other models
        if args.model == MODEL1_5:
            if not args.unkmethod or not args.lowmethod:
                print "Error: Must specify unk/low method for model 1.5."
                parser.print_usage()
                sys.exit(2)
        else:
            if args.unkmethod or args.lowmethod:
                print "Error: Cannot specify unk/low method for models other than 1.5."
                parser.print_usage()
                sys.exit(2)

        outfile = codecs.getwriter('utf-8')(sys.stdout)
        sys.stderr = codecs.getwriter('utf_8')(sys.stderr)
        annotate(args.model, args.file, outfile, args.ratio, args.lowmethod, args.unkmethod, args.format,
                 args.annotated, args.folds)
    else:
        if not args.annotated:
            print "Error: Can't sweep without annotated data."
            parser.print_usage()
            sys.exit(2)

        eval_rows = []
        for model in SUPPORTED_MODELS:
            print >> sys.stderr, "Evaluating model %s..." % model
            if model != MODEL1_5:
                # No params to mess with
                eval_rows.append(("Model_" + model,
                    annotate(model, args.file, None, args.ratio, None, None, args.format, 
                             args.annotated, args.folds, True)))
                sys.stdout.flush()
            else:
                for lowmethod, unkmethod in product(LOW_METHODS, UNK_METHODS):
                    print >> sys.stderr, "Evaluating params low: %s, unk: %s..." % (lowmethod, unkmethod)
                    eval_rows.append(("_".join(["Model", model, lowmethod, unkmethod]),
                        annotate(model, args.file, None, args.ratio, lowmethod, unkmethod, args.format, 
                                 args.annotated, args.folds, True)))
                    sys.stdout.flush()

        headers = ["Model", "All LID Accuracy", "Non-CS LID Accuracy", "CS Precision", "CS Recall",
                   "CS F1", "CS MCC",  "(No und) All LID Accuracy", "(No und) Non-CS LID Accuracy", 
                   "(No und) CS Precision", "(No und) CS Recall",  "(No und) CS F1", 
                   "(No und) CS MCC", "Token Accuracy"]
        print ",".join(headers)

        for model, row in eval_rows:
            perf = [str(item) for item in chain.from_iterable(eval.all_stats for eval in row)]
            print ",".join([model] + perf)


if __name__ == "__main__":
    main()

