var assert = require('assert');
var utils = require('./utils.js');

function findPresents(elves, current, targeter) {
    var target = targeter(elves, current);
    assert(typeof(target) !== 'undefined', 'no target found with ' + targeter);
    return target;
}

var leftPresentFinder = function(elves, current) {
    for (var i = 1; i <= elves.length; i++) {
        var index = (current + i) % elves.length;
        if (elves[index] > 0) {
            return index;
        }
    }
};

function findPresentsToLeft(elves, current) { // 'left' actually means higher-indexed in the array
    return findPresents(elves, current, leftPresentFinder);
}

var acrossPresentFinder = function(elves, current) {
    var numWithPresents = utils.count(elves, elf => elf > 0);
    var halfway = parseInt(numWithPresents / 2);
    var n = 0;
    for (var i = 1; i <= elves.length; i++) {
        var index = (current + i) % elves.length;
        if (elves[index] > 0) {
            n++;
            if (n >= halfway) {
                return index;
            }
        } 
    }
};

var removeLoser = function(elves, indexOfLoser) {
    elves.splice(indexOfLoser, 1);
}

function advance(elves, turn, presentFinder, loserCallback) {
    if (elves[turn] === 0) {
        return true;
    }
    var indexOfLoser = findPresents(elves, turn, presentFinder);
    if (indexOfLoser === turn) {
        // game stops because nobody else has presents
        return false;
    }
    elves[turn] += elves[indexOfLoser];
    elves[indexOfLoser] = 0;
    if (loserCallback) {
        loserCallback(elves, indexOfLoser);
    }
    return true;
}

function play(elves, presentFinder, loserCallback) {
    var turn = 0, round = 0;
    while (advance(elves, turn, presentFinder, loserCallback)) {
        turn++;
        turn = turn % elves.length;
        round++;
    }
    var winner = utils.indexOf(elves, elf => elf > 0);
    return winner;
}

function onePerElf(numElves, value) {
    return utils.repeat(numElves, 1);
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
    // assert.equal(acrossPresentFinder([1], 0), 0);
    // assert.equal(acrossPresentFinder([1, 1], 0), 1);
    // assert.equal(acrossPresentFinder([1, 1, 1], 0), 1);
    // assert.equal(acrossPresentFinder([1, 1, 1, 1], 0), 2);
    // assert.equal(acrossPresentFinder([1, 0, 1], 0), 2);
    // assert.equal(acrossPresentFinder([1, 0, 0], 0), 0);
    // assert.equal(acrossPresentFinder([1, 1, 0], 0), 1);
    // assert.equal(acrossPresentFinder([1, 0, 0, 0], 0), 0);
    // assert.equal(acrossPresentFinder([1, 0, 0, 1], 0), 3);
    // assert.equal(acrossPresentFinder([1, 0, 1, 1], 0), 2);
    // assert.equal(acrossPresentFinder([1, 0, 1, 0], 0), 2);
    // assert.equal(acrossPresentFinder([1, 1, 1, 0], 0), 1);
    // assert.equal(acrossPresentFinder([1, 1, 0, 1], 0), 1);

    assert.deepEqual(onePerElf(5), [1, 1, 1, 1, 1]);
    var winnerIndex = play(onePerElf(5), leftPresentFinder);
    assert.equal(winnerIndex + 1, 3);
    winnerIndex = play(onePerElf(5), acrossPresentFinder, removeLoser);
    assert.equal(winnerIndex + 1, 2);    
})();

function doPartX(presentFinder) {
    var numElves = 3012210;
    var numRounds;
    var winnerIndex = play(onePerElf(numElves), presentFinder, (elves, turn, round) => {
        if (round % 10000000 === 0) {
            console.error("round " + round);
        }
        numRounds = round;
    });
    var elf = (winnerIndex + 1).toString();
    console.log("winner is elf " + elf + " after " + numRounds + " rounds");
    
}

function doPartOne() {
    doPartX(leftPresentFinder);
}

function doPartTwo() {
    doPartX(acrossPresentFinder);
}

doPartTwo();

