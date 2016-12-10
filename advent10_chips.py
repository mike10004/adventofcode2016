#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#  advent2_bathroom.py
#  

import re
import sys
import operator 

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
        assert isinstance(chip, basestring), "expect chip to be string, but %s is %s" % (str(chip), str(type(chip)))
        self.chips.append(chip)

    def __str__(self):
        return "{id=%s,chips=%s}" % (str(self.tag), str(self.chips))

class Bin(ChipHolder):
    pass

class NotReadyError(Exception):
    pass

class Bot(ChipHolder):

    def ready(self):
        return len(self.chips) == 2

    def compare(self, comparator):
        if not self.ready():
            raise NotReadyError("Bot %s does not have 2 chips to compare: %s" % (self.tag, str(self.chips)))
        a, b = map(int, self.chips)
        _TRACKER.track(self.tag, a, b)
        return str(a) if comparator(a, b) else str(b)

    def low(self):
        return self.compare(_LT)

    def high(self):
        return self.compare(_GT)

    def give(self, chip, holder):
        try:
            self.chips.remove(chip)
        except ValueError:
            print >> sys.stderr, "bot %s cannot give chip %s: chips=%s" % (self.tag, chip, self.chips)
            raise
        holder.accept(chip)

class Parser:

    def __init__(self, pattern):
        self.pattern = pattern
    
    def act(self, groups, holders):
        raise NotImplementedError()

    def process(self, instruction, holders):
        m = self.pattern.match(instruction)
        if m is not None:
            g = [m.group(0)] + list(m.groups())
            return self.act(g, holders)

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
        if bot.ready():
            low, high = bot.low(), bot.high()
            bot.give(low, holders[g[2]][g[3]])
            bot.give(high, holders[g[4]][g[5]])
            return True
        return False

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
    for bot in bots.values():
        if args.verbose: 
            print 'bot:', bot
    num_moves = None
    num_rounds = 0
    while num_moves is None or num_moves > 0:
        num_moves = 0
        for inst in instructions:
            if actp.process(inst, holders):
                num_moves += 1
        num_rounds += 1
        if args.verbose: 
            print num_moves, 'in round', num_rounds
    for output in bins.values():
        if args.verbose: 
            print 'output:', output
    for move in set(_TRACKER.moves):
        if args.verbose: 
            print 'move:', move
    if args.find_move is not None:
        a, b = args.find_move
        for tag, x, y in set(_TRACKER.moves):
            if (x == a and y == b) or (x == b and y == a):
                print "found:", (tag, x, y)
    if args.product is not None:
        targets = [bins[b] for b in args.product]
        subproducts = [reduce(operator.mul, map(int, b.chips), 1) for b in targets]
        product = reduce(operator.mul, subproducts, 1)
        print 'product of', args.product, '=', product
    return 0

if __name__ == '__main__':
    from argparse import ArgumentParser
    p = ArgumentParser()
    p.add_argument("--verbose", action="store_true", default=False)
    p.add_argument("--find-move", metavar="ID", type=int, nargs=2, help="find the move that compared chips with given IDs")
    p.add_argument("--product", nargs='+', help="compute product of output bins", metavar="BIN")
    args = p.parse_args()
    sys.exit(main(args))
