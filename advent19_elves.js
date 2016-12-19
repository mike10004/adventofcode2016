var assert = require('assert');
var utils = require('./utils.js');

function Elves(n, logger) {

    logger = logger || (()=>false);
    var cursor = 0;
    var winners = {};
    var me = this;
    var order = utils.repeat(n, i => i);
    var getNumPresents = function(i) {
        var w = winners[i];
        return typeof(w) === 'undefined' ? 1 : w;
    };

    this.advance = function() {
        var currentIndex = order[cursor];
        var currentPresents = getNumPresents(currentIndex);
        var loserCursor = (cursor + parseInt(order.length / 2)) % order.length;
        var loserIndex = order[loserCursor];
        var loserPresents = getNumPresents(loserIndex);
        var newPresents = currentPresents + loserPresents;
        winners[currentIndex] = newPresents;
        delete winners[loserIndex];
        order.splice(loserCursor, 1);
        if (loserCursor > cursor) {
            cursor++;
        }
        cursor = cursor % order.length;
        
    };

    this.getNumRemaining = function() {
        return order.length;
    };

    this.getCursor = function() {
        return cursor;
    };

    this.getNumWinners = function() {
        var keys = Object.keys(winners);
        return keys.length;
    };

    this.getWinner = function() {
        if (me.getNumWinners() !== 1) {
            assert.equal(me.getNumWinners(), 1, "getWinner called with non-terminal winners object with keys " + Object.keys(winners));
        }
        for (var k in winners) {
            assert(!isNaN(parseInt(k)), "failed to parse winner key " + k);
            return {
                label: (parseInt(k) + 1).toString(),
                numPresents: winners[k]
            };
        }
        throw 'illegal state';
    };

    this.getOrder = function() {
        return order.concat();
    }
}

function play(elves, callback) {
    callback = callback || (()=>false);
    var round = 0;
    callback(elves, round);
    while (elves.getNumRemaining() > 1) {
        elves.advance();
        round++;
        callback(elves, round);
    }
    return elves.getWinner();
}

function doUnitTest(){
    var numElves = 5;
    var winner = play(new Elves(numElves), (elves, round) => {
        console.error(round, elves.getCursor(), elves.getOrder().map(i => i+1));
    });
    console.error("winner", winner);
    assert.equal(winner.label, '2');
    assert.equal(winner.numPresents, numElves);
}

doUnitTest();

function doPartTwo() {
    // var numElves = 3012210;
    var numElves = 300;
    var numRounds;
    var elves = onePerElf(numElves);
    var winnerIndex = play(elves, (elves, round) => {
        if (round % 10000000 === 0) {
            console.error("round " + round);
        }
        numRounds = round;
    });
    var elf = (winnerIndex + 1).toString();
    console.log("winner is elf " + elf + " after " + numRounds + " rounds");
}

// doPartTwo();
