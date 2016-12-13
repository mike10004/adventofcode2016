package aoc2016day11;

import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class AgentTest {

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void playExample() throws Exception {
        Building exampleBuilding = BuildingTest.example();
        Agent agent = new Agent(11) {
            @Override
            protected void depthChanged(int newDepth) {
                System.out.format("examining strategies at depth %d%n", newDepth);
            }
        };
        Optional<List<Building>> result = agent.play(exampleBuilding);
        assertTrue("result absent", result.isPresent());
        Building.dump(result.get(), System.out);
        int count = Building.count(result.get());
        System.out.format("winning path has %d moves%n", count);
        assertEquals("winning path numMoves", 11, count);
    }

}