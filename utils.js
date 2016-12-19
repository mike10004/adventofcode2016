// utils.js

(function(){

    function consumeFile(filename, inputHandler, noInputHandler) {
        inputHandler = inputHandler || ((x)=>console.log(x));
        noInputHandler = noInputHandler || (()=>false);
        var fs = require('fs');
        fs.readFile(filename, 'utf8', function(err, data) {
            if (err) {
                noInputHandler();
                throw err;
            }
            inputHandler(data);
        });
    }

    var consumeStdin = function(inputHandler, noInputHandler) {
        inputHandler = inputHandler || (data => console.log(data));
        noInputHandler = noInputHandler || (() => {
            console.error("no data on stdin");
            return 1;
        });
        process.stdin.setEncoding('utf8');
        var consumed = false;
        process.stdin.on('readable', () => {
            var chunk = process.stdin.read();
            if (chunk == null) {
                var exitCode = 0;
                if (!consumed) {
                    exitCode = typeof(noInputHandler()) === 'undefined' || 1;
                }
                process.exit(consumed ? 0 : 1);
            } else {
                consumed = true;
                inputHandler(chunk);                
            }
        });
    }

    module.exports = {

        consume: function(filename, inputHandler, noInputHandler) {
            if (typeof(filename) === 'undefined' || filename === '-') {
                consumeStdin(inputHandler, noInputHandler);
            } else {
                consumeFile(filename, inputHandler, noInputHandler);
            }
        },

        /**
         * Consume standard input and invoke handler.
         */
        consumeStdin: consumeStdin,

        parseArgs: function(argv, options) {
            argv = argv || process.argv;
            var argv0Components = (argv[0] || '').split('/');
            var basename = argv0Components[argv0Components.length - 1];
            if (basename !== 'node' && basename !== 'nodejs') {
                throw 'expected argv[0] === "/path/to/node" but it is '+ argv[0] + ' with basename ' + basename;
            }
            var args = argv.slice(2);
            var sentinel = args.indexOf('--');
            if (sentinel === -1) {
                sentinel = args.length;
            }
            var positionals = args.slice(0, sentinel)
                    .filter(a => a.indexOf('-') === -1 || (a.indexOf('-') > 0 && a.indexOf('--') > 0))
                    .concat(args.slice(sentinel, args.length - sentinel));
            var optionArgs = args.slice(0, sentinel)
                    .filter(a => a.indexOf('-') === 0);
            options = options || {};
            optionArgs.forEach(a => {
                var parseage = a.substr(a.indexOf('--') === 0 ? 2 : 1);
                var name, value = true;
                var eqIndex = parseage.indexOf('=');
                if (eqIndex >= 0) {
                    name = parseage.substr(0, eqIndex);
                    value = parseage.substr(eqIndex + 1);
                } else {
                    name = parseage;
                }
                options[name] = value;
            });
            positionals['options'] = options;
            return positionals;
        },

        indexOf: function(array, predicate, nposValue) {
            for (var i = 0; i < array.length; i++) {
                if (predicate(array[i], i)) {
                    return i;
                }
            }
            nposValue = typeof(nposValue) === 'undefined' ? -1 : nposValue;
            return nposValue;
        },

        contains: function(array, predicate) {
            if (typeof(predicate) !== 'function') {
                return array.indexOf(predicate) >= 0;
            }
            for (var i = 0; i < array.length; i++) {
                if (predicate(array[i], i)) {
                    return true;
                }
            }
            return false;
        },

        count: function(array, predicate) {
            var n = 0;
            for (var i = 0; i < array.length; i++) {
                if (predicate(array[i], i)) {
                    n++;
                }
            }
            return n;            
        },

        repeat: function(numElves, value) {
            var elves = new Array();
            var valueFactory = typeof(value) === 'function' ? value : () => value;
            for (var i = 0; i < numElves; i++) {
                elves.push(valueFactory(i));
            }
            return elves;
        }
    };

})();
