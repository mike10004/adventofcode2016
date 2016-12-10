#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#  advent2_bathroom.py
#  

import re
import sys

class Tracker:

    def __init__(self):
        self.moves = []  # list of (bot.tag, chip1, chip2) tuples

    def track(self, tag, a, b):
        self.moves.append((tag, a, b))

_TRACKER = Tracker()

_GT = lambda x, y: x > y
_LT = lambda x, y: x < y

class ChipHolder:

    def __init__(self, tag):
        self.tag = tag
        self.chips = []

    def accept(self, chip):
        self.chips.append(chip)

    def __str__(self):
        return "{id=%s,chips=%s}" % (str(self.tag), str(self.chips))

class Bin(ChipHolder):
    pass

class Bot(ChipHolder):

    def ready(self):
        return len(self.chips) == 2

    def compare(self, comparator):
        assert self.ready()
        a, b = map(int, self.chips)
        _TRACKER.track(self.tag, a, b)
        return a if comparator(a, b) else b

    def low(self):
        return self.compare(_LT)

    def high(self):
        return self.compare(_GT)

    def give_low(self, holder):
        holder.accept(self.low())
    
    def give_high(self, holder):
        holder.accept(self.high())

# value 5 goes to bot 2
# bot 2 gives low to bot 1 and high to bot 0
# value 3 goes to bot 1
# bot 1 gives low to output 1 and high to bot 0
# bot 0 gives low to output 2 and high to output 0
# value 2 goes to bot 2

class Parser:

    def __init__(self, pattern):
        self.pattern = pattern
    
    def act(self, groups, holders):
        raise NotImplementedError()

    def process(self, instruction, holders):
        m = self.pattern.match(instruction)
        if m is not None:
            g = [m.group(0)] + list(m.groups())
            self.act(g, holders)

class AssignmentParser(Parser):

    def __init__(self):
        Parser.__init__(self, re.compile(r'^value (\d+) goes to bot (\d+)$'))
    
    def act(self, g, holders):
        assert len(g) == 3
        holders['bot'][g[2]].accept(g[1])

class ActionParser(Parser):

    def __init__(self):
        Parser.__init__(self, re.compile(r'^bot (\d+) gives low to (\w+) (\d+) and high to (\w+) (\d+)$'))
    
    def act(self, g, holders):
        assert len(g) == 6
        bot = holders['bot'][g[1]]
        bot.give_low(holders[g[2]][g[3]])
        bot.give_high(holders[g[4]][g[5]])

class HolderDict:

    def __init__(self, clazz):
        self.d = {}
        self.callable = clazz
    
    def __getitem__(self, k):
        if k not in self.d:
            self.d[k] = self.callable(k)
        return self.d[k]
    
    def values(self):
        return self.d.values()

def main(args):
    instructions = [line.strip() for line in sys.stdin.readlines()]
    bots = HolderDict(Bot)
    bins = HolderDict(Bin)
    holders = {
        'bot': bots,
        'output': bins,
    }
    assp = AssignmentParser()
    for inst in instructions:
        assp.process(inst, holders)
    actp = ActionParser()
    for inst in instructions:
        actp.process(inst, holders)
    for output in bins.values():
        print 'output:', output
    for move in set(_TRACKER.moves):
        print 'move:', move
    return 0

if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
