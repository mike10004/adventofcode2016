#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#  advent1_easterbunny.py
#  

import sys, operator

ORIENTATIONS = "NESW"

DISTANCE_FACTORS = {
  'N': (0, 1),
  'E': (1, 0),
  'S': (0, -1),
  'W': (-1, 0)
}

DIRECTION_OFFSETS = {'L': -1, 'R': 1}

def taxicab_magnitude(position):
    return sum([abs(m) for m in position])

def index_of_duplicate(values):
    for i in xrange(len(values)):
        for j in xrange(i + 1, len(values)):
            if values[i] == values[j]:
                return i

def nonzero(seq):
    for s in seq:
        if s != 0:
            return s
    raise ValueError("no nonzero values in " + str(seq))

def add(tuple1, tuple2):
    return tuple(map(operator.add, tuple1, tuple2))

class Agent:
    
    def __init__(self, position=(0, 0), orientation_index=0):
        self.position = position
        self.orientation_index = orientation_index
        self.positions_visited = [position]
    
    def reorient(self, direction):
        self.orientation_index = (self.orientation_index + DIRECTION_OFFSETS[direction]) % len(ORIENTATIONS)
    
    def walk(self, distance):
        factor = DISTANCE_FACTORS[ORIENTATIONS[self.orientation_index]]
        offset = distance * factor[0], distance * factor[1]
        previous = self.position 
        step_range = range(nonzero(factor), nonzero(offset), nonzero(factor))
        offset_steps = [(abs(factor[0]) * step, abs(factor[1]) * step) for step in step_range]
        for offset_step in offset_steps:
            self.positions_visited.append(add(previous, offset_step))
        self.position = add(self.position, offset)
        self.positions_visited.append(self.position)
    
    def move(self, direction, distance):
        self.reorient(direction)
        self.walk(distance)
        
    
    def __str__(self):
        pos = str(self.position)
        ori = ORIENTATIONS[self.orientation_index]
        return "%s, %s (%d blocks from start)" % (pos, ori, taxicab_magnitude(self.position))

def parse_move(token):
    direction = token[0]
    assert direction == 'L' or direction == 'R', "invalid direction: " + direction
    distance = int(token[1:])
    return direction, distance

def main(args):
    me = Agent()
    input_text = sys.stdin.read().strip()
    moves = [parse_move(token.strip()) for token in input_text.split(',')] if len(input_text) > 0 else ()
    for move in moves:
        me.move(move[0], move[1])
    print me
    print len(me.positions_visited), "position(s) visited"
    first_dupe_position_index = index_of_duplicate(me.positions_visited)
    if first_dupe_position_index is None:
        print >> sys.stderr, "no duplicate positions"
    else:
        dupe = me.positions_visited[first_dupe_position_index]
        print "%s visited twice; %d blocks away" % (str(dupe), taxicab_magnitude(dupe))
    return 0

if __name__ == '__main__':
    sys.exit(main(sys.argv))
