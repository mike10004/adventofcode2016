package aoc2016day11;

import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class DepthFirstAgentTest extends AgentTestBase {

    @Test
    public void playExample() throws Exception {
        testPlayExample(new DepthFirstAgent(11));
    }

    @org.junit.Ignore
    @Test
    public void playAlmostWon_depthFirst() throws Exception {
        Building building = Buildings.createBuildingWith4FloorsAndEverythingOnThirdFloor();
        Agent agent = new DepthFirstAgent(11);
        Optional<List<Building>> result = agent.play(building);
        assertTrue("result absent", result.isPresent());
        Building.dump(result.get(), System.out);
        int count = Building.count(result.get());
        System.out.format("winning path has %d moves%n", count);
    }
}