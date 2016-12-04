#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#  advent4_rooms.py
#  

import sys, re

def create_cmp(encrypted_name):
    def custom_cmp(a, b):
        na = len(filter(lambda x: x == a, encrypted_name))
        nb = len(filter(lambda x: x == b, encrypted_name))
        if na == nb:
            return cmp(a, b)
        return nb - na
    return custom_cmp

class Room:
    
    def __init__(self, name, sector, checksum):
        self.name = name
        self.sector = sector
        self.checksum = checksum
    
    def check_real(self):
        return self.compute_checksum() == self.checksum
    
    def compute_checksum(self):
        name_chars = set(filter(lambda c: c >= 'a' and c <= 'z', self.name))
        assert len(name_chars) >= 5, 'name does not have enough unique chars: ' + self.name + '  ' + str(name_chars)
        c = create_cmp(self.name)
        s = ''.join(sorted(name_chars, cmp=c)[:5])
        return s 

def parse_room(code):
    patt = re.compile(r'^([-a-z]+)-(\d+)\[(\w+)\]$')
    m = patt.match(code)
    assert m is not None, "unexpected format: %s" % code
    return Room(m.group(1), int(m.group(2)), m.group(3))

_TEST_CASES = [
    ('aaaaa-bbb-z-y-x-123[abxyz]', True),
    ('totally-real-room-200[decoy]', False),
    ('a-b-c-d-e-f-g-h-987[abcde]', True),
    ('not-a-real-room-404[oarel]', True),
    ('chobani-pom-yogurt-7[oabcg]', True),
    ('four-percent-milkfat-19[efprc]', False)
]

def main(args):
    for room_code, expected in _TEST_CASES:
        room = parse_room(room_code)
        assert room.check_real() == expected, (room_code, expected, room.compute_checksum())
    if len(args) == 0:
        input_text = sys.stdin.read()
    else:
        input_text = ' '.join(args)
    room_codes = input_text.split()
    rooms = map(parse_room, room_codes)
    real_rooms = filter(lambda room: room.check_real(), rooms)
    sector_sum = sum([room.sector for room in real_rooms])
    print "%d is sum of sector IDs of %d real rooms out of %d codes" % (sector_sum, len(real_rooms), len(rooms))
    return 0

if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
