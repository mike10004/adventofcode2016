var utils = require('./utils.js');
var assembunny = require('./assembunny.js');

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

    (function test(testCases, log){
        log = log || (() => false);
        var assert = require('assert');
        function newRegisters(labels) {
            var registers = {};
            labels.forEach(lbl => registers[lbl] = 0);
            return registers;
        }

        testCases.forEach(testCase => {
            var output = new assembunny.Processor(newRegisters(['a', 'b', 'c', 'd']), log).process(testCase.input);
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
        var processor = new assembunny.Processor(registers, logger);
        var instructions = input.split(/[\n\r]+/);
        processor.process(instructions, args.options.limit);
        console.log(registers);
    })(logger);
});
// expect output:
// { a: 318007, b: 196418, c: 0, d: 0 }