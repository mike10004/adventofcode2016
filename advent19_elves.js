var assert = require('assert');

function findPresentsToLeft(elves, current) { // 'left' actually means higher-indexed in the array
    for (var i = 1; i <= elves.length; i++) {
        var index = (current + i) % elves.length;
        if (elves[index] > 0) {
            return index;
        }
    }
    throw 'no presents among all ' + elves.length + ' elves';
}

function advance(elves, turn) {
    if (elves[turn] === 0) {
        return true;
    }
    var indexOfLoser = findPresentsToLeft(elves, turn);
    if (indexOfLoser === turn) {
        // game stops because nobody else has presents
        return false;
    }
    elves[turn] += elves[indexOfLoser];
    elves[indexOfLoser] = 0;
    return true;
}

function indexOf(array, predicate) {
    for (var i = 0; i < array.length; i++) {
        if (predicate(array[i])) {
            return i;
        }
    }
    return -1;
}

function play(elves, callback) {
    var turn = 0, round = 0;
    while (advance(elves, turn)) {
        turn++;
        turn = turn % elves.length;
        round++;
        callback(elves, turn, round);
    }
    var winner = indexOf(elves, elf => elf > 0);
    return winner;
}

function onePerElf(numElves, value) {
    var elves = new Array();
    for (var i = 0; i < numElves; i++) {
        elves.push(typeof(value) === 'undefined' ? 1 : value);
    }
    return elves;
}

/*
Elf 1 takes Elf 2's present.
Elf 2 has no presents and is skipped.
Elf 3 takes Elf 4's present.
Elf 4 has no presents and is also skipped.
Elf 5 takes Elf 1's two presents.
Neither Elf 1 nor Elf 2 have any presents, so both are skipped.
Elf 3 takes Elf 5's three presents.
*/
(function doUnitTest(){
    assert.equal(findPresentsToLeft([1], 0), 0);
    assert.equal(findPresentsToLeft([1, 0], 1), 0);
    assert.equal(findPresentsToLeft([1, 0], 0), 0);
    assert.equal(findPresentsToLeft([0, 1], 0), 1);
    assert.equal(findPresentsToLeft([1, 0, 1, 1, 0, 0, 1], 0), 2);
    assert.equal(findPresentsToLeft([1, 0, 1, 1, 0, 0, 1], 1), 2);
    assert.equal(findPresentsToLeft([1, 0, 1, 1, 0, 0, 1], 2), 3);
    assert.equal(findPresentsToLeft([1, 0, 1, 1, 0, 0, 1], 4), 6);
    assert.equal(findPresentsToLeft([1, 0, 1, 1, 0, 0, 0], 3), 0);
    assert.equal(findPresentsToLeft([0, 0, 1, 1, 0, 0, 1], 6), 2);
    assert.deepEqual(onePerElf(5), [1, 1, 1, 1, 1]);
    var winnerIndex = play(onePerElf(5), (elves, turn, round) => {
        console.error(elves, turn, round);
        assert.equal(elves.length, 5, 'elves array length changed');
    });
    assert.equal(winnerIndex + 1, 3);
})();