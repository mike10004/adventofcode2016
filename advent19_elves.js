var assert = require('assert');
var utils = require('./utils.js');

function Elves(n) {

    var me = this;

    var order = utils.repeat(n, function(i) { return {index: i};});   // array of elf indexes
    var cursor = 0;                        // whose turn it is (pointer into 'order' array)
    
    /**
     * Plays one round of white elephant. Transfers presents from one elf to
     * another, removing dead elves from the order array. Increments the
     * cursor if necessary.
     */
    this.advance = function() {
        var current = order[cursor];
        var currentPresents = current.numPresents || 1;
        var loserCursor = (cursor + parseInt(order.length / 2)) % order.length;
        var loser = order[loserCursor];
        var loserPresents = loser.numPresents || 1;
        current.numPresents = currentPresents + loserPresents;
        order.splice(loserCursor, 1);
        if (loserCursor > cursor) {
            cursor++;
        }
        cursor = cursor % order.length;
    };

    this.getNumRemaining = function() {
        return order.length;
    };

    this.whoseTurn = function() {
        return (order[cursor].index + 1).toString();
    };

    this.getWinner = function() {
        assert.equal(order.length, 1);
        return {
            index: order[0].index,
            label: (order[0].index + 1).toString(),
            numPresents: order[0].numPresents
        }
    };

    this.getOrder = function() {
        return order.concat();
    };
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
    var factory = function(i) { return {index: i};};
    assert.deepEqual(utils.repeat(2, factory), [{index: 0}, {index: 1}]);
    var numElves = 5;
    var winner = play(new Elves(numElves), (elves, round) => {
        console.error(round, elves.whoseTurn(), elves.getOrder().map(elf => elf.index+1));
    });
    console.error("winner", winner);
    assert.equal(winner.label, '2');
    assert.equal(winner.numPresents, numElves);
}

doUnitTest();

function doPartTwo(numElves) {
    console.log("starting with " + numElves + " elves");
    var messageInterval = 10000, timer = "play-"  + messageInterval.toString();
    var numRounds;
    var elves = new Elves(numElves);
    console.time(timer);
    var winner = play(elves, (elves, round) => {
        if (round > 0 && (round % messageInterval === 0)) {
            console.log("round " + round + "; " + elves.getNumRemaining() + " elves remaining");
            console.timeEnd(timer);
            console.time(timer);
        }
        numRounds = round;
    }); 
    console.timeEnd(timer);
    console.log("after " + numRounds + " rounds, one elf remains", winner);
}

// puzzle input: 3012210
var arg = process.argv[2];
if (typeof(arg) === 'undefined') {
    console.error("must specify argument: number of elves");
    process.exit(1);
}
var parsedNum = parseInt(arg, 10);
if (isNaN(parsedNum)) {
    console.error("invalid number: " + arg);
    process.exit(1);
}
doPartTwo(parsedNum);
