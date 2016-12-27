var utils = require('./utils.js');
var assert = require('assert');
/***************************************************************************
  
- cpy x y copies x (either an integer or the value of a register) into register y.
- inc x increases the value of register x by one.
- dec x decreases the value of register x by one.
- jnz x y jumps to an instruction y away (positive means forward; negative means 
  backward), but only if x is not zero.

 *****************************************************************************/
var args = utils.parseArgs(process.argv, {'limit': 1000000});
if (args.length > 1) {
    console.error("usage: specify filename or - to read from stdin");
    process.exit(1);
}
var filename = args[0] || '-';
var REGISTER_LABELS = ['a', 'b', 'c', 'd'];
function createRegisters() {
    var registers = {};
    REGISTER_LABELS.forEach(label => registers[label] = 0);
    return registers;
};
utils.consume(filename, function(input){
    var logger = (args.options['v'] || args.options['verbose']) ? console.error : (()=>false);
    input = input.trim();
    var SIGNAL_INTERRUPT = 'SIGNAL_INTERRUPT';
    function Processor(registers, logger, veryVerbose) {
        if (typeof(registers) === 'undefined') throw 'registers must be an object';
        var pattern = /(\w+)\s+((?:[-+])?\w+)(?:\s+((?:[-+])?\w+))?/;

        function val(variable) {
            if (/[-+]?\d+/.test(variable)) {
                var number = parseInt(variable, 10);
                if (isNaN(number)) {
                    throw 'failure to parse ' + variable;
                }
                return number;
            } else {
                var value = registers[variable];
                if (typeof(value) === 'undefined') {
                    throw 'invalid register: ' + variable + '; available: ' + Object.keys(registers).join();
                }
                return value;
            }
        }

        function Performer(instruction) {
            var m = pattern.exec(instruction);
            assert.ok(m !== null, 'invalid instruction');
            var command = m[1], x = m[2], y = m[3];

            this.execute = function(registers, signal) {
                switch (command) {
                    case 'inc': 
                        registers[x]++;
                        return 1;
                    case 'dec':
                        registers[x]--;
                        return 1;
                    case 'jnz':
                        var question = val(x);
                        if (question !== 0) {
                            var offset = val(y);
                            return offset;
                        } else {
                            return 1;
                        }
                    case 'cpy':
                        registers[y] = val(x);
                        return 1; 
                    case 'out':
                        if (!signal(val(x))) {
                            return SIGNAL_INTERRUPT;
                        }
                        return 1;
                }
            };

            this.toString = (() => instruction);
        }

        var perform = function(instruction, executions, signal) { // return offset
            var m = pattern.exec(instruction);
            if (m === null) throw 'invalid instruction: ' + instruction;
            var command = m[1], x = m[2], y = m[3];
            if (veryVerbose) {
                logger(executions, command, x, y || '');
            }
            throw 'illegal state; tried to process ' + instruction;
        }

        this.process = function(instructions, limit, signalCallback) {
            var cursor = 0;
            var executions = 0;
            var performers = instructions.map(inst => new Performer(inst));
            while (cursor < performers.length) {
                if (limit && executions > limit) {
                    console.error("breaking because executions limit of " + limit + " was reached");
                    break;
                }
                var performer = performers[cursor];
                var offset = performer.execute(registers, signalCallback);
                if (veryVerbose) {
                    logger(executions, cursor, performer.toString(), registers, offset);
                }
                if (offset === SIGNAL_INTERRUPT) {
                    //logger('interrupted at cursor', cursor, 'in instructions array of length', instructions.length);
                    break;
                } else {
                    cursor += offset;
                    executions++;
                }
            }
            return registers;
        }

    }

    function SignalCollector(signal) {
        this.callback = x => {
            signal.push(x);
            return true;
        };
    }

    /**
     * @param evidenceRequirement max executions to perform
     * @param searchLimit maximum register 'a' value to try
     */
    function findSignal(instructions, evidenceRequirement, initialRegisterA, collectSignal, searchLimit) {
        initialRegisterA = initialRegisterA || 0;
        searchLimit = searchLimit || -1;
        while (searchLimit < 0 || initialRegisterA < searchLimit) {
            var registers = createRegisters();
            initialRegisterA++;
            registers['a'] = initialRegisterA;
            if (initialRegisterA > 0 && initialRegisterA % 1000 === 0) {
                console.error("tried with initial register a value of " + initialRegisterA.toString());
            }
            var processor = new Processor(registers, logger);
            var signal = [];
            function SignalChecker() {
                var previous = 1;
                var clean;
                this.callback = (() => function(x) {
                    if (collectSignal) {
                        signal.push(x);
                    }
                    if (x !== 0 && x !== 1) {
                        return clean = false;
                    }
                    if (previous === 1 && x !== 0) {
                        return clean = false;
                    }
                    if (previous === 0 && x !== 1) {
                        return clean = false;
                    }
                    previous = x;
                    return clean = true;
                });
                this.isClean = (() => clean);
            }
            var signalChecker = new SignalChecker();
            var callback = signalChecker.callback();
            processor.process(instructions, evidenceRequirement, callback);
            if (signalChecker.isClean()) {
                console.log("clean execution with initial register a value", initialRegisterA);
                if (collectSignal) {
                    console.log("signal", signal.slice(0, 10));
                }
                return initialRegisterA;
            }
        }
        if (initialRegisterA >= searchLimit) {
            console.error("reached search limit " + searchLimit);
            return false;
        }
        throw 'illegal state';
    }

    (function testPartTwo(){
        var instructions = [
            'cpy 0 b',
            'cpy 1 c',
            'dec a',
            'dec a',
            'dec a',
            'jnz a 4', // exit
            'out b',
            'out c',
            'jnz 1 -2'
        ];
        var expected = 3;
        var initialRegisterAValue = findSignal(instructions, 1000, 0, true, 10);
        assert.equal(initialRegisterAValue, expected, "unexpected: " + initialRegisterAValue);
    })();

    (function testPartOne(testCases, log){
        log = log || (() => false);
        
        function newRegisters(labels) {
            var registers = {};
            labels.forEach(lbl => registers[lbl] = 0);
            return registers;
        }

        testCases.forEach(testCase => {
            var output = new Processor(newRegisters(['a', 'b', 'c', 'd']), log).process(testCase.input, 100, new SignalCollector(new Array()).callback());
            log('input', testCase.input, 'output', output);
            assert.deepEqual(testCase.output, output, 'test case output ' + JSON.stringify(output) + ' not equal to expected output ' + JSON.stringify(testCase.output));
        });
    })([
        {
            input: ['cpy 41 a', 'inc a', 'inc a', 'dec a', 'jnz a 2', 'dec a'],
            output: {'a': 42, 'b': 0, 'c': 0, 'd': 0}
        }
    ]);

    (function(logger) {
        var found = false, initialRegisterA = 0, searchLimit = 1000000;
        var instructions = input.split("\n");
        var validInitialRegisterAValue = findSignal(instructions, parseInt(args.options.limit));
        console.log("valid initial register a value: " + validInitialRegisterAValue);
    })(logger);
});
