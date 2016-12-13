var utils = require('./utils.js');

/***************************************************************************
  
- cpy x y copies x (either an integer or the value of a register) into register y.
- inc x increases the value of register x by one.
- dec x decreases the value of register x by one.
- jnz x y jumps to an instruction y away (positive means forward; negative means 
  backward), but only if x is not zero.

 *****************************************************************************/
var args = utils.parseArgs(process.argv, {'limit': 10000000});
if (args.length > 1) {
    console.error("usage: specify filename or - to read from stdin");
    process.exit(1);
}
var filename = args[0] || '-';
utils.consume(filename, function(input){

    var REGISTER_LABELS = ['a', 'b', 'c', 'd'];
    var registers = {};
    REGISTER_LABELS.forEach(label => {
        registers[label] = parseInt(args.options[label] || 0, 10);
        if (isNaN(registers[label])) throw 'invalid initial register value for ' + label + ': ' + args.options[label];
    });
    var logger = (args.options['v'] || args.options['verbose']) ? console.error : (()=>false);
    input = input.trim();

    function Processor(registers, logger) {
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

        var perform = function(instruction, executions) { // return offset
            var m = pattern.exec(instruction);
            if (m === null) throw 'invalid instruction: ' + instruction;
            var command = m[1], x = m[2], y = m[3];
            logger(executions, command, x, y || '');
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
            }
            throw 'illegal state; tried to process ' + instruction;
        }

        this.process = function(instructions, limit) {
            var cursor = 0;
            var executions = 0;
            while (cursor < instructions.length) {
                if (limit && executions > limit) {
                    console.error("breaking because executions limit of " + limit + " was reached");
                    break;
                }
                var inst = instructions[cursor];
                var offset = perform(inst, executions);
                logger(executions, cursor, inst, registers, offset);
                cursor += offset;
                executions++;
            }
            return registers;
        }

    }

    (function test(testCases, log){
        log = log || (() => false);
        var assert = require('assert');
        function newRegisters(labels) {
            var registers = {};
            labels.forEach(lbl => registers[lbl] = 0);
            return registers;
        }

        testCases.forEach(testCase => {
            var output = new Processor(newRegisters(['a', 'b', 'c', 'd']), log).process(testCase.input);
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
        var processor = new Processor(registers, logger);
        var instructions = input.split(/[\n\r]+/);
        processor.process(instructions, args.options.limit);
        console.log(registers);
    })(logger);
});
