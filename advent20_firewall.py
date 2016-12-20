#!/usr/bin/env python

import sys
import re
from collections import defaultdict
import bisect

_IP_RANGE = (0, 4294967295)

def parse_range(range_str):
    m = re.compile(r'^\s*(\d+)\s*-\s*(\d+)\s*$').match(range_str)
    if m is None:
        raise ValueError(range_str)
    return int(m.group(1)), int(m.group(2))



def merge(a, b):
    alow, ahigh = a
    blow, bhigh = b
    if ahigh < blow or bhigh < alow:
        return [a, b]
    s = sorted([alow, blow, ahigh, bhigh])
    return [(s[0], s[-1])]

def merge_all(ranges):
    raise NotImplementedError()
    return merged

def _unit_tests():
    assert merge((0, 1), (2, 3)) == [(0, 1), (2, 3)]
    assert merge((0, 1), (0, 1)) == [(0, 1)]
    assert merge((0, 1), (1, 2)) == [(0, 2)]
    assert set(merge((2, 3), (0, 1))) == set([(0, 1), (2, 3)])
    assert set(merge((1, 2), (0, 1))) == set([(0, 2)])
    assert set(merge((0, 3), (1, 2))) == set([(0, 3)])
    assert set(merge((1, 2), (0, 3))) == set([(0, 3)])
    assert set(merge((0, 3), (1, 3))) == set([(0, 3)])
    assert set(merge((1, 3), (0, 3))) == set([(0, 3)])
    assert merge_all([(0, 2), (5, 8), (4, 7)]) == [(0, 2), (4, 8)]
    assert merge_all([(0, 2), (4, 7), (5, 8)]) == [(0, 2), (4, 8)]
    assert merge_all([(4, 7), (5, 8), (0, 2)]) == [(0, 2), (4, 8)]
    assert merge_all([(4, 7), (0, 2), (5, 8)]) == [(0, 2), (4, 8)]
    assert merge_all([(5, 8), (0, 2), (4, 7)]) == [(0, 2), (4, 8)]
    assert merge_all([(5, 8), (4, 7), (0, 2)]) == [(0, 2), (4, 8)]


def main(args):
    _unit_tests()
    ranges = [parse_range(s) for s in sys.stdin]

if __name__ == '__main__':
    from argparse import ArgumentParser
    p = ArgumentParser()
    args = p.parse_args()
    sys.exit(main(args))
