function Accumulator() {

  var well = [];

  var getDistribution = function(pos) {
    var dist = well[pos];
    if (!dist) {
      well[pos] = dist = {};
    }
    return dist;
  };

  var comparator = function(mode) {
    return function(a, b) {
      switch (mode) {
        case 'lt': return a < b;
        case 'le': return a <= b;
        case 'eq': return a === b;
        case 'ge': return a >= b;
        case 'gt': return a > b;
        default: throw Error('mode unrecognized: ' + mode);
      }
    };
  }

  var find = function(dist, comparator) {
    var mc, max;
    for (var ch in dist) {
      if (typeof(max) === 'undefined' || comparator(dist[ch], max)) {
        max = dist[ch];
        mc = ch;
      }
    }
    return mc;
  };

  var incrementCharacter = function(distribution, ch) {
    distribution[ch] = (distribution[ch] || 0) + 1;
    return distribution; 
  };

  this.update = function(message) {
    for (var pos in message) {
      incrementCharacter(getDistribution(pos), message[pos]);
    }
  };

  this.compute = function(mode) {
    return well.map((dist) => find(dist, comparator(mode))).join('');
  }

  var reset = function() {
    return well.splice(0, well.length);
  }

  this.unitTest = function(){
    var assert = require('assert');
    assert.equal(find({'a': 1, 'b': 2, 'c': 4, 'd': 2}, comparator('gt')), 'c', "findMostCommon");
    assert.equal(find({'a': 1, 'b': 2, 'c': 4, 'd': 2}, comparator('lt')), 'a', "findLeastCommon");
    assert.deepEqual(incrementCharacter({'a': 1, 'b': 3}, 'a'), {'a': 2, 'b': 3}, "incrementCharacter");
  };
}

if (process.argv.indexOf('--test') >= 0) {
  new Accumulator().unitTest();
  console.log("test finished");
  process.exit(0);
}

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
      var acc = new Accumulator();
      var messages = chunk.split(/\s+/);
      messages.forEach(acc.update);
      console.error("processed " + chunk.length + " as " + messages.length + " messages");
      console.log(acc.compute(process.argv.reverse()[0]));
    }
  });
})();
