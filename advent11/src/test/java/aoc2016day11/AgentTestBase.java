package aoc2016day11;

import org.junit.Assert;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AgentTestBase {

//    protected static class Builder {
//        private final Floor.Factory ff = Floor.Factory.getInstance();
//        private List<Floor> floors = new ArrayList<>();
//        private int elevatorPosition = 0;
//
//        private Builder() {
//
//        }
//
//        public Builder(Floor first) {
//            floors.add(first);
//        }
//
//        public Builder on(String...items) {
//            then(items);
//            elevatorPosition = floors.size() - 1;
//            return this;
//        }
//        public Builder then(String...items) {
//            Floor f = ff.get(items);
//            checkArgument(Item.areSafe(f.items));
//            floors.add(f);
//            return this;
//        }
//
//        public Builder empty() {
//            floors.add(ff.get());
//            return this;
//        }
//        public Building finish() {
//            return new Building(elevatorPosition, floors);
//        }
//    }

//    protected Builder build(String...items) {
//        return new Builder().then(items);
//    }
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    protected void testPlayExample(Agent agent) {
        Building exampleBuilding = Buildings.createExampleBuilding();
        Optional<List<Building>> result = agent.play(exampleBuilding);
        assertTrue("result absent", result.isPresent());
        Buildings.dump(result.get(), System.out);
        int count = Buildings.countMoves(result.get());
        System.out.format("winning path has %d moves%n", count);
        assertEquals("winning path numMoves", 11, count);
    }

    protected void testKnownMovesAway(Building b, int expected, Agent agent) {
        System.out.format("testing agent with board %d moves from winning...%n", expected);
        Optional<List<Building>> strategy = agent.play(b);
        if (strategy.isPresent()) {
            int actual = Buildings.countMoves(strategy.get());
            assertEquals("expected moves count", expected, actual);
        } else {
            Assert.fail("no strategy found starting from " + b + " with expected = " + expected);
        }
    }}