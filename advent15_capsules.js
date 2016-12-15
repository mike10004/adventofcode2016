/******************************************************************************
--- Day 15: Timing is Everything ---

The halls open into an interior plaza containing a large kinetic sculpture. 
The sculpture is in a sealed enclosure and seems to involve a set of identical 
spherical capsules that are carried to the top and allowed to bounce through 
the maze of spinning pieces.

Part of the sculpture is even interactive! When a button is pressed, a capsule 
is dropped and tries to fall through slots in a set of rotating discs to 
finally go through a little hole at the bottom and come out of the sculpture. 
If any of the slots aren't aligned with the capsule as it passes, the capsule 
bounces off the disc and soars away. You feel compelled to get one of those 
capsules.

The discs pause their motion each second and come in different sizes; they seem 
to each have a fixed number of positions at which they stop. You decide to call 
the position with the slot 0, and count up for each position it reaches next.

Furthermore, the discs are spaced out so that after you push the button, one 
second elapses before the first disc is reached, and one second elapses as 
the capsule passes from one disc to the one below it. So, if you push the 
button at time=100, then the capsule reaches the top disc at time=101, the 
second disc at time=102, the third disc at time=103, and so on.

The button will only drop a capsule at an integer time - no fractional seconds 
allowed.

For example, at time=0, suppose you see the following arrangement:

* Disc #1 has 5 positions; at time=0, it is at position 4.
* Disc #2 has 2 positions; at time=0, it is at position 1.

If you press the button exactly at time=0, the capsule would start to fall; it 
would reach the first disc at time=1. Since the first disc was at position 4 
at time=0, by time=1 it has ticked one position forward. As a five-position 
disc, the next position is 0, and the capsule falls through the slot.

Then, at time=2, the capsule reaches the second disc. The second disc has 
ticked forward two positions at this point: it started at position 1, then 
continued to position 0, and finally ended up at position 1 again. Because 
there's only a slot at position 0, the capsule bounces away.

If, however, you wait until time=5 to push the button, then when the 
capsule reaches each disc, the first disc will have ticked forward 5+1 = 6 times
(to position 0), and the second disc will have ticked forward 5+2 = 7 times 
(also to position 0). In this case, the capsule would fall through the discs 
and come out of the machine.

However, your situation has more than two discs; you've noted their positions 
in your puzzle input. What is the first time you can press the button to get 
a capsule?

*/

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

doPartOne(); // Your puzzle answer was 16824.

/*
--- Part Two ---

After getting the first capsule (it contained a star! what great fortune!), 
the machine detects your success and begins to rearrange itself.

When it's done, the discs are back in their original configuration as if it 
were time=0 again, but a new disc with 11 positions and starting at 
position 0 has appeared exactly one second below the previously-bottom disc.

With this new disc, and counting again starting from time=0 with the 
configuration in your puzzle input, what is the first time you can press 
the button to get another capsule?
*/

function doPartTwo() {
    var partTwoDiscFactory = () => puzzleInputDiscFactory().concat([new Disc(11, 0)]);
    var numSecondsToWait = findEarliestButtonPushTime(partTwoDiscFactory);
    console.log(numSecondsToWait);
}

doPartTwo();