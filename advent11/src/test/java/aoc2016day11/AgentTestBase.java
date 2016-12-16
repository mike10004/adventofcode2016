package aoc2016day11;

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

}