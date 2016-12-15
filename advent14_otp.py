#!/usr/bin/env python

import sys
import re
import hashlib
import logging
from collections import defaultdict

_log = logging.getLogger('a14otp')
_PATT3 = re.compile(r'(?:(\w)\1\1)')
_PATT5 = re.compile(r'(?:(\w)\1\1\1\1)')
_MAX_CURSOR = 10000000   # 10 million

def clean(items, predicate):
    assert isinstance(items, list) or isinstance(items, dict)
    keys = xrange(len(items)) if isinstance(items, list) else items.keys() 
    indices = []
    for i in keys:
        if not predicate(items[i]):
            indices.append(i)
    indices.sort(reverse=True)
    for i in indices:
        del items[i]
    return items

def get_triplet_char(hsh):
    m = _PATT3.search(hsh)
    assert m is not None, "no triplets: " + hsh
    return m.group(1)

class CursorError(ValueError):
    pass

def default_hasher(salt, index, stretch):
    if index > _MAX_CURSOR:
        raise CursorError("%d > %s" % (index, _MAX_CURSOR))
    h = salt + str(index)
    for i in xrange(1 + stretch):
        hasher = hashlib.md5()
        hasher.update(h)
        h = hasher.hexdigest()
    return h    

class BuffStream(object):

    def __init__(self, salt, stretch, cursor=0, memory=1000, hasher=default_hasher):
        self.salt = salt
        self.hasher = hasher
        self.cursor = cursor
        self.memory = memory
        assert memory > 0, "memory must be > 0"
        self.hashes = defaultdict(lambda: None)
        self.quints = defaultdict(list)  # ch -> index of hashes
        self.stopped = False
        self.stretch = stretch
        self._prepare()

    def _clean(self):
        young = lambda i: i >= self.cursor
        clean(self.hashes, young)
        for ch in self.quints:
            clean(self.quints[ch], young)
        clean(self.quints, lambda l: len(l) > 0)
    
    def _add_hash(self, index):
        h = self.hasher(self.salt, index, self.stretch)
        m = _PATT3.search(h)
        if m is not None:
            self.hashes[index] = h
        for g in _PATT5.findall(h):
            self.quints[g].append(index)

    def _prepare(self):
        for i in xrange(self.cursor, self.memory):
            self._add_hash(i)

    def next(self):
        good = None
        while good is None and not self.stopped:
            current = self.hashes[self.cursor]
            self.cursor += 1
            self._clean()
            next_index = self.cursor - 1 + self.memory
            try:
                self._add_hash(next_index)
            except CursorError as e:
                _log.debug("tried to add hash %d: %s", next_index, e)
                self.stopped = True
            if current is not None:  # it's a triplet
                ch = get_triplet_char(current)
                if ch in self.quints:
                    good = current
        return good

def prefab_hasher(hashes, max_cursor=None):
    if max_cursor is None: 
        max_cursor = len(hashes) - 1
    def nexthash(salt, index, stretch):  # pylint: disable=unused-argument
        if index > max_cursor:
            raise CursorError("index %d for hashes %s" % (index, str(hashes)))
        return hashes[index]
    return nexthash

def generate(s, nkeys, action=lambda x, y, z: None):
    otpkeys = []
    while len(otpkeys) < nkeys:
        key = s.next()
        index = s.cursor - 1
        otpkeys.append((index, key))
        action(len(otpkeys), index, key)
    return otpkeys

def test_stream(memory, hashes, *expected):
    s = BuffStream('salt', 0, memory=memory, hasher=prefab_hasher(hashes + list(['$'] * (memory + 1))))
    for i in xrange(len(expected)):
        actual = s.next()
        if expected[i] != actual:
            print >> sys.stderr, "expected next() call %d == %s but was %s (cursor=%d, memory=%d in %s)" % (i+1, expected[i], actual, s.cursor, s.memory, str(hashes))
            sys.exit(2)

def test(skip_parts=()):
    # pylint: disable=protected-access
    # assert _PATT3.search('abcddd13d5ba') is not None
    # assert _PATT5.findall('35aaaaa293bc9') == ['a']
    # assert _PATT5.findall('35ddddd293bc9eeeeeee124fff0ggg9hhhhh') == ['d', 'e', 'h']
    assert clean([], lambda x: False) == []
    assert clean([], lambda x: True) == []
    assert clean([1, 2, 3], lambda x: False) == []
    assert clean([1, 2, 3], lambda x: True) == [1, 2, 3]
    assert clean(['a', 'bb', 'c', 'ddd'], lambda c: len(c) > 1) == ['bb', 'ddd']
    assert clean({'a': 100, 'b': 101, 'c': 102, 'd': 103}, lambda v: v % 2 == 0) == {'a': 100, 'c': 102}
    test_stream(1, ['a', 'b', 'ccc', 'ccccc'], 'ccc')
    test_stream(1, ['aaa', 'b', 'ccc', 'ccccc'], 'ccc')
    test_stream(1, ['0', 'aaa', 'aaaaa', '1', 'bbb', 'bbbbb'], 'aaa', 'bbb')
    test_stream(1, ['aaa1', 'aaaaa2', 'aaaaa3'], 'aaa1', 'aaaaa2')
    test_stream(1, ['a', 'b', 'c', 'c', 'd', 'e'], None)
    test_stream(1, ['a', 'bbb', 'c', 'bbbbb', 'd'], None)
    test_stream(2, ['aaa', 'aaaaa'], 'aaa')
    test_stream(2, ['aaa', 'x', 'aaaaa'], 'aaa')
    test_stream(2, ['aaa', 'x', 'y', 'aaaaa'], None)
    test_stream(2, ['a', 'b', 'ccc', 'd', 'ccccc', 'e', 'f', 'ggg', 'ggggg', 'h'], 'ccc', 'ggg')
    test_stream(3, ['aaa', 'b', 'c', 'aaaaa'], 'aaa')
    if '1' not in skip_parts:
        # part one
        s = BuffStream('abc', 0)
        keys = generate(s, 64)
        first_index, first = keys[0]
        assert first_index == 39, "first index %d != 39" % first_index
        assert first == '347dac6ee8eeea4652c7476d0f97bee5', "first = %s, cursor = %d" % (first, s.cursor)
        last_index = keys[63][0]
        assert last_index == 22728, "last index %d != 22728"
    if '2' not in skip_parts:
        s = BuffStream('abc', 2016)
        keys = generate(s, 64)
        first_index, first = keys[0]
        assert first_index == 10, "first index %d != 39" % first_index
        # assert first == '347dac6ee8eeea4652c7476d0f97bee5', "first = %s, cursor = %d" % (first, s.cursor)
        last_index = keys[63][0]
        assert last_index == 22551, "last index %d != 22728"

def main(pargs):
    test(() if pargs.skip_part is None else pargs.skip_part.split(','))
    s = BuffStream(pargs.salt, pargs.stretch)
    def printer(nkeys, index, key):
        print "%2d %5d %s" % (nkeys, index, key)
    generate(s, 64, printer)
    return 0

if __name__ == '__main__':
    from argparse import ArgumentParser
    p = ArgumentParser()
    p.add_argument('salt')
    p.add_argument('--keys', default=64, metavar='N')
    p.add_argument('--skip-part', choices=('1', '2', '1,2', '2,1'))
    p.add_argument('--stretch', metavar="N", default=2016, help="stretch hashes by rehashing N times")
    p.add_argument('--log-level', choices=('DEBUG', 'INFO', 'WARN', 'ERROR'), default='INFO')
    args = p.parse_args()
    logging.basicConfig(level=eval('logging.' + args.log_level))
    sys.exit(main(args))
