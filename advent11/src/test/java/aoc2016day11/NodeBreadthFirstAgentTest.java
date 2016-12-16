package aoc2016day11;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class NodeBreadthFirstAgentTest extends AgentTestBase {


    @Test
    public void oneMoveAway() {
        testKnownMovesAway(Buildings.oneMoveFromWinning(), 1);
    }

    @Test
    public void twoMovesAway() {
        testKnownMovesAway(Buildings.twoMovesFromWinning(), 2);
    }

    @Test
    public void threeMovesAway() {
        testKnownMovesAway(Buildings.threeMovesFromWinning(), 3);
    }

    @Test
    public void testSimple1() {
        Building b = build("PG", "PM").then().finish();
        testCanWin(b, new NodeBreadthFirstAgent(2));
    }

    @Test
    public void testSimple2() {
        Building b = build("PG", "PM", "TG", "TM").then().finish();
        testCanWin(b, new NodeBreadthFirstAgent(100));
    }

    @Test
    public void testSimple3() {
        Building b = build("PG", "PM", "TG", "TM", "SG", "SM").then().finish();
        testCanWin(b, new NodeBreadthFirstAgent(100));
    }

    @Ignore
    @Test
    public void testSimple4() {
        Building b = build("PG", "PM", "RG", "RM", "TG", "TM", "SG", "SM").then().finish();
        testCanWin(b, new NodeBreadthFirstAgent(100).toggleVerbose());
    }

    private void testCanWin(Building b, Agent agent) {
        Optional<List<Building>> path = agent.play(b);
        assertTrue("no path to victory", path.isPresent());
        System.out.format("%d moves to win%n", Building.count(path.get()));
        Building.dump(path.get(), System.out);
        System.out.format("%d moves to win%n", Building.count(path.get()));
    }

    private void testKnownMovesAway(Building b, int expected) {
        testKnownMovesAway(b, expected, new NodeBreadthFirstAgent(expected + 1));
    }
}