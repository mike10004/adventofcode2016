#!/usr/bin/env python

import sys
import re
import hashlib
from collections import defaultdict

_PATT3 = re.compile(r'(?:(\w)\1\1)')
_PATT5 = re.compile(r'(?:(\w)\1\1\1\1)')
_MAX_CURSOR = 10000000   # 10 million

class HashStream(object):

    def __init__(self, salt, cursor=0, memory=1000, triplets=defaultdict(list)):
        self.cursor = cursor
        self.salt = salt
        self.memory = memory
        if not isinstance(triplets, defaultdict):
            t = triplets
            triplets = defaultdict(list)
            triplets.update(t)
        self.triplets = triplets  # dict of char -> (index, hash)

    def _clean_triplets(self):
        for ch in self.triplets:
            chlist = self.triplets[ch]
            too_old = []
            for i in xrange(len(chlist)):
                index = chlist[i][0]
                if self.cursor - index > self.memory:
                    too_old.append(i)
            too_old.sort(reverse=True)
            for i in too_old:
                del chlist[i]

    def _maybe_add_triplet(self, hsh):
        m = _PATT3.search(hsh)
        if m is not None:
            ch = m.group(1)
            self.triplets[ch].append((self.cursor, hsh))
            return ch, self.cursor

    def _find_triplet_hash(self, hsh, retain=False):
        """Finds the hash containing a triplet matching the given hash's 
        quintuplet, if it exists. Otherwise returns None."""
        if self.cursor >= self.memory:
            quints = _PATT5.findall(hsh)
            for q in quints:
                if q in self.triplets:
                    trlist = self.triplets[q]
                    tindex, thash = trlist[0]
                    assert self.cursor - tindex <= self.memory, "triplet too old: %s %s" % (tindex, thash) 
                    if not retain:
                        del trlist[0]
                    return thash
    
    def next(self, sentinel=lambda c: c > _MAX_CURSOR):
        thash = None
        while thash is None and not sentinel(self.cursor):
            self._clean_triplets()
            hasher = hashlib.md5()
            hasher.update(self.salt + str(self.cursor))
            hsh = hasher.digest()
            thash = self._find_triplet_hash(hsh)
            if thash is not None: 
                return thash  # there might be more; if we find one, repeat with same hash
            self._maybe_add_triplet(hsh)
            self.cursor += 1
        
def test():
    # pylint: disable=protected-access
    assert _PATT3.search('abcddd13d5ba') is not None
    assert _PATT5.findall('35aaaaa293bc9') == ['a']
    
    s = HashStream('abc', cursor=5000, triplets={
        'a': [(3999, 'aaa')], 
        'b': [(4000, 'bbbar')],
        'd': [(4500, 'ddd1'), (4600, 'ddd2')],
        'f': [(4900, 'fffoo')],
        'g': [(4901, 'gggaw')],

    })
    s._clean_triplets()
    assert len(s.triplets['a']) == 0
    assert len(s.triplets['b']) == 1
    assert s._find_triplet_hash('bbbbb', retain=True) == 'bar'
    assert s._find_triplet_hash('ccccc', retain=True) is None
    assert s._maybe_add_triplet('123xxx098') == ('x', 5000)
    assert s._maybe_add_triplet('2345678') is None
    assert s._find_triplet_hash('134ddddd99', retain=True) == 'ddd1'



def main(args):
    test()
    return 0

if __name__ == '__main__':
    from argparse import ArgumentParser
    p = ArgumentParser()
    p.add_argument('salt')
    args = p.parse_args()
    sys.exit(main(args))
