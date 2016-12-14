/**
 * advent day 13 - cubicles
 */

var assert = require('assert');
var logger = () => false; 
// logger = console.error; // uncomment to be verbose

function Point(x, y) {
    assert(typeof(x) === 'number' && typeof(y) === 'number', 'x and y must be numbers');
    this.x = x;
    this.y = y;
    var me = this;
    this.toString = function() {
        return "(" + me.x + ", " + me.y + ")";
    };

    this.equals = function(other) {
        if (other === null) {
            return false;
        }
        if (typeof(other) !== 'object') {
            return false;
        } 
        assert(typeof(other.x) === 'number' && typeof(other.y) === 'number', "other object must have x and y properties");
        return me.x === other.x && me.y === other.y;
    }

    this.isNonnegative = function() {
        return x >= 0 && y >= 0;
    };

    this.offset = function(p) {
        return new Point(x + p.x, y + p.y);
    };

    var neighborOffsetsWithDiagonals = [{x: -1, y: -1}, {x: -1, y: 0}, {x: -1, y: 1},
                                        {x: 0, y: -1},                 {x: 0, y: 1},
                                        {x: 1, y: -1},  {x: 1, y: 0},  {x: 1, y: 1}];
    
    var neighborOffsets =                [{x: -1, y: 0}, 
                           {x: 0, y: -1},                {x: 0, y: 1},
                                          {x: 1, y: 0}];

    this.neighbors = function(wallFilter) {
        assert(typeof(wallFilter) === 'function');
        var offsets = neighborOffsets.map(me.offset);
        offsets = offsets.filter(p => p.isNonnegative());
        offsets = offsets.filter(wallFilter);
        return offsets;
    };

    this.distanceTo = function(p) {
        return Math.sqrt(Math.pow(x - p.x, 2) + Math.pow(y - p.y, 2));
    };

    this.taxicabDistanceTo = function(p) {
        return Math.abs(p.x - x) + Math.abs(p.y - y);
    }

    this.comparator = function(a, b) {
        var aDist = a.distanceTo(me), bDist = b.distanceTo(me);
        return aDist - bDist; // if aDist < bDist, return negative so that a precedes b in sort order
    };
}

/**
 * 
 * - Find x*x + 3*x + 2*x*y + y + y*y.
 * - Add the office designer's favorite number (your puzzle input).
 * - Find the binary representation of that sum; count the number of bits that are 1.
 *    - If the number of bits that are 1 is even, it's an open space.
 *    - If the number of bits that are 1 is odd, it's a wall.
 */
function isWall(p, favNumber) {
    var x = p.x, y = p.y;
    var z = (x*x + 3*x + 2*x*y + y + y*y) + favNumber;
    var b = z.toString(2);
    var n = 0;
    for (var i in b) {
        if (b[i] == '1') {
            n++;
        }
    }
    return n % 2 == 1;
}

function AgentFactory(wallFilter) {
    assert(typeof(wallFilter) === 'function');
    
    this.start = function(point) {
        return new Agent(point, new Array(), 0);
    };

    function indexOfPoint(array, p) {
        for (var i in array) {
            if (p.equals(array[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @param {Point} point current position
     * @param {Point} previous array of positions we've already been to
     * @param {Point} numMoves how many moves it took to get here
     * @param {function} wallFilter predicate applied to points to filter out walls from possible next moves list  
     */
    function Agent(point, previous, numMoves) {
        assert(typeof(point) === 'object', 'point must be Point: ' + point);
        assert(previous instanceof Array, 'previous must be Array: ' + previous);
        assert(typeof(numMoves) === 'number', 'numMoves must be number: ' + numMoves);
        var me = this;
        this.move = function(destination, maxMoves) {
            assert.equal(typeof(maxMoves), 'number', 'maxMoves must be a number');
            assert.ok(maxMoves >= 0, 'required: maxMoves >= 0; maxMoves = ' + maxMoves);
            
            if (point.equals(destination)) {
                return numMoves;
            }
            if (numMoves < maxMoves) {
                var neighbors = point.neighbors(wallFilter)
                        .filter(p => indexOfPoint(previous, p) === -1);
                neighbors.sort(destination.comparator);
                for (var i in neighbors) {
                    var nextAgent = new Agent(neighbors[i], previous.concat([point]), numMoves + 1);
                    var answer = nextAgent.move(destination, maxMoves);
                    if (answer >= 0) {
                        return answer;
                    }
                }
            }
            return -1; // neighbors empty or no solution found
        };
    }

}

function findMinimumMoves(start, destination, agentFactory, maxMoves, lowerBound) {
    maxMoves = maxMoves || Math.pow(2, start.taxicabDistanceTo(destination));
    lowerBound = lowerBound || start.taxicabDistanceTo(destination);
    var upperBound = lowerBound;
    var result = -1;
    logger('findMinimumMoves: from ' + start + ' to ' + destination + ' in ' + maxMoves + ' or less');
    while (upperBound <= maxMoves) {
        logger('findMinimumMoves: searching for solution in <= ' + upperBound + ' moves');
        var agent = agentFactory.start(start);
        result = agent.move(destination, upperBound);
        if (result >= 0) {
            break;
        }
        upperBound++;
    }
    return result;
}

function doUnitTest() {
    logger('doUnitTest');
    (function testNeighbors() {
        var p = new Point(1, 1);
        assert.equal(p.neighbors(x => true).length, 4);
    })();

    function getPoint(board, numColumns, x, y, outOfRange) {
        outOfRange = outOfRange || '#';
        assert.equal(typeof(board), 'string', 'board is not a string: ' + board);
        assert.ok(typeof(numColumns) === 'number' && !isNaN(numColumns), 'numColumns is not a number: ' + numColumns);
        assert.ok(typeof(x) === 'number' && typeof(y) === 'number', 'x or y is not a number');
        assert.equal(0, board.length % numColumns);
        var index = y * numColumns + x;
        return index < board.length ? board[index]: outOfRange;
    }
    assert.equal('#', getPoint('#', 1, 0, 0));
    //  012
    // 0#.#
    // 1##.
    assert.equal('#', getPoint('#.###.', 3, 0, 0));
    assert.equal('.', getPoint('#.###.', 3, 1, 0));
    assert.equal('#', getPoint('#.###.', 3, 2, 0));
    assert.equal('#', getPoint('#.###.', 3, 0, 1));
    assert.equal('#', getPoint('#.###.', 3, 1, 1));
    assert.equal('.', getPoint('#.###.', 3, 2, 1));
    function makeWallFilter(board, numColumns) {
        return p => getPoint(board, numColumns, p.x, p.y) === '.';
    }

    function newBoard(rows) {
        if (typeof(rows) === 'string') {
            rows = Array.prototype.map.call(arguments, x => x);
        } 
        var board = rows.join('');
        return {
            cells: board,
            rendered: rows.join('\n'),
            numRows: rows.length, 
            numColumns: board.length / rows.length
        };
    }

    var numTests = 0;
    function test(boardObj, start, destination, numMoves, maxMoves) {
        logger(++numTests, "test", boardObj.cells.length, start.toString(), destination.toString(), numMoves);
        var wallFilter = makeWallFilter(boardObj.cells, boardObj.numColumns);
        assert.equal(wallFilter(destination), true, "destination " + destination + " must not be a wall");
        var agentFactory = new AgentFactory(wallFilter);
        var answer = findMinimumMoves(start, destination, agentFactory, maxMoves);
        if (answer !== numMoves) {
            logger(boardObj.rendered);
            logger('start ' + start + ', destination = ' + destination + ', numMoves= ' + numMoves);
            logger(numTests, "wrong answer: " + answer + "; expected " + numMoves);
            process.exit(1);
        }
        assert.equal(numMoves, answer);
    }
    
    function p(x, y) {
        return new Point(x, y);
    }

    test(newBoard('.'), p(0, 0), p(0, 0), 0);
    test(newBoard('.'), p(0, 0), p(0, 0), 0, 0);
    test(newBoard('..'), p(0, 0), p(1, 0), 1);
    test(newBoard('..'), p(0, 0), p(1, 0), 1, 1);
    test(newBoard('..', '..'), p(0, 0), p(1, 1), 2);
    test(newBoard('.#', '..'), p(0, 0), p(1, 1), 2);
    test(newBoard('.#.',
                  '...'), p(0, 0), p(2, 0), 4);
    test(newBoard('.#.',
                  '...'), p(0, 0), p(2, 0), 4, 4);
    test(newBoard('..#.',
                  '#...'), p(0, 0), p(3, 0), 5);
    test(newBoard('..#.',
                  '....'), p(0, 0), p(3, 0), 5);
    test(newBoard('..#.',
                  '..#.',
                  '....'), p(0, 0), p(3, 0), 7);
    test(newBoard('..#.#',
                  '..##.',
                  '#..#.',
                  '..#..',
                  '#...#'), p(0, 0), p(4, 3), 9);

}

function doGivenTest() {
    logger('doGivenTest');
    var favNumber = 10, maxMoves = 12;
    var agentFactory = new AgentFactory(p => !isWall(p, favNumber));
    var actual = findMinimumMoves(new Point(1, 1), new Point(7, 4), agentFactory, maxMoves);
    if (11 !== actual) {
        logger("doGivenTest: actual = " + actual);
        process.exit(1);
    }
}

function doPartOne(favNumber, from, to){
    logger("doPartOne");
    var agentFactory = new AgentFactory(p => !isWall(p, favNumber));
    var result = findMinimumMoves(from, to, agentFactory);
    console.log(result.toString() + " moves from " + from.toString() + " to " + to.toString());
}

function doPartTwo(favNumber, start, maxMoves) {
    logger("doPartTwo");
    function rectangle(corner1, corner2) {
        var xMin = Math.min(corner1.x, corner2.x), yMin = Math.min(corner1.y, corner2.y);
        var xMax = Math.max(corner1.x, corner2.x), yMax = Math.max(corner1.y, corner2.y);
        var points = [];
        for (var x = xMin; x <= xMax; x++) {
            for (var y = yMin; y <= yMax; y++) {
                points.push(new Point(x, y));
            }
        }
        return points;
    }
    var c1 = new Point(start.x - maxMoves, start.y - maxMoves), c2 = new Point(start.x + maxMoves, start.y + maxMoves); 
    var points = rectangle(c1, c2);
    logger(points.length + " in rectangle from " + c1 + " to " + c2);
    points = points.filter(p => start.taxicabDistanceTo(p) <= maxMoves)
                    .filter(p => !isWall(p, favNumber));
    logger("filtered to " + points.length + " destinations");
    var numCanReach = 0;
    points.forEach(p => {
        var agentFactory = new AgentFactory(p => !isWall(p, favNumber));
        var result = findMinimumMoves(start, p, agentFactory, maxMoves);
        if (result >= 0) {
            logger(++numCanReach, "can reach " + p.toString());
        } else {
            logger("can't reach " + p.toString() + " in max " + maxMoves + " moves");
        }
    });
    console.log("from " + start.toString() + " " + numCanReach + " destinations are within " + maxMoves + " moves");
}

doUnitTest();
doGivenTest();
doPartOne(1364, new Point(1, 1), new Point(31, 39));
doPartTwo(1364, new Point(1, 1), 50);