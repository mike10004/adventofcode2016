#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#  advent7_ababab.py
#  

#~ --- Part Two ---

#~ You would also like to know which IPs support SSL (super-secret listening).

#~ An IP supports SSL if it has an Area-Broadcast Accessor, or ABA, 
#~ anywhere in the supernet sequences (outside any square bracketed 
#~ sections), and a corresponding Byte Allocation Block, or BAB, 
#~ anywhere in the hypernet sequences. An ABA is any three-character 
#~ sequence which consists of the same character twice with a different 
#~ character between them, such as xyx or aba. A corresponding BAB is 
#~ the same characters but in reversed positions: yxy and bab, 
#~ respectively.

#~ For example:

#~ * aba[bab]xyz supports SSL (aba outside square brackets with corresponding bab within square brackets).
#~ * xyx[xyx]xyx does not support SSL (xyx, but no corresponding yxy).
#~ * aaa[kek]eke supports SSL (eke in supernet with corresponding kek in hypernet; the aaa sequence is not related, because the interior character must be different).
#~ * zazbz[bzb]cdb supports SSL (zaz has no corresponding aza, but zbz has a corresponding bzb, even though zaz and zbz overlap).

#~ How many IPs in your puzzle input support SSL?

import sys
import re

_TEST_CASES = (
    ('aba[bab]xyz', True),
    ('xyx[xyx]xyx', False),
    ('aaa[kek]eke', True),
    ('zazbz[bzb]cdb', True),
)

def is_ssl(line):
    supernets = re.findall(r'(?:^|\])(\w+)(?:$|\[)', line)
    hypernets = re.findall(r'\[(\w+)\]', line)
    for supernet in supernets:
        abas = []
        for i in xrange(len(supernet)):
            abas += re.findall(r'(\w)(\w)(\1)', supernet[i:])
        abas = map(lambda aba: ''.join(aba), abas)
        abas = list(set(abas))
        abas = filter(lambda aba: aba[0] != aba[1], abas)
        for aba in abas:
            for hypernet in hypernets:
                bab = aba[1] + aba[0] + aba[1]
                if bab in hypernet:
                    return True
    return False

def find_ssl(lines):
    for line in lines:
        if is_ssl(line):
            print "SSL %s" % line

def main(args):
    if len(args) == 0:
        lines = sys.stdin.read().split()
    else:
        lines = args
    find_ssl(lines)
    return 0

if __name__ == '__main__':
    for line, expected in _TEST_CASES:
        actual = is_ssl(line)
        assert expected == actual, "expected ssl=%s for '%s'" % (expected, line)
    sys.exit(main(sys.argv[1:]))
