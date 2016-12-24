// - cpy x y copies x (either an integer or the value of a register) into register y.
// - inc x increases the value of register x by one.
// - dec x decreases the value of register x by one.
// - jnz x y jumps to an instruction y away (positive means forward; negative means 
//   backward), but only if x is not zero.
// - tgl x toggles the instruction x instructions away 
// For one-argument instructions, inc becomes dec, 
//   and all other one-argument instructions become inc.
// For two-argument instructions, jnz becomes cpy, 
//   and all other two-instructions become jnz.
// The arguments of a toggled instruction are not affected.
// If an attempt is made to toggle an instruction outside the 
// program, nothing happens.
// If toggling produces an invalid instruction (like cpy 1 2) and an attempt is later made to execute that instruction, skip it instead.
// If tgl toggles itself (for example, if a is 0, tgl a would target itself and become inc a), the resulting instruction is not executed until the next time it is reached.
(function(){
    
    var numArgs = {
        'cpy': 2,
        'tgl': 1,
        'jnz': 2,
        'inc': 1,
        'dec': 1
    };
    
    module.exports = {
        Processor: function(registers, logger) {
            if (typeof(registers) === 'undefined') throw 'registers must be an object';
            logger = logger || (()=>false);
            var pattern = /(\w+)\s+((?:[-+])?\w+)(?:\s+((?:[-+])?\w+))?\s*/;

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

            function parseInstruction(instruction) {
                var m = pattern.exec(instruction);
                if (m === null) throw 'invalid instruction: ' + instruction;
                return {
                    command: m[1],
                    x: m[2],
                    y: m[3]
                }
            };

            function writeInstruction(command, x, y) {
                return [command, x, y || ' '].join(' ');
            }

            function getToggledCommand(command) {
                var n = numArgs[command];
                if (n === 1) {
                    return command === 'inc' ? 'dec' : 'inc';
                } else if (n === 2) {
                    return command === 'jnz' ? 'cpy' : 'jnz';
                } else {
                    throw 'numArgs: ' + n;
                }
            }

            function isStringifiedNumber(value) {
                return !isNaN(parseInt(value)); 
            }

            function isValid(instruction) {
                switch (instruction.command) {
                    case 'cpy':
                        return !isStringifiedNumber(instruction.y);
                    case 'jnz':
                    case 'tgl':
                        return true;
                    case 'inc':
                    case 'dec':
                        return !isStringifiedNumber(instruction.x);
                    default: 
                        throw 'could not check valid: ' + JSON.stringify(instruction);
                }
            }

            function toggle(instructions, target) {
                var text = instructions[target];
                if (typeof(text) === 'undefined') {
                    return;
                }
                var instruction = parseInstruction(text);
                var command = getToggledCommand(instruction.command);
                instructions[target] = writeInstruction(command, instruction.x, instruction.y);
            }

            var perform = function(instructions, index, executions) { // return offset
                var instruction = instructions[index];
                var parsed = parseInstruction(instruction);
                var command = parsed.command, x = parsed.x, y = parsed.y;
                logger(executions, command, x, y || '');
                if (isValid(parsed)) {
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
                        case 'tgl':
                            toggle(instructions, index + val(x));
                            return 1;
                    }
                    throw 'illegal state; tried to process ' + instruction;
                }
            };

            this.process = function(instructions, limit) {
                var cursor = 0;
                var executions = 0;
                while (cursor < instructions.length) {
                    if (limit && executions > limit) {
                        console.error("breaking because executions limit of " + limit + " was reached");
                        break;
                    }
                    var instruction = instructions[cursor];
                    var offset = perform(instructions, cursor, executions);
                    logger(executions, cursor, instruction, instructions[cursor] == instruction ? 'unchanged' : instructions[cursor], registers, offset);
                    cursor += offset;
                    executions++;
                }
                return registers;
            };

        }
        
    };

})();