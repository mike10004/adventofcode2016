/******************************************************************************

--- Day 9: Explosives in Cyberspace ---

--- Part One ---

Wandering around a secure area, you come across a datalink port to a new part 
of the network. After briefly scanning it for interesting files, you find one 
file in particular that catches your attention. It's compressed with an 
experimental format, but fortunately, the documentation for the format is nearby.

The format compresses a sequence of characters. Whitespace is ignored. To 
indicate that some sequence should be repeated, a marker is added to the file, 
like (10x2). To decompress this marker, take the subsequent 10 characters and 
repeat them 2 times. Then, continue reading the file after the repeated data. 
The marker itself is not included in the decompressed output.

If parentheses or other characters appear within the data referenced by a 
marker, that's okay - treat it like normal data, not a marker, and then 
resume looking for markers after the decompressed section.

For example:

* ADVENT contains no markers and decompresses to itself with no changes, 
  resulting in a decompressed length of 6.
* A(1x5)BC repeats only the B a total of 5 times, becoming ABBBBBC for a 
  decompressed length of 7.
* (3x3)XYZ becomes XYZXYZXYZ for a decompressed length of 9.
* A(2x2)BCD(2x2)EFG doubles the BC and EF, becoming ABCBCDEFEFG for a 
  decompressed length of 11.
* (6x1)(1x3)A simply becomes (1x3)A - the (1x3) looks like a marker, but 
  because it's within a data section of another marker, it is not treated 
  any differently from the A that comes after it. It has a decompressed 
  length of 6.
* X(8x2)(3x3)ABCY becomes X(3x3)ABC(3x3)ABCY (for a decompressed length of 18), 
  because the decompressed data from the (8x2) marker (the (3x3)ABC) is skipped 
  and not processed further.

What is the decompressed length of the file (your puzzle input)? Don't count 
whitespace.

Your puzzle answer was 123908.

The first half of this puzzle is complete! It provides one gold star: *

--- Part Two ---

Apparently, the file actually uses version two of the format.

In version two, the only difference is that markers within decompressed data 
are decompressed. This, the documentation explains, provides much more 
substantial compression capabilities, allowing many-gigabyte files to be stored 
in only a few kilobytes.

For example:

* (3x3)XYZ still becomes XYZXYZXYZ, as the decompressed section contains no markers.
* X(8x2)(3x3)ABCY becomes XABCABCABCABCABCABCY, because the decompressed data 
  from the (8x2) marker is then further decompressed, thus triggering the 
  (3x3) marker twice for a total of six ABC sequences.
* (27x12)(20x12)(13x14)(7x10)(1x12)A decompresses into a string of A repeated 
  241920 times.
* (25x3)(3x3)ABC(2x3)XY(5x2)PQRSTX(18x9)(3x2)TWO(5x7)SEVEN becomes 445 
  characters long.

Unfortunately, the computer you brought probably doesn't have enough memory to 
actually decompress the file; you'll have to come up with another way to get 
its decompressed length.

What is the decompressed length of the file using this improved format? (10755693147)

******************************************************************************/

var markerPattern = /\((\d+)x(\d+)\)/;

function Marker(n, text) {
  this.count = function() {
    var m = markerPattern.exec(text);
    if (m === null) {
      return n * text.length;
    } else {
      var headCount = m.index;
      var bodyLen = parseInt(m[1]), bodyN = parseInt(m[2]);
      var bodyText = text.substr(m.index + m[0].length, bodyLen);
      var body = new Marker(bodyN, bodyText);
      var tailText = text.substr(m.index + m[0].length + bodyLen);
      var tail = new Marker(1, tailText);
      return n * (headCount + body.count() + tail.count()); 
    }
  }
}

function computeDecompressedLength(input) {
  var root = new Marker(1, input);
  return root.count();
}

(function(testCases, log){
  log = log || (()=>false);
  var assert = require('assert');
  testCases.forEach(testCase => {
    var actual = computeDecompressedLength(testCase.input);
    assert.equal(testCase.expected, actual, "on input " + testCase.input + " expected " + testCase.expected + " but got " + actual);
    log(testCase.input + " -> " + actual + "\n"); 
  });
})([
  {input: 'ADVENT', expected: 'ADVENT'.length},
  {input: '(1x2)A', expected: 'AA'.length},
  {input: 'A(1x2)B', expected: 'ABB'.length},
  {input: '(1x2)BC', expected: 'BBC'.length},
  {input: 'L(1x2)CR', expected: 'LCCR'.length},
  {input: 'C(2x3)DE', expected: 'CDEDEDE'.length},
  {input: 'C(7x2)L(1x5)R', expected: 'CLRRRRRLRRRRR'.length}, // 13
  {input: 'C(2x3)DEXC(2x3)DE', expected: 'CDEDEDEXCDEDEDE'.length}, // 15
  {input: 'C(2x3)DEC(2x3)DE', expected: 'CDEDEDECDEDEDE'.length},
  {input: 'C(7x2)L(1x5)RC(7x2)L(1x5)R', expected: 'CLRRRRRLRRRRRCLRRRRRLRRRRR'.length},
  {input: '(3x3)XYZ', expected: 'XYZXYZXYZ'.length},
  {input: 'X(8x2)(3x3)ABCY', expected: 'XABCABCABCABCABCABCY'.length},
  {input: '(27x12)(20x12)(13x14)(7x10)(1x12)A', expected: 241920},
  {input: '(25x3)(3x3)ABC(2x3)XY(5x2)PQRSTX(18x9)(3x2)TWO(5x7)SEVEN', expected: 445}
]);

(function(consumer){
  process.stdin.setEncoding('utf8');
  var consumed = false;
  process.stdin.on('readable', () => {
    var chunk = process.stdin.read();
    if (chunk == null) {
      if (!consumed) {
        console.error("no data on stdin");
      }
      process.exit(consumed ? 0 : 1);
    } else {
      consumed = true;
      consumer(chunk);
    }
  });
})((input)=>{
  var originalLength = input.length;
  input = input.split().join('').trim(); // remove whitespace
  console.log("input length " + input.length + " (trimmed from " + originalLength + ")");
  var output = computeDecompressedLength(input);
  console.log("decompressed length = " + output);
});

