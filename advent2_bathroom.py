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

def is_int_pair(t):
    return len(t) == 2 and type(t[0]) == int and type(t[1]) == int

def add_clamped(tuple1, tuple2, clamp1, clamp2):
    assert is_int_pair(tuple1)
    assert is_int_pair(tuple2)
    assert is_int_pair(clamp1)
    assert is_int_pair(clamp2)
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
    
    def label(self, position):
        return self.labels[self.positions.index(position)]
    
    def position(self, label):
        return self.positions[self.labels.index(label)]
    
    def x_bounds_at(self, position):
        x, y = position
        assert type(x) == int and type(y) == int, "position must be (int, int) tuple: " + str(position)
        horizontals = [y0 for x0, y0 in filter(lambda p: p[0] == x, self.positions)]
        assert len(horizontals) > 0, "no horizontals among " + str(self.positions) + " at position " + str(position)
        return min(horizontals), max(horizontals)
        
    def y_bounds_at(self, position):
        x, y = position
        assert type(x) == int and type(y) == int, "position must be (int, int) tuple: " + str(position)
        verticals = [x0 for x0, y0 in filter(lambda p: p[1] == y, self.positions)]
        return min(verticals), max(verticals)
        
    
class Finger:
    
    def __init__(self, position):
        self.position = position
    
    def move(self, pad, direction):
        offset = OFFSETS[direction]
        self.position = add_clamped(self.position, offset, pad.x_bounds_at(self.position), pad.y_bounds_at(self.position))

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
