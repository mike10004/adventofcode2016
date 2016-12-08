/*****************************************************************************
--- Day 8: Two-Factor Authentication ---

You come across a door implementing what you can only assume is an 
implementation of two-factor authentication after a long game of requirements 
telephone.

To get past the door, you first swipe a keycard (no problem; there was one on
a nearby desk). Then, it displays a code on a little screen, and you type
that code on a keypad. Then, presumably, the door unlocks.

Unfortunately, the screen has been smashed. After a few minutes, you've taken 
everything apart and figured out how it works. Now you just have to work out 
what the screen would have displayed.

The magnetic strip on the card you swiped encodes a series of instructions for
the screen; these instructions are your puzzle input. The screen is 50 pixels
wide and 6 pixels tall, all of which start off, and is capable of three
somewhat peculiar operations:

* `rect AxB` turns on all of the pixels in a rectangle at the top-left of the 
screen which is A wide and B tall.

* `rotate row y=A by B` shifts all of the pixels in row A (0 is the top row) 
right by B pixels. Pixels that would fall off the right end appear at the left
end of the row.

* `rotate column x=A by B` shifts all of the pixels in column A (0 is the left 
column) down by B pixels. Pixels that would fall off the bottom appear at the 
top of the column.

For example, here is a simple sequence on a smaller screen:

rect 3x2 creates a small rectangle in the top-left corner:

###....
###....
.......
rotate column x=1 by 1 rotates the second column down by one pixel:

#.#....
###....
.#.....
rotate row y=0 by 4 rotates the top row right by four pixels:

....#.#
###....
.#.....
rotate row x=1 by 1 again rotates the second column down by one pixel, 
causing the bottom pixel to wrap back to the top:

.#..#.#
#.#....
.#.....
As you can see, this display technology is extremely powerful, and will soon 
dominate the tiny-code-displaying-screen market. That's what the advertisement on the back of the display tries to convince you, anyway.

There seems to be an intermediate check of the voltage used by the display: 
after you swipe your card, if the screen did work, how many pixels should 
be lit?
********************************************************************************************/

var assert = require('assert');
var testCases = [
    {
        width: 7,
        height: 3,
        commands: [
            'rect 3x2', 
            'rotate column x=1 by 1', 
            'rotate row y=0 by 4', 
            'rotate row x=1 by 1'
        ],
        screen: '.#..#.#\n#.#....\n.#.....\n'
    },
    {
        width: 2, height: 2,
        commands: ['rect 1x1', 'rotate column x=0 by 1'],
        screen: '..\n#.\n'
    },
    {
        width: 2, height: 2,
        commands: ['rect 1x1', 'rotate row y=0 by 1'],
        screen: '.#\n..\n'
    },
    {
        width: 2, height: 2,
        commands: ['rect 1x1', 'rotate column x=0 by 2'],
        screen: '#.\n..\n'
    },
    {
        width: 2, height: 2,
        commands: ['rect 1x1', 'rotate row y=0 by 2'],
        screen: '#.\n..\n'
    },
    {
        width: 2, height: 2,
        commands: ['rect 1x1', 'rotate column x=0 by 3'],
        screen: '..\n#.\n'
    },
    {
        width: 2, height: 2,
        commands: ['rect 1x1', 'rotate row y=0 by 3'],
        screen: '.#\n..\n'
    },
    {
        width: 2, height: 2,
        commands: ['rect 1x1', 'rotate column x=0 by 1', 'rotate row y=1 by 1'],
        screen: '..\n.#\n'
    },
    {
        width: 2, height: 2,
        commands: ['rect 1x1', 'rotate column x=0 by 1', 'rotate row y=1 by 1', 'rect 1x1'],
        screen: '#.\n.#\n'
    },
    {
        width: 2, height: 2,
        commands: ['rect 1x1', 'rotate column x=0 by 1', 'rotate row y=1 by 1', 'rect 1x1', 'rotate column x=1 by 1'],
        screen: '##\n..\n'
    }
];

function run(W, H, commands, log) {
    
    var ON = '#', OFF = '.';

    function Screen(W, H) {
        
        var rows = createArray(H, () => createArray(W, OFF));

        var checkDimensions = function(source) {
            assert.equal(H, rows.length);
            rows.forEach((row, i) => {
                assert.equal(W, row.length, 'row length incorrect from ' + source)
                assert.equal(W, row.filter(c => c === OFF || c === ON).length, "row " + i + " has illegal character; from " + source);
            });
        };

        function createArray(length, fill) {
            var r = new Array(length);
            for (var i = 0; i < length; i++) {
                r[i] = typeof(fill) === 'function' ? fill() : fill;
            }
            return r;
        }

        this.rect = function(A, B) {
            log('rect', A, B);
            for (var row = 0; row < B; row++) {
                for (var col = 0; col < A; col++) {
                    rows[row][col] = ON;
                }
            }
        };

        this.rotateRow = function(A, B) {
            log('rotateRow', A, B);
            assert(B >= 0, "rotation magnitude must be nonnegative");
            B = B % W;
            var chunk = rows[A].splice(W - B, B);
            rows[A] = chunk.concat(rows[A]);
        };

        this.rotateColumn = function(A, B) {
            log('rotateColumn', A, B);
            assert(B >= 0, "rotation magnitude must be nonnegative");
            B = B % H;
            var chunk = [];
            rows.filter((row, i) => i >= H - B).forEach((row, i) => chunk.push(row[A]));
            rows.filter((row, i) => i < H - B).forEach((row, i) => chunk.push(row[A]));
            chunk.forEach((value, i) => rows[i][A] = value);
        }
        
        function Handler(regex, fn, name) {
            this.invoke = function(command) {
                var args = regex.exec(command);
                if (args) {
                    fn(args);
                    return true;
                }
            }

            this.name = name;
        }
        
        var me = this;
        
        var rotators = {
            'x': this.rotateColumn,
            'y': this.rotateRow
        };

        var handlers = [
            new Handler(/rect (\d+)x(\d+)/, function(groups){
                var A = parseInt(groups[1], 10), B = parseInt(groups[2], 10);
                me.rect(A, B);
            }, 'RectHandler'),
            new Handler(/rotate \w+ ([xy])=(\d+) by (\d+)/, function(groups){
                var A = parseInt(groups[2], 10), B = parseInt(groups[3], 10);
                rotators[groups[1]](A, B);
            }, 'RotateHandler')
        ];
        
        this.perform = function(command) {
            for (var i in handlers) {
                if (handlers[i].invoke(command)) {
                    checkDimensions(handlers[i].name);
                    return;
                }
            }
            throw 'command unrecognized: ' + command;
        }

        this.draw = function() {
            var drawing = '';
            rows.forEach(function(row, i){
                var rowStr = row.map(c => c || 'X').join('');
                assert.equal(W, rowStr.length, "row " + i + " has length " + rowStr.length + " instead of " + W);
                drawing = drawing + rowStr;
                drawing += '\n';
            });
            return drawing;
        }
    }    
    
    var screen = new Screen(W, H);
    log("processing " + commands.length + " commands");
    commands.forEach(screen.perform);
    var result = screen.draw();
    return result;
};

testCases.forEach(testCase => {
    var result = run(testCase.width, testCase.height, testCase.commands, () => false);
    assert.equal(testCase.screen, result, "after commands " + testCase.commands + ", expected screen:\n" + testCase.screen + "\n but got " + result);
});

(function(){
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
      var commands = chunk.split(/[\n\r]/).filter(s => s.length > 0);
      var result = run(50, 6, commands, console.error);
      console.log(result);
      var numOn = result.split('').filter(c => c === '#').length;
      console.log(numOn + " cells are 'on'");
    }
  });
})();
