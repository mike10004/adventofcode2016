var assert = require('assert');

/*
Call the data you have at this point "a".
Make a copy of "a"; call this copy "b".
Reverse the order of the characters in "b".
In "b", replace all instances of 0 with 1 and all 1s with 0.
The resulting data is "a", then a single 0, then "b".
*/
function dragonify(a, minLength) {
    var b = a.split('').reverse().map(ch => ch == '0' ? '1' : '0').join('');
    var d = a + '0' + b;
    if (d.length < minLength) {
        return dragonify(d, minLength);
    } 
    return d;
}

[
    {input: '1', expected: '100'},
    {input: '0', expected: '001'},
    {input: '11111', expected: '11111000000'},
    {input: '111100001010', expected: '1111000010100101011110000'}
].forEach(testCase => {
    var actual = dragonify(testCase.input, 0);
    assert.equal(actual, testCase.expected, "dragonify: expected " + testCase.expected + " != " + actual + " actual");
});

/*
The checksum for some given data is created by considering each non-overlapping 
pair of characters in the input data. If the two characters match (00 or 11), 
the next checksum character is a 1. If the characters do not match (01 or 10), 
the next checksum character is a 0. This should produce a new string which is 
exactly half as long as the original. If the length of the checksum is even, 
repeat the process until you end up with a checksum with an odd length.
*/
function computeChecksum(d, length) {
    d = d.length > length ? d.substr(0, length) : d;
    var s = '';
    for (var i = 0; i < d.length; i += 2) {
        s += (d[i] === d[i+1] ? '1' : '0');
    }
    return s.length % 2 === 0 ? computeChecksum(s, length) : s;
}

[{input: '110010110100', expected: '100'}].forEach(testCase => {
    var actual = computeChecksum(testCase.input, testCase.input.length);
    assert.equal(actual, testCase.expected, "computeChecksum: expected " + testCase.expected + " != " + actual + " actual");
});

[{input: '10000', diskSize: 20, expected: '01100'}].forEach(testCase => {
    var actual = computeChecksum(dragonify(testCase.input, testCase.diskSize), testCase.diskSize);
    assert.equal(actual, testCase.expected, "dragonify->checksum: expected " + testCase.expected + " != " + actual + " actual");
});

function doPartOne(input, diskSize) {
    var dragonified = dragonify(input, diskSize);
    var checksum = computeChecksum(dragonified, diskSize);
    console.log(checksum);
}

var puzzleInput = '11101000110010100';
var partOneDiskSize = 272;
doPartOne(puzzleInput, partOneDiskSize);
