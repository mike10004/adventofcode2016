package aoc2016day11;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class MyBuildingTest {


    @Test
    public void testToString() {
        System.out.println(Buildings.createPuzzleInputBuilding());
    }

    @Test
    public void findMoves1() {
        MyBuilding b = (MyBuilding) Buildings.build(2)
                .add(0, 1)
                .finish(0);
        System.out.println(b);
        List<MyBuilding.Move> nexts = b.findValidMoves().collect(Collectors.toList());
        System.out.println("valid next moves: " + nexts);
        assertEquals("num moves", 1, nexts.size());
    }

    @Test
    public void findMoves2() {
        MyBuilding b = (MyBuilding) Buildings.oneMoveFromWinning();
        System.out.println(b);
        List<MyBuilding.Move> moves = b.findValidMoves().collect(Collectors.toList());
        System.out.println("next moves: " + moves);
        List<Building> states = moves.stream().map(MyBuilding.Move::perform)
                .map(r -> r.building).collect(Collectors.toList());

        long numWins = states.stream().filter(Building::isWin).count();
        assertEquals("numWins", 1, numWins);
    }

    @Test
    public void findMoves3() {
        MyBuilding b = (MyBuilding) Buildings.build(2).add(0, 0).finish(0);
        System.out.println(b);
        List<MyBuilding.Move> moves = b.findValidMoves().collect(Collectors.toList());
        System.out.println("next moves: " + moves);
        assertEquals("num next moves", 3, moves.size());
    }

    @Test
    public void testEquals() {

        MyBuilding b1 = Buildings.build(1).add(0, 0).finish(0);
        MyBuilding b2 = Buildings.build(1).add(0, 0).finish(0);
        assertEquals("simple", b1, b2);

        b1 = Buildings.build(2).add(0, 0).add(1, 1).finish(0);
        b2 = Buildings.build(2).add(1, 1).add(0, 0).finish(0);
        assertEquals("more complex", b1, b2);

    }

    @Test
    public void testFindUnprohibited() {
        MyBuilding start = Buildings.build(3)
                .add(0, 2)
                .add(2, 2)
                .finish(0);
        MyBuilding first = start.move(Direction.UP, 0, Kind.M);
        List<Building> next = first.computeReachable(Collections.singleton(start)).collect(Collectors.toList());
        assertEquals("num next moves: ", 1, next.size());
    }

    @Test
    public void findValidMoves_2elements() {
        MyBuilding start = Buildings.build(3)
                .add(0, 0)
                .add(0, 0)
                .finish(0);
        System.out.print(start);
        List<MyBuilding.Move> moves = start.findValidMoves().collect(Collectors.toList());
        // expect: move microchip up, move m+g up, move g+g up, move m+m up
        System.out.println("moves: " + moves);
        assertEquals("num moves", 4, moves.size());
    }

    @Test
    public void findValidMoves_3elements() {
        MyBuilding start = Buildings.build(3)
                .add(0, 0)
                .add(0, 0)
                .add(0, 0)
                .finish(0);
        System.out.print(start);
        List<MyBuilding.Move> moves = start.findValidMoves().collect(Collectors.toList());
        // expect: move microchip up, move m+g up, move m+m up
        System.out.println("moves: " + moves);
        assertEquals("num moves", 3, moves.size());
    }

    @Test
    public void countPossibleStates() {
        MyBuilding b = (MyBuilding) Buildings.createPuzzleInputBuilding();
        System.out.print(b);
        System.out.format("...has %d possible states%n", b.countMaxPossibleStates());
    }

    @Test
    public void computeReachableEqualsFindValidMoves() {
        MyBuilding b = (MyBuilding) Buildings.gameWith9NextMoves();
        List<Building> reachable = b.computeReachable(Collections.emptyList()).collect(Collectors.toList());
        List<MyBuilding.Move> validMoves = b.findValidMoves().collect(Collectors.toList());
        assertEquals("reachable.count == validMoves.count", validMoves.size(), reachable.size());
    }
}