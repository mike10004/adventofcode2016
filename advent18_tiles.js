var SAFE = '.', TRAP = '^';

function isSafe(tile) {
    if (typeof(tile) === 'undefined') {
        return true;
    }
    return tile === SAFE;
}

/*
Its left and center tiles are traps, but its right tile is not.
Its center and right tiles are traps, but its left tile is not.
Only its left tile is a trap.
Only its right tile is a trap.
*/
function newTile(left, center, right) {
    if (!isSafe(left) && !isSafe(center) && isSafe(right)) {
        return TRAP;
    }
    if (!isSafe(center) && !isSafe(right) && isSafe(left)) {
        return TRAP;
    }
    if (!isSafe(left) && isSafe(center) && isSafe(right)) {
        return TRAP;
    }
    if (!isSafe(right) && isSafe(center) && isSafe(left)) {
        return TRAP;
    }
    return SAFE;
}

function nextRow(row) {
    var next = '';
    for (var i = 0; i < row.length; i++) {
        var left = row[i - 1], center = row[i], right = row[i + 1];
        next += newTile(left, center, right);
    }
    return next;
}

function makeField(firstRow, numRows) {
    var rows = [firstRow];
    var row = firstRow;
    while (rows.length < numRows) {
        row = nextRow(row);
        rows.push(row);
    }
    return rows;
}

function countSafe(field) {
    if (typeof(field) === 'string') {
        return field.split('').filter(isSafe).length;
    }
    return field.map(countSafe).reduce((a, b) => a + b, 0);
}

function doUnitTest() {
    var assert = require('assert');
    assert.equal(nextRow('..^^.'), '.^^^^');
    assert.equal(countSafe(makeField('..^^.', 3)), 6);
    assert.equal(countSafe(makeField('.^^.^.^^^^', 10)), 38);
}

doUnitTest();

var puzzleInput = '.^^..^...^..^^.^^^.^^^.^^^^^^.^.^^^^.^^.^^^^^^.^...^......^...^^^..^^^.....^^^^^^^^^....^^...^^^^..^';
var partOneField = makeField(puzzleInput, 40);
console.log(countSafe(partOneField));