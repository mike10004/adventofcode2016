var pattern = /\((\d+)x(\d+)\)/;

function repeat(x, n) {
    var a = new Array(n);
    for (var i = 0; i < n; i++) {
        a[i] = x;
    }
    return a.join('');
}

function decompress(input, recursive, log){
    log = log || (()=>false);
    var portion = input, position = 0;
    var output = [];
    while (true) {
        var m = pattern.exec(portion);
        var chunk = portion.substr(0, m === null ? portion.length : m.index);
        output.push(recursive ? chunk.length : chunk);
        if (m === null) {
            break;
        }
        var dataSectionLength = parseInt(m[1], 10), n = parseInt(m[2], 10);
        var dataSection = portion.substr(m.index + m[0].length, dataSectionLength);
        var exploded = repeat(dataSection, n);
        var tail = portion.substr(m.index + m[0].length + dataSectionLength);
        if (recursive) {
            portion = exploded + tail;
        } else {
            output.push(exploded);
            portion = tail;
        }
    }
    if (recursive) {
        var sum = output.reduce((a, b) => a + b, 0);
        return sum;
    } else {
        var decompressed = output.join('');
        log("decompressed", input.substr(0, 64), "->", decompressed.substr(0, 64));
        return decompressed;
    }
    
};

var assert = require('assert');

function test(testCase, recursive) {
    var decompressed = decompress(testCase[0], recursive, console.error);
    var expected = testCase[1];
    assert.equal(recursive, typeof(expected) === 'number', "if recursive, 'expected' must be a number");
    assert.equal(expected, decompressed, (recursive ? "(recursive) " : "(flat) ") + "on input " + testCase[0] + " expected " + decompressed + " to equal " + expected);
};

[
    ['ADVENT', 'ADVENT'],
    ['A(1x5)BC', 'ABBBBBC'],
    ['(3x3)XYZ', 'XYZXYZXYZ'],
    ['A(2x2)BCD(2x2)EFG', 'ABCBCDEFEFG'],
    ['(6x1)(1x3)A', '(1x3)A'],
    ['X(8x2)(3x3)ABCY', 'X(3x3)ABC(3x3)ABCY']    
].forEach(testCase => test(testCase, false));

[
    ['(3x3)XYZ', 'XYZXYZXYZ'.length],
    ['X(8x2)(3x3)ABCY', 'XABCABCABCABCABCABCY'.length],
    ['(27x12)(20x12)(13x14)(7x10)(1x12)A', 241920],
    ['(25x3)(3x3)ABC(2x3)XY(5x2)PQRSTX(18x9)(3x2)TWO(5x7)SEVEN', 445]    
].forEach(testCase => test(testCase, true));

(function(handler){
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
      handler(chunk);
    }
  });
})(function(input){
    var MODE_RECURSIVE = true;
    input = input.trim().split().join(''); // collapse whitespace
    var output = decompress(input, MODE_RECURSIVE);
    console.log(output);
    if (!MODE_RECURSIVE) {
        console.error(output.length + " characters in decompressed output");
    }
});