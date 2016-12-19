package aoc2016day11;

import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class BreadthFirstAgentTest extends AgentTestBase {


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
        Building b = Buildings.build(2).add(0, 0).finish(0);
        testCanWin(b, new BreadthFirstAgent(2));
    }

    @Test
    public void testSimple2() {
        Building b = Buildings.build(2).add(0, 0).add(0, 0).finish(0);
        testCanWin(b, new BreadthFirstAgent(100));
    }

    @Test
    public void testSimple3() {
        Building b = Buildings.build(2).add(0, 0).add(0, 0).add(0, 0).finish(0);
        testCanWin(b, new BreadthFirstAgent(100));
    }

    private void testCanWin(Building b, Agent agent) {
        System.out.println("testCanWin");
        System.out.print(b);
        Optional<List<Building>> path = agent.play(b);
        assertTrue("no path to victory", path.isPresent());
        System.out.format("%d moves to win%n", Buildings.countMoves(path.get()));
        Buildings.dump(path.get(), System.out);
        System.out.format("%d moves to win%n", Buildings.countMoves(path.get()));
    }

    private void testKnownMovesAway(Building b, int expected) {
        System.out.println("testKnownMovesAway: " + expected);
        System.out.println(b);
        testKnownMovesAway(b, expected, new BreadthFirstAgent(expected + 1));
    }

    @Test
    public void test9() {
        Building b = Buildings.gameWith4NextMoves();
        Agent agent = new BreadthFirstAgent(10);
        Optional<List<Building>> result = agent.play(b);
        System.out.println(result);
    }
}