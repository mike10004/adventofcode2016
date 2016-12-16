package aoc2016day11;

import org.junit.Test;

import static org.junit.Assert.*;

public class NodeDepthFirstAgentTest extends AgentTestBase {

    @Test
    public void playExample() throws Exception {
        testPlayExample(new NodeDepthFirstAgent(11));
    }

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

    private void testKnownMovesAway(Building b, int expected) {
        testKnownMovesAway(b, expected, new NodeDepthFirstAgent(expected + 1));
    }
}