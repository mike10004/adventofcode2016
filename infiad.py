#!/usr/bin/env python

import sys

alphabet = 'abcdefghijklmnopqrstuvwxyz'

def translate(phrase, subs):
    output = [subs.get(c, '_') if c in alphabet else c for c in phrase]
    return ''.join(output)

def find_key(subs, v):
    for k in subs:
        if subs[k] == v:
            return k
    raise ValueError("no key produces " + v + " in " + str(subs))

def make_distribution(phrase):
    d = {}
    for c in phrase:
        if c in alphabet:
            d[c] = len([a for a in phrase if a == c])
    return d

def main():
    line = None
    phrase = 'Fvzcry gbpu? Xbz arkg-yriry glcra va Hgerpug bc baf areqxjnegvre!'.lower()
    assert len(alphabet) == 26 and len(set(alphabet)) == 26
    subs = {}
    while line is None or line != 'exit':
        distro = make_distribution(phrase)
        print ', '.join(["%s %s" % (c, distro[c]) for c in sorted(distro.keys(), cmp=lambda x, y: distro[y] - distro[x])])
        if len(subs) > 0: 
            print ', '.join(["%s-%s" % (c, subs[c]) for c in subs])
        print 'o:', phrase
        print 't:', translate(phrase, subs)
        print 'enter substitution: ',
        line = sys.stdin.readline().rstrip("\r\n").lower()
        print
        if line.startswith('-'):
            parts = line.split()
            if len(parts) == 1:
                subs = {}
            else:
                try:
                    del subs[parts[1]]
                except KeyError as e:
                    print >> sys.stderr, "can't reset unset mapping", e
            continue
        if line != 'exit' and len(line.strip()) > 0:
            if len(line.split()) != 2:
                print >> sys.stderr, "invalid input"
                continue
            k, v = line.split()
            if k in alphabet and v in alphabet:
                if v in subs.values():
                    print >> sys.stderr, "%s -> %s would duplicate image %s -> %s" % (k, v, find_key(subs, v), v)
                    continue
                subs[k] = v
            else:
                print >> sys.stderr, "invalid input: must be alphabet characters", k, v
    return 0

if __name__ == '__main__':
    sys.exit(main())
