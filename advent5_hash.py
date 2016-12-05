#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#  advent5_hash.py
#  

import sys, hashlib

def count(charlist):
    return len(filter(lambda x: x is not None, charlist))

class Simple:
    
    def is_interesting(self, digest, charlist):
        return digest[:5] == '00000'
    
    def populate(self, digest, charlist):
        charlist[count(charlist)] = digest[5]

class Fancy:
    
    def __init__(self, pwlen=8):
        self.pwlen = pwlen
    
    def is_interesting(self, digest, charlist):
        try:
            return digest[:5] == '00000' and int(digest[5]) < self.pwlen and charlist[int(digest[5])] is None
        except ValueError:
            return False

    def populate(self, digest, charlist):
        position = int(digest[5])
        charlist[position] = digest[6]

def join(charlist):
    return ''.join([ch or ' ' for ch in charlist])

def find_password(door, finder, salt_start=0, pwlen=8, alg='md5', verbose=False):
    salt = salt_start
    p = [None] * pwlen
    while count(p) < pwlen:
        digest = ''
        while not finder.is_interesting(digest, p):
            h = hashlib.new(alg)
            h.update(door + str(salt))
            digest = h.hexdigest()
            salt += 1
        finder.populate(digest, p)
        if verbose: 
            print >> sys.stderr, "%8s: md5('%s%d')=%s" % (join(p), door, salt - 1, digest[:12] + '...')
    return join(p)

def main(args):
    if args.verbose:
        print >> sys.stderr, 'finding password for door', args.door
    print args.door, '->', find_password(args.door, eval(args.finder)(), salt_start=args.salt_start, verbose=args.verbose)
    return 0

if __name__ == '__main__':
    from argparse import ArgumentParser
    p = ArgumentParser()
    p.add_argument('-s', '--salt-start', default=0, type=int)
    p.add_argument('--test', action='store_true')
    p.add_argument('-v', '--verbose', action='store_true', default=False, help='print messages on stderr')
    p.add_argument('--finder', choices=('Simple', 'Fancy'), default='Fancy')
    p.add_argument('door', nargs='?', help='door id')
    args = p.parse_args()
    if args.test:
        for door, finder, pw in (
                ('abc', Simple(), '18f47a30'), 
                ('ojvtpuvg', Simple(), '4543c154'), 
                ('abc', Fancy(), '05ace8e3'),
            ):
            actual = find_password(door, finder, verbose=args.verbose)
            assert actual == pw, "input '%s' with %s should be %s but is %s" % (door, str(type(finder)), pw, actual)
    else:
        sys.exit(main(args))
