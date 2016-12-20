#!/usr/bin/env python

import sys
import re

_IP_RANGE = (0, 4294967295)

def parse_range(range_str):
    m = re.compile(r'^\s*(\d+)\s*-\s*(\d+)\s*$').match(range_str)
    if m is None:
        raise ValueError(range_str)
    return int(m.group(1)), int(m.group(2))

def are_disjoint(a, b):
    return a[1] < b[0] or b[1] < a[0]

def are_all_disjoint(ranges):
    if len(ranges) <= 1:
        return True
    for i in xrange(len(ranges)):
        for j in xrange(i + 1, len(ranges)):
            if not are_disjoint(ranges[i], ranges[j]):
                return False
    return True

def merge(a, b):
    if are_disjoint(a, b):
        return [a, b]
    alow, ahigh = a
    blow, bhigh = b
    s = sorted([alow, blow, ahigh, bhigh])
    return [(s[0], s[-1])]

def index_pairs(max_index):
    pairs = []
    for i in xrange(max_index):
        for j in xrange(i + 1, max_index):
            pairs.append((i, j))
    return pairs

def extract_pair(items, j, k):
    assert j is not None and k is not None
    assert j != k
    assert len(items) >= 2
    assert j < len(items) and k < len(items), "indices not in range: %d and %d must be < %d" % (j, k, len(items))
    p, q = min(j, k), max(j, k)
    return items[p], items[q], items[:p] + items[p+1:q] + items[q+1:]

def merge_all(ranges):
    if are_all_disjoint(ranges):
        return list(ranges)
    m, n = None, None
    for i, j in index_pairs(len(ranges)):  # len(ranges) >= 2 because are_all_disjoint returned False
        if not are_disjoint(ranges[i], ranges[j]):
            m, n = i, j
            break
    assert m is not None and n is not None  # because some pair is not disjoint
    a, b, others = extract_pair(ranges, m, n)
    c = merge(a, b)
    assert len(c) == 1
    return merge_all(c + others)


def _unit_tests():
    assert are_disjoint((0, 1), (0, 1)) is False
    assert are_disjoint((0, 1), (0, 2)) is False
    assert are_disjoint((0, 1), (1, 2)) is False
    assert are_disjoint((0, 1), (1, 1)) is False
    assert are_disjoint((0, 2), (0, 1)) is False
    assert are_disjoint((1, 2), (0, 1)) is False
    assert are_disjoint((1, 1), (0, 1)) is False
    assert are_disjoint((0, 0), (0, 1)) is False
    assert are_disjoint((0, 0), (1, 1)) is True
    assert are_disjoint((1, 1), (0, 0)) is True
    assert are_disjoint((0, 2), (1, 2)) is False
    assert are_disjoint((0, 2), (1, 3)) is False
    assert are_disjoint((0, 2), (2, 3)) is False
    assert are_disjoint((1, 2), (0, 2)) is False
    assert are_disjoint((1, 3), (0, 2)) is False
    assert are_disjoint((2, 3), (0, 2)) is False
    assert are_disjoint((0, 1), (2, 3)) is True
    assert are_disjoint((2, 3), (0, 1)) is True
    assert merge((0, 1), (2, 3)) == [(0, 1), (2, 3)]
    assert merge((0, 1), (0, 1)) == [(0, 1)]
    assert merge((0, 1), (1, 2)) == [(0, 2)]
    assert set(merge((2, 3), (0, 1))) == set([(0, 1), (2, 3)])
    assert set(merge((1, 2), (0, 1))) == set([(0, 2)])
    assert set(merge((0, 3), (1, 2))) == set([(0, 3)])
    assert set(merge((1, 2), (0, 3))) == set([(0, 3)])
    assert set(merge((0, 3), (1, 3))) == set([(0, 3)])
    assert set(merge((1, 3), (0, 3))) == set([(0, 3)])
    assert set(merge_all([(0, 2), (5, 8), (4, 7)])) == set([(0, 2), (4, 8)])
    assert set(merge_all([(0, 2), (4, 7), (5, 8)])) == set([(0, 2), (4, 8)])
    assert set(merge_all([(4, 7), (5, 8), (0, 2)])) == set([(0, 2), (4, 8)])
    assert set(merge_all([(4, 7), (0, 2), (5, 8)])) == set([(0, 2), (4, 8)])
    assert set(merge_all([(5, 8), (0, 2), (4, 7)])) == set([(0, 2), (4, 8)])
    assert set(merge_all([(5, 8), (4, 7), (0, 2)])) == set([(0, 2), (4, 8)])

def _size(rng):
    return max(rng) - min(rng) + 1

def main():
    from argparse import ArgumentParser
    p = ArgumentParser()
    p.add_argument("--range", nargs=2, metavar="N", default=(0, 4294967295), help="ip range to search")
    args = p.parse_args()
    _unit_tests()
    ranges = [parse_range(s) for s in sys.stdin]
    ranges = merge_all(ranges)
    ip_min, ip_max = args.range
    excluded_size = sum([_size(rng) for rng in ranges])
    universe_size = _size((ip_min, ip_max))
    print universe_size - excluded_size
    return 0

if __name__ == '__main__':
    sys.exit(main())
