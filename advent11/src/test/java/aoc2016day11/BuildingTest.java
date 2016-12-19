package aoc2016day11;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class BuildingTest {


    @Test
    public void testToString() {
        System.out.println(Buildings.createPuzzleInputBuilding());
    }

    @Test
    public void findMoves1() {
        Building b = Buildings.build(2)
                .add(0, 1)
                .finish(0);
        System.out.println(b);
        List<Building.Move> nexts = b.findValidMoves();
        System.out.println("valid next moves: " + nexts);
        assertEquals("num moves", 1, nexts.size());
    }

    @Test
    public void findMoves2() {
        Building b = Buildings.oneMoveFromWinning();
        System.out.println(b);
        List<Building.Move> moves = b.findValidMoves();
        System.out.println("next moves: " + moves);
        List<Building> states = moves.stream().map(m -> m.to)
                .collect(Collectors.toList());

        long numWins = states.stream().filter(Building::isWin).count();
        assertEquals("numWins", 1, numWins);
    }

    @Test
    public void findMoves3() {
        Building b = Buildings.build(2).add(0, 0).finish(0);
        System.out.println(b);
        List<Building.Move> moves = b.findValidMoves();
        System.out.println("next moves: " + moves);
        assertEquals("num next moves", 3, moves.size());
    }

    @Test
    public void testEquals() {

        Building b1 = Buildings.build(1).add(0, 0).finish(0);
        Building b2 = Buildings.build(1).add(0, 0).finish(0);
        assertEquals("simple", b1, b2);

        b1 = Buildings.build(2).add(0, 0).add(1, 1).finish(0);
        b2 = Buildings.build(2).add(1, 1).add(0, 0).finish(0);
        assertEquals("more complex", b1, b2);

    }

    @Test
    public void testFindUnprohibited() {
        Building start = Buildings.build(3)
                .add(0, 2)
                .add(2, 2)
                .finish(0);
        Building first = start.move(Direction.UP, new Target(0, Kind.M), null);
        List<Building> next = first.computeReachable(Collections.singleton(start)).collect(Collectors.toList());
        assertEquals("num next moves: ", 1, next.size());
    }

    @Test
    public void findValidMoves_2elements() {
        Building start = Buildings.build(3)
                .add(0, 0)
                .add(0, 0)
                .finish(0);
        System.out.print(start);
        List<Building.Move> moves = start.findValidMoves();
        // expect: move microchip up, move m+g up, move g+g up, move m+m up
        System.out.println("moves: " + moves);
        assertEquals("num moves", 4, moves.size());
    }

    @Test
    public void findValidMoves_3elements() {
        Building start = Buildings.build(3)
                .add(0, 0)
                .add(0, 0)
                .add(0, 0)
                .finish(0);
        System.out.print(start);
        List<Building.Move> moves = start.findValidMoves();
        // expect: move microchip up, move m+g up, move m+m up
        System.out.println("moves: " + moves);
        assertEquals("num moves", 3, moves.size());
    }

    @Test
    public void countPossibleStates() {
        Building b = Buildings.createPuzzleInputBuilding();
        System.out.print(b);
        System.out.format("...has %d possible states%n", b.countMaxPossibleStates());
    }

    @Test
    public void computeReachableEqualsFindValidMoves() {
        Building b = Buildings.gameWith4NextMoves();
        List<Building> reachable = b.computeReachable(Collections.emptyList()).collect(Collectors.toList());
        List<Building.Move> validMoves = b.findValidMoves();
        assertEquals("reachable.count == validMoves.count", validMoves.size(), reachable.size());
    }

    @Test
    public void isSafe() {
        Building b = Buildings.build(2)
                .add(0, 0)
                .add(0, 1)
                .finish(0);
        assertFalse(b.isSafe());
        b = Buildings.build(2)
                .add(0, 1)
                .add(1, 0)
                .finish(0);
        assertFalse(b.isSafe());
    }

    @Test
    public void computeHash_elevatorPosition() {
        Building b1 = Buildings.build(2)
                .add(0, 0)
                .add(1, 1)
                .finish(0);
        Building b2 = Buildings.build(2)
                .add(0, 0)
                .add(1, 1)
                .finish(1);
        assertFalse("hashes equal if elevator on different floor", b1.hash == b2.hash);
    }


    @Test
    public void computeHash_max() {
        int numFloors = 4;
        Buildings.Builder bb = Buildings.build(numFloors);
        for (int i = 0; i < 6; i++) {
            bb.add(numFloors - 1, numFloors - 1);
        }
        Building b = bb.finish(numFloors - 1);
        System.out.format("%shash = %d%n", b, b.hash);
    }


    @Test
    public void computeHash_min() {
        int numFloors = 4;
        Buildings.Builder bb = Buildings.build(numFloors);
        for (int i = 0; i < 6; i++) {
            bb.add(0, 0);
        }
        Building b = bb.finish(numFloors - 1);
        System.out.format("%shash = %d%n", b, b.hash);
    }}