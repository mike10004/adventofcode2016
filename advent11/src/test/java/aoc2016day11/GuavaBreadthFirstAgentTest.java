package aoc2016day11;

import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class GuavaBreadthFirstAgentTest extends AgentTestBase {

    @org.junit.Ignore
    @Test
    public void playAlmostWon_breadthFirst() throws Exception {
        Building building = Buildings.createBuildingWith4FloorsAndEverythingOnThirdFloor();
        Agent agent = new GuavaBreadthFirstAgent(11);
        Optional<List<Building>> result = agent.play(building);
        assertTrue("result absent", result.isPresent());
        Building.dump(result.get(), System.out);
        int count = Building.count(result.get());
        System.out.format("winning path has %d moves%n", count);
    }

    @Test
    public void playExample() {
        testPlayExample(new GuavaBreadthFirstAgent(11));
    }
}