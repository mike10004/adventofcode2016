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
    
    def __init__(self, positions, labels):
        self.positions = positions
        self.labels = labels
    
    def label(self, position, default=None):
        try:
            return self.labels[self.positions.index(position)]
        except ValueError:
            if default is None:
                raise
            return default
    
    def position(self, label):
        return self.positions[self.labels.index(label)]
    
    def y_bounds_at(self, position):
        x, y = position
        horizontals = [y0 for x0, y0 in filter(lambda p: p[0] == x, self.positions)]
        return min(horizontals), max(horizontals)
        
    def x_bounds_at(self, position):
        x, y = position
        verticals = [x0 for x0, y0 in filter(lambda p: p[1] == y, self.positions)]
        return min(verticals), max(verticals)
    
    
    def draw(self, blank=' '):
        xs, ys = [p[0] for p in self.positions], [p[1] for p in self.positions]
        min_x, max_x = min(xs), max(xs)
        min_y, max_y = min(ys), max(ys)
        drawing = ''
        for y in xrange(0, max_y - min_y + 1):
            y += min_y
            for x in xrange(0, max_x - min_x + 1):
                x += min_x
                drawing += self.label((x, -y), blank)
            drawing += '\n'
        return drawing.rstrip('\n')
        
class StandardPad(Pad):
    
    def __init__(self):
        Pad.__init__(self, tuple(itertools.product(range(-1, 2), range(-1, 2))), "741852963")

class DiamondPad(Pad):
    
    def __init__(self):
        d = {
                                    '1': (0, 2),
                      '2': (-1, 1), '3': (0, 1), '4': (1, 1),
        '5': (-2, 0), '6': (-1, 0), '7': (0, 0), '8': (1, 0), '9': (2, 0),
                      'A': (-1,-1), 'B': (0,-1), 'C': (1,-1), 
                                    'D': (0,-2)
        }
        labels = ''.join(label for label in d)
        positions = tuple([d[label] for label in d])
        Pad.__init__(self, positions, labels)

class Finger:
    
    def __init__(self, position):
        self.position = position
    
    def move(self, pad, direction):
        offset = OFFSETS[direction]
        self.position = add_clamped(self.position, offset, pad.x_bounds_at(self.position), pad.y_bounds_at(self.position))

def main(args):
    from argparse import ArgumentParser
    parser = ArgumentParser()
    parser.add_argument("--start", default="5")
    parser.add_argument("--pad", choices=('square', 'diamond'), default='diamond')
    parser.add_argument("input", nargs="?", default=None)
    args = parser.parse_args()
    pad = DiamondPad() if args.pad == 'diamond' else StandardPad()
    print pad.draw()
    print "---"
    f = Finger(pad.position(args.start))
    result = ''
    input_text = args.input or sys.stdin.read().strip()
    if len(input_text) > 0:
        for moves in input_text.split('\n'):
            for move in moves.strip():
                f.move(pad, move)
            result += pad.label(f.position)
        print result
    return 0

if __name__ == '__main__':
    import sys
    sys.exit(main(sys.argv))
