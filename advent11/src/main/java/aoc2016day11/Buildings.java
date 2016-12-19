package aoc2016day11;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class Buildings {

    private Buildings() {}

    public static int countMoves(Collection<Building> moves) {
        if (moves.isEmpty()) {
            return 0;
        }
        return moves.size() - 1;
    }

    public static class Builder {
        private final int numFloors;
        private final List<int[]> elements = new ArrayList<>();

        private Builder(int numFloors) {
            this.numFloors = numFloors;
        }

        public Building finish(int elevator) {
            Building.ElementCache cache = new Building.ElementCache(numFloors);
            Element[] elementsList = elements.stream().map(p -> new Element(p[0], p[1])).collect(Collectors.toList()).toArray(new Element[0]);
            return new Building(numFloors, elevator, elementsList, cache);
        }

        public Builder add(int microchip, int generator) {
            elements. add(new int[]{microchip, generator});
            return this;
        }
    }

    public static Builder build(int numFloors) {
        return new Builder(numFloors);
    }

    public static Building oneMoveFromWinning() {
        return build(3)
                .add(1, 2)
                .add(2, 2)
                .finish(1);
    }

    public static Building twoMovesFromWinning() {
        return build(3)
                .add(1, 2)
                .add(2, 2)
                .finish(2);
    }

/*
F4 .  .  .  .  LM
F3 E  HG HM LG .
F2 .  .  .  .  .
F1 .  .  .  .  .
 */
    public static Building threeMovesFromWinning() {
        return build(4)
                .add(2, 2)
                .add(2, 3)
                .finish(2);
    }

    public static Building createBuildingWith4FloorsAndEverythingOnThirdFloor() {
        throw new UnsupportedOperationException();
    }

    /*
The first floor contains
    a thulium generator,
    a thulium-compatible microchip,
    a plutonium generator, and
    a strontium generator.
The second floor contains
    a plutonium-compatible microchip and
    a strontium-compatible microchip.
The third floor contains
    a promethium generator,
    a promethium-compatible microchip,
    a ruthenium generator, and
    a ruthenium-compatible microchip.
The fourth floor contains nothing relevant.
 */
    public static Building createPuzzleInputBuilding() {
        return build(4)
                .add(1, 0) // plutonium
                .add(2, 2) // promethium
                .add(2, 2) // ruthenium
                .add(1, 0) // strontium
                .add(0, 0) // thulium
                .finish(0);
    }

    public static Building createPartTwoPuzzleInputBuilding() {
        return build(4)
                .add(1, 0) // plutonium
                .add(2, 2) // promethium
                .add(2, 2) // ruthenium
                .add(1, 0) // strontium
                .add(0, 0) // thulium
                .add(0, 0) // elerium
                .add(0, 0) // dilithium
                .finish(0);
    }

    public static Building createExampleBuilding() {
        /*
        The first floor contains a hydrogen-compatible microchip
            and a lithium-compatible microchip.
        The second floor contains a hydrogen generator.
        The third floor contains a lithium generator.
        The fourth floor contains nothing relevant.
         */
//        Item hg = generator(P);
//        Item hm = microchip(P);
//        Item lg = generator(R);
//        Item lm = microchip(R);
//        Floor.Factory ff = Floor.Factory.getInstance();
//        return Building.onFirstFloor(asList(ff.get(asList(hm, lm)), ff.get(asList(hg)), ff.get(asList(lg)), ff.empty()));
        return build(4)
                .add(0, 1)
                .add(0, 2)
                .finish(0);
    }

    public static void dump(Iterable<Building> moves, PrintStream out) {
        for (Building b : moves) {
            out.println(b);
        }
    }

    public static Building gameWith4NextMoves() {
        /*
 4  .  .  .  .  .  .  .  .  .  .  .
 3  E PG PM XG XM RG RM  .  .  .  .
 2  .  .  .  .  .  .  . SG SM  .  .
 1  .  .  .  .  .  .  .  .  . TG TM
         */
        return Buildings.build(4)
                .add(2, 2)
                .add(2, 2)
                .add(2, 2)
                .add(1, 1)
                .add(0, 0)
                .finish(2);

    }
}
