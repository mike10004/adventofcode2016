var pattern = /\((\d+)x(\d+)\)/;

function decompress(input, log){
    log = log || (()=>false);
    function repeat(x, n) {
        var a = new Array(n);
        for (var i = 0; i < n; i++) {
            a[i] = x;
        }
        return a.join('');
    }

    var portion = input, position = 0;
    var output = [];
    while (true) {
        var m = pattern.exec(portion);
        output.push(portion.substr(0, m === null ? portion.length : m.index));
        if (m === null) {
            break;
        }
        var dataSectionLength = parseInt(m[1], 10), n = parseInt(m[2], 10);
        var dataSection = portion.substr(m.index + m[0].length, dataSectionLength);
        output.push(repeat(dataSection, n));
        portion = portion.substr(m.index + m[0].length + dataSectionLength);
    }
    var decompressed = output.join('');
    log("decompressed", input, "->", decompressed);
    return decompressed;
};

var assert = require('assert');

[
    ['ADVENT', 'ADVENT'],
    ['A(1x5)BC', 'ABBBBBC'],
    ['(3x3)XYZ', 'XYZXYZXYZ'],
    ['A(2x2)BCD(2x2)EFG', 'ABCBCDEFEFG'],
    ['(6x1)(1x3)A', '(1x3)A'],
    ['X(8x2)(3x3)ABCY', 'X(3x3)ABC(3x3)ABCY']    
].forEach(testCase => {
    var decompressed = decompress(testCase[0]);
    assert.equal(testCase[1], decompressed, "expected " + decompressed + " to equal " + testCase[1]);
});

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
})(function(input, verbose){
    var length0 = input.length;
    input = input.trim().split().join(''); // collapse whitespace
    var output = decompress(input);
    if (verbose) {
        console.log(output);
        console.log();
    }
    console.log(output.length + " characters decompressed from input of length " + length0);
});