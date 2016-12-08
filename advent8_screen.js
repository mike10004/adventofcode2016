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

var sampleCommands = ['rect 3x2', 
'rotate column x=1 by 1', 
'rotate row y=0 by 4', 
'rotate row x=1 by 1'];

(function(W, H, commands, log){
    
    var ON = '#', OFF = '.';

    function row() {
        var r = new Array(W);
        for (var i = 0; i < W; i++) {
            r[i] = OFF;
        }
        return r;
    }
    
    function Screen(W, H) {
        
        var rows = [];
        for (var col = 0; col < H; col++) {
            rows.push(row());
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
            var chunk = rows[A].splice(W - B, B);
            rows[A] = chunk.concat(rows[A]);
        };

        this.rotateColumn = function(A, B) {
            log('rotateColumn', A, B);
            var chunk = [];
            for (var row = H - B; row < H; row++) {
                chunk.push(rows[row][A]);
            }
            for (var row = 0; row < B; row++) {
                chunk.push(rows[row][A]);
            }
            for (var row = 0; row < H; row++) {
                rows[row][A] = chunk[row];
            }
        }
        
        function Handler(regex, fn) {
            this.invoke = function(command) {
                var args = regex.exec(command);
                if (args) {
                    fn(args);
                    return true;
                }
            }

            this.toString = function() {
                return "Handler{" + regex + "}";
            }
        }
        var me = this;
        var rotators = {
                    'x': me.rotateColumn,
                    'y': me.rotateRow
                };
        var handlers = [
            new Handler(/rect (\d+)x(\d+)/, function(groups){
                var A = parseInt(groups[1], 10), B = parseInt(groups[2], 10);
                me.rect(A, B);
            }),
            new Handler(/rotate \w+ ([xy])=(\d+) by (\d+)/, function(groups){
                var A = parseInt(groups[2], 10), B = parseInt(groups[3], 10);
                rotators[groups[1]](A, B);
            })
        ];
        // /rotate \w+ ([xy])=(\d+) by (\d+)/.exec('rotate column x=1 by 1')
        this.perform = function(command) {
            for (var i in handlers) {
                if (handlers[i].invoke(command)) {
                    return;
                }
            }
            throw 'command unrecognized: ' + command;
        }

        this.draw = function() {
            var drawing = '';
            rows.forEach(function(row){
                drawing = drawing + row.join('');
                drawing += '\n';
            });
            return drawing;
        }
    }    
    
    var screen = new Screen(W, H);
    log("processing " + commands.length + " commands");
    commands.forEach(screen.perform);
    console.log(screen.draw());
})(50, 6, sampleCommands, console.error);

