#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#  advent2_bathroom.py
#  

import itertools, operator, sys

clamp_above = min
clamp_below = max

def clamp(value, lower_bound, upper_bound):
    return clamp_above(clamp_below(value, lower_bound), upper_bound)

def add_clamped(tuple1, tuple2, clamp1, clamp2):
    unclamped = tuple(map(operator.add, tuple1, tuple2))
    return clamp(unclamped[0], clamp1[0], clamp1[1]), clamp(unclamped[1], clamp2[0], clamp2[1])

OFFSETS = {
    'U': (0, 1),
    'R': (1, 0),
    'D': (0, -1),
    'L': (-1, 0)
}

class Pad:
    
    positions = tuple(itertools.product(range(-1, 2), range(-1, 2)))
    labels = "741852963"
    
    def __init__(self):
        self.bounds = tuple((min([p[idx] for p in self.positions]), max([p[idx] for p in self.positions])) for idx in (0, 1))
    
    def label(self, position):
        return self.labels[self.positions.index(position)]
    
    def position(self, label):
        return self.positions[self.labels.index(label)]
    
class Finger:
    
    def __init__(self, position):
        self.position = position
    
    def move(self, pad, direction):
        offset = OFFSETS[direction]
        self.position = add_clamped(self.position, offset, pad.bounds[0], pad.bounds[1])

def main(args):
    pad = Pad()
    f = Finger(pad.position('5'))
    result = ''
    for moves in sys.stdin.readlines():
        for move in moves.strip():
            f.move(pad, move)
        result += pad.label(f.position)
    print result
    return 0

if __name__ == '__main__':
    import sys
    sys.exit(main(sys.argv))
