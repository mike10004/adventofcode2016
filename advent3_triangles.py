#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#  advent3_triangles.py
#  

import itertools
import sys

def triad(t, oddman_index):
    pair = tuple([t[i] for i in xrange(len(t)) if i != oddman_index])
    oddman = t[oddman_index]
    return pair, oddman
    
def is_triangle(t):
    a, b, c = t
    assert a > 0 and b > 0 and c > 0, "values must be positive: %s" % str(t)
    triads = [triad(t, i) for i in xrange(len(t))]
    for pair, oddman in triads:
        if sum(pair) <= oddman:
            return False
    return True

_KNOWN_TRUE = ((5, 12, 13), (3, 3, 3), (200, 300, 450), (2, 1, 2))
_KNOWN_FALSE = ((1, 2, 3), (5, 10, 25))

def evaluate(triples):
    return [1 if is_triangle(t) else 0 for t in triples]

def main(args):
    triples = []
    for i in xrange(0, len(args.sidelengths), 3):
        triple = args.sidelengths[i+0], args.sidelengths[i+1], args.sidelengths[i+2]
        triples.append(tuple([int(s) for s in triple]))
    evaluations = evaluate(triples)
    if args.verbose:
        print >> sys.stderr, "evaluated", len(triples), "triples:", triples
    print "%d of %d triples represent valid triangle side lengths" % (sum(evaluations), len(triples))
    return 0

if __name__ == '__main__':
    assert sum(evaluate(_KNOWN_TRUE)) == len(_KNOWN_TRUE)
    assert sum(evaluate(_KNOWN_FALSE)) == 0
    from argparse import ArgumentParser
    p = ArgumentParser()
    p.add_argument("sidelengths", nargs="*", help="side lengths, every 3 of which are interpreted as a possible triangle; leave empty to read from stdin", default=[])
    p.add_argument("--verbose", default=False, action="store_true")
    args = p.parse_args()
    if len(args.sidelengths) == 0:
        args.sidelengths = sys.stdin.read().split()
    sys.exit(main(args)) 
    
