#!/usr/bin/env python

# Advent Day 20 parts one and two

import sys
import re

def parse_range(range_str):
    m = re.compile(r'^\s*(\d+)\s*-\s*(\d+)\s*$').match(range_str)
    if m is None:
        raise ValueError(range_str)
    return int(m.group(1)), int(m.group(2))

def are_disjoint(a, b):
    assert a[0] <= a[1] and b[0] <= b[1]
    return a[1] < (b[0] - 1) or b[1] < (a[0] - 1)

def are_all_disjoint(ranges):
    if len(ranges) <= 1:
        return True
    for i in xrange(len(ranges)):
        for j in xrange(i + 1, len(ranges)):
            if not are_disjoint(ranges[i], ranges[j]):
                return False
    return True

def index_pairs(min_index, max_index_exclusive):
    """Generator of pairs of distinct indices in the given interval."""
    for i in xrange(min_index, max_index_exclusive):
        for j in xrange(i + 1, max_index_exclusive):
            yield (i, j)

def extract_pair(items, j, k):
    """Return a 3-tuple that contains two specified items from a list and a new list with all but those items."""
    assert j != k
    assert len(items) >= 2
    assert j < len(items) and k < len(items), "indices not in range: %d and %d must be < %d" % (j, k, len(items))
    p, q = min(j, k), max(j, k)
    return items[p], items[q], items[:p] + items[p+1:q] + items[q+1:]

def merge(a, b):
    """Merge two ranges. Return a list of ranges that is an irreducible representation of the given ranges.
       Given ranges may be disjoin, continguous, or overlapping. Return value will be a list of one range
       or a list of two disjoint ranges (if the inputs are disjoint)."""
    if are_disjoint(a, b):
        return [a, b]
    alow, ahigh = a
    blow, bhigh = b
    s = sorted([alow, blow, ahigh, bhigh])
    return [(s[0], s[-1])]

def merge_all(ranges):
    """Merge multiple ranges. Iteratively reduce the given ranges to an irreducible list of disjoint ranges."""
    ranges = list(ranges)
    all_disjoint = False
    while not all_disjoint:
        all_disjoint = True
        for i, j in index_pairs(0, len(ranges)):  # len(ranges) >= 2 because are_all_disjoint returned False
            if not are_disjoint(ranges[i], ranges[j]):
                a, b, others = extract_pair(ranges, i, j)
                ranges = merge(a, b) + others
                all_disjoint = False
                break
    return ranges

def _unit_tests():
    assert [p for p in index_pairs(0, 0)] == []
    assert [p for p in index_pairs(0, 1)] == []
    assert [p for p in index_pairs(0, 2)] == [(0, 1)]
    assert [p for p in index_pairs(0, 3)] == [(0, 1), (0, 2), (1, 2)]
    assert [p for p in index_pairs(0, 4)] == [(0, 1), (0, 2), (0, 3), (1, 2), (1, 3), (2, 3)]
    assert are_disjoint((0, 1), (0, 1)) is False
    assert are_disjoint((0, 1), (0, 2)) is False
    assert are_disjoint((0, 1), (1, 2)) is False
    assert are_disjoint((0, 1), (1, 1)) is False
    assert are_disjoint((0, 2), (0, 1)) is False
    assert are_disjoint((1, 2), (0, 1)) is False
    assert are_disjoint((1, 1), (0, 1)) is False
    assert are_disjoint((0, 0), (0, 1)) is False
    assert are_disjoint((0, 0), (1, 1)) is False
    assert are_disjoint((1, 1), (0, 0)) is False
    assert are_disjoint((0, 0), (2, 2)) is True
    assert are_disjoint((2, 2), (0, 0)) is True
    assert are_disjoint((0, 2), (1, 2)) is False
    assert are_disjoint((0, 2), (1, 3)) is False
    assert are_disjoint((0, 2), (2, 3)) is False
    assert are_disjoint((1, 2), (0, 2)) is False
    assert are_disjoint((1, 3), (0, 2)) is False
    assert are_disjoint((2, 3), (0, 2)) is False
    assert are_disjoint((0, 1), (2, 3)) is False
    assert are_disjoint((2, 3), (0, 1)) is False
    assert are_disjoint((1, 2), (3, 4)) is False
    assert are_disjoint((3, 4), (1, 2)) is False
    assert are_disjoint((1, 2), (4, 5)) is True
    assert are_disjoint((4, 5), (1, 2)) is True
    assert merge((0, 1), (3, 4)) == [(0, 1), (3, 4)]
    assert merge((0, 1), (0, 1)) == [(0, 1)]
    assert merge((0, 1), (1, 2)) == [(0, 2)]
    assert set(merge((3, 4), (0, 1))) == set([(0, 1), (3, 4)])
    assert set(merge((1, 2), (0, 1))) == set([(0, 2)])
    assert set(merge((0, 3), (1, 2))) == set([(0, 3)])
    assert set(merge((1, 2), (0, 3))) == set([(0, 3)])
    assert set(merge((0, 3), (1, 3))) == set([(0, 3)])
    assert set(merge((1, 3), (0, 3))) == set([(0, 3)])
    assert set(merge((1, 2), (3, 4))) == set([(1, 4)])
    assert set(merge((3, 4), (1, 2))) == set([(1, 4)])
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
    p.add_argument("--skip-tests", action="store_true", default=False)
    args = p.parse_args()
    if not args.skip_tests:
        _unit_tests()
    ranges = [parse_range(s) for s in sys.stdin]
    merged_ranges = merge_all(ranges)
    print "merged ranges from", len(ranges), "to", len(merged_ranges)
    merged_ranges.sort(cmp=lambda a, b: a[0] - b[0])
    ip_min, ip_max = args.range
    min_unblocked = ip_min
    if len(merged_ranges) > 0:
        if ip_min >= merged_ranges[0][0]:
            min_unblocked = merged_ranges[0][1] + 1
    print "least unblocked:", min_unblocked
    excluded_size = sum([_size(rng) for rng in merged_ranges])
    universe_size = _size((ip_min, ip_max))
    print "num unblocked:", universe_size - excluded_size
    return 0

if __name__ == '__main__':
    sys.exit(main())
