package aoc2016day11;

import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class AgentTest {

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void playExample() throws Exception {
        Building exampleBuilding = Play.createExampleBuilding();
        Agent agent = new VerboseAgent(11);
        Optional<List<Building>> result = agent.play(exampleBuilding);
        assertTrue("result absent", result.isPresent());
        Building.dump(result.get(), System.out);
        int count = Building.count(result.get());
        System.out.format("winning path has %d moves%n", count);
        assertEquals("winning path numMoves", 11, count);
    }

    @Test
    public void playAlmostWon_breadthFirst() throws Exception {
        Building building = Play.createBuildingWith4FloorsAndEverythingOnThirdFloor();
        Agent agent = new VerboseAgent(11);
        Optional<List<Building>> result = agent.play(building);
        assertTrue("result absent", result.isPresent());
        Building.dump(result.get(), System.out);
        int count = Building.count(result.get());
        System.out.format("winning path has %d moves%n", count);
    }

    @Test
    public void playAlmostWon_depthFirst() throws Exception {
        Building building = Play.createBuildingWith4FloorsAndEverythingOnThirdFloor();
        Agent agent = new VerboseAgent(11);
        Optional<List<Building>> result = agent.playDepthFirst(building);
        assertTrue("result absent", result.isPresent());
        Building.dump(result.get(), System.out);
        int count = Building.count(result.get());
        System.out.format("winning path has %d moves%n", count);
    }

}