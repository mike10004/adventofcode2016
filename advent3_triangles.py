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

def parse_triples_every_3(sidelengths):
    triples = []
    for i in xrange(0, len(sidelengths), 3):
        triples.append([sidelengths[i+j] for j in xrange(0, 3)])
    return triples
    
def parse_triples_every_third(sidelengths):
    triples = []
    for i in xrange(0, len(sidelengths), 9):
        for j in xrange(0, 3):
            triples.append([sidelengths[i + j + k * 3] for k in xrange(0, 3)])
    return triples
    
def main(args):
    if args.parse_mode == 'rows':
        triples = parse_triples_every_3(map(int, args.sidelengths))
    else:
        triples = parse_triples_every_third(map(int, args.sidelengths))
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
    p.add_argument("parse_mode", choices=('rows', 'cols'), help="how triples are oriented in the input")
    p.add_argument("sidelengths", nargs="*", help="side lengths, every 3 of which are interpreted as a possible triangle; leave empty to read from stdin", default=[])
    p.add_argument("--verbose", default=False, action="store_true")
    args = p.parse_args()
    if len(args.sidelengths) == 0:
        args.sidelengths = sys.stdin.read().split()
    sys.exit(main(args)) 
    
