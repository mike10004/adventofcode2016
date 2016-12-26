#!/usr/bin/env python

# Advent of Code 2016 Day 21

import sys
import re
import itertools

_DEBUG = False

# swap position X with position Y means that the letters at indexes X and Y (counting from 0) 
# should be swapped.

def swap_pos(s, x, y):
    s = list(s)
    a, b = s[x], s[y]
    s[y] = a
    s[x] = b
    return s

# swap letter X with letter Y means that the letters X and Y should be swapped 
# (regardless of where they appear in the string).

def swap_letter(s, x, y):
    assert x != y
    return [y if c == x else (x if c == y else c) for c in s]

# rotate left/right X steps means that the whole string should be rotated; 
# for example, one right rotation would turn abcd into dabc.
# one left rotation would turn abcd into bcda.
def rotate_steps(s, direction, x):
    n = len(s)
    if direction == 'right':
        return s[n-x:] + s[:n-x]
    elif direction == 'left':
        return s[x:] + s[:x]
    assert False, 'direction invalid: ' + str(direction)

# rotate based on position of letter X means that the whole string should be 
# rotated to the right based on the index of letter X (counting from 0) as 
# determined before this instruction does any rotations. Once the index is determined, 
# rotate the string to the right one time, plus a number of times equal to that index, 
# plus one additional time if the index was at least 4.
def rotate_letter(s, x, unused=None):
    i = s.index(x)
    p = 1 + i + (1 if i >= 4 else 0)
    return rotate_steps(s, 'right', p)

# reverse positions X through Y means that the span of letters at indexes X through Y
# (including the letters at X and Y) should be reversed in order.

def reverse(s, x, y):
    _min, _max = x, y + 1
    head, body, tail = s[:_min], s[_min:_max], s[_max:]
    body.reverse()
    return head + body + tail

# move position X to position Y means that the letter which is at index X should be 
# removed from the string, then inserted such that it ends up at index Y.

def move(s, x, y):
    ch = s[x]
    t = s[:x] + s[x+1:]
    head, tail = t[:y], t[y:]
    return head + [ch] + tail

def _test(fn, s, arg1, arg2, expected):
    t = [c for c in s]
    actual = ''.join(fn(t, arg1, arg2))
    assert actual == expected, "%s(\"%s\", %s, %s): actual %s != %s expected" % (fn.__name__, s, str(arg1), str(arg2), actual, expected)

def _run_tests():
    _test(swap_pos, 'abcdef', 1, 3, 'adcbef')
    _test(swap_letter, 'abcd', 'a', 'c', 'cbad')
    _test(rotate_steps, 'abcd', 'left', 1, 'bcda')
    _test(rotate_steps, 'abcd', 'right', 1, 'dabc')
    _test(rotate_letter, 'abdec', 'b', '', 'ecabd')
    _test(rotate_letter, 'ecabd', 'd', '', 'decab')
    _test(rotate_letter, 'abcdef', 'b', '', 'efabcd')
    _test(rotate_letter, 'abcdefghij', 'c', '', 'hijabcdefg')
    _test(rotate_letter, 'abcdefghij', 'f', '', 'defghijabc')
    _test(rotate_letter, 'abcdef', 'a', '', 'fabcde')
    _test(rotate_letter, 'abcdef', 'b', '', 'efabcd')
    _test(rotate_letter, 'abcdef', 'c', '', 'defabc')
    _test(rotate_letter, 'abcdef', 'd', '', 'cdefab')
    _test(rotate_letter, 'abcdef', 'e', '', 'abcdef')
    _test(rotate_letter, 'abcdef', 'f', '', 'fabcde')
    _test(reverse, 'edcba', 0, 4, 'abcde')
    _test(reverse, 'abcdef', 2, 4, 'abedcf')
    _test(move, 'abcdefg', 3, 5, 'abcefdg')

# swap position X with position Y
# swap letter X with letter Y
# rotate left/right X steps
# rotate based on position of letter X
# reverse positions X through Y
# move position X to position Y

def maybe_int(value):
    try:
        return int(value)
    except ValueError:
        return value

_expressions = [
    (re.compile(r'swap position (\d+) with position (\d+)'), swap_pos),
    (re.compile(r'swap letter (\w+) with letter (\w+)'), swap_letter),
    (re.compile(r'rotate ((?:left)|(?:right)) (\d+) step(?:s)?'), rotate_steps),
    (re.compile(r'rotate based on position of letter (\w+)()'), rotate_letter),
    (re.compile(r'reverse positions (\d+) through (\d+)'), reverse),
    (re.compile(r'move position (\d+) to position (\d+)'), move),
]

def parse_instructions(inst_str):
    insts = []
    for line in inst_str:
        line = line.strip()
        any_match = False
        for patt, fn in _expressions:
            m = patt.match(line)
            if m is not None:
                any_match = True
                arg1, arg2 = maybe_int(m.group(1)), maybe_int(m.group(2))
                insts.append((fn, arg1, arg2))
                break
        assert any_match, "no match for %s" % line
    return insts

def scramble(s, instructions):
    for fn, arg1, arg2 in instructions:
        s = fn(s, arg1, arg2)
    return s

def unscramble(target, instructions):
    n = 0
    target_exploded = [c for c in target]
    for t in itertools.permutations(target_exploded):
        n += 1
        u = scramble(t, instructions)
        if _DEBUG:
            print >> sys.stderr, "%5d %s -> %s" % (n, t, u)
        if u == target_exploded:
            return ''.join(t)

def main(seed, target):
    _run_tests()
    instructions = parse_instructions(sys.stdin)
    s = [c for c in seed]
    print seed, '->', ''.join(scramble(s, instructions))
    t = unscramble(target, instructions)
    if t is None:
        print >> sys.stderr, "no permutation of %s can be unscrambled" % target
        return 1
    print t, '->', target
    return 0

if __name__ == '__main__':
    sys.exit(main('abcdefgh', 'fbgdceah'))

# Expected output:
#     abcdefgh -> fdhbcgea
#     egfbcadh -> fbgdceah
