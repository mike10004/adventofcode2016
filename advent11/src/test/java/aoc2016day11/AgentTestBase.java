package aoc2016day11;

import org.junit.Assert;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AgentTestBase {

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    protected void testPlayExample(Agent agent) {
        Building exampleBuilding = Buildings.createExampleBuilding();
        Optional<List<Building>> result = agent.play(exampleBuilding);
        assertTrue("result absent", result.isPresent());
        Building.dump(result.get(), System.out);
        int count = Building.count(result.get());
        System.out.format("winning path has %d moves%n", count);
        assertEquals("winning path numMoves", 11, count);
    }

    protected void testKnownMovesAway(Building b, int expected, Agent agent) {
        System.out.format("testing agent with board %d moves from winning...%n", expected);
        Optional<List<Building>> strategy = agent.play(b);
        if (strategy.isPresent()) {
            int actual = Building.count(strategy.get());
            assertEquals("expected moves count", expected, actual);
        } else {
            Assert.fail("no strategy found starting from " + b + " with expected = " + expected);
        }
    }}