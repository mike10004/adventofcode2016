package aoc2016day11;

import org.junit.Assert;
import org.junit.Test;

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

    private void testKnownMovesAway(Building b, int expected) {
        testKnownMovesAway(b, expected, new NodeBreadthFirstAgent(expected + 1));
    }
}