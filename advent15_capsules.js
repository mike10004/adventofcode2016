var assert = require('assert');

function Disc(numPositions, start) {
    assert(numPositions > 0, "numPositions must be > 0");
    assert(start < numPositions, "start < numPositions required: " + start);

    var current = start;
    var me = this;
    this.numPositions = numPositions;

    this.tick = function(numTicks) {
        numTicks = typeof(numTicks) === 'undefined' ? 1 : numTicks;
        current = (current + numTicks) % numPositions;
        return me;
    };

    this.isOpen = function() {
        return current === 0;
    };
}

function Sculpture(discs) {

    var me = this;

    function tickAll(n) {
        discs.forEach(disc => disc.tick(n));
    }

    this.tick = function(numSeconds) {
        tickAll(numSeconds);
        return me;
    };

    this.pushButton = function() {
        for (var disc = 0; disc < discs.length; disc++) {
            tickAll(1);
            if (!discs[disc].isOpen()) {
                return false;
            }
        } 
        return true;
    };
}

function findPeriod(discs) {
    var p = discs.map(d => d.numPositions).reduce((m, n) => m * n, 1); // lcm would work
    return p;
}

function findEarliestButtonPushTime(discFactory) {
    var period = findPeriod(discFactory()); 
    for (var n = 0; n < period; n++) {
        var discs = discFactory();
        var sculpture = new Sculpture(discs);
        var capsuleReleased = sculpture.tick(n).pushButton();
        if (capsuleReleased) {
            return n;
        }
    }
    return -1;
}

function doUnitTest() {
    assert.equal(new Disc(1, 0).isOpen(), true);
    assert.equal(new Disc(1, 0).tick().isOpen(), true);
    assert.equal(new Disc(1, 0).tick().tick().isOpen(), true);
    assert.equal(new Disc(2, 0).isOpen(), true);
    assert.equal(new Disc(2, 1).isOpen(),  false);
    assert.equal(new Disc(2, 0).tick().isOpen(), false);
    assert.equal(new Disc(2, 1).tick().isOpen(), true);
    assert.equal(new Disc(3, 0).isOpen(), true);
    assert.equal(new Disc(3, 0).tick(1).isOpen(), false);
    assert.equal(new Disc(3, 0).tick(2).isOpen(), false);
    assert.equal(new Disc(3, 0).tick(3).isOpen(), true);
    assert.equal(new Disc(3, 1).isOpen(), false);
    assert.equal(new Disc(3, 1).tick(1).isOpen(), false);
    assert.equal(new Disc(3, 1).tick(2).isOpen(), true);
    assert.equal(new Disc(3, 1).tick(3).isOpen(), false);
    assert.equal(new Disc(3, 2).isOpen(), false);
    assert.equal(new Disc(3, 2).tick(1).isOpen(), true);
    assert.equal(new Disc(3, 2).tick(2).isOpen(), false);
    assert.equal(new Disc(3, 2).tick(3).isOpen(), false);

/*
Disc #1 has 5 positions; at time=0, it is at position 4.
Disc #2 has 2 positions; at time=0, it is at position 1.
*/
    var discFactory = function() {
        return [new Disc(5, 4), new Disc(2, 1)];
    }

    assert.equal(new Sculpture(discFactory()).pushButton(), false);
    assert.equal(new Sculpture(discFactory()).tick(5).pushButton(), true);
    assert.equal(findEarliestButtonPushTime(discFactory), 5);

}

doUnitTest();


/* Puzzle input
Disc #1 has 17 positions; at time=0, it is at position 5.
Disc #2 has 19 positions; at time=0, it is at position 8.
Disc #3 has 7 positions; at time=0, it is at position 1.
Disc #4 has 13 positions; at time=0, it is at position 7.
Disc #5 has 5 positions; at time=0, it is at position 1.
Disc #6 has 3 positions; at time=0, it is at position 0.
*/

var puzzleInputDiscFactory = function () {
    return [
        new Disc(17, 5),
        new Disc(19, 8),
        new Disc(7, 1),
        new Disc(13, 7),
        new Disc(5, 1),
        new Disc(3, 0)
    ];
};

function doPartOne() {
    var numSecondsToWait = findEarliestButtonPushTime(puzzleInputDiscFactory);
    console.log(numSecondsToWait);
}

doPartOne();
