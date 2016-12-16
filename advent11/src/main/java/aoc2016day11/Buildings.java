package aoc2016day11;

import java.util.EnumSet;
import java.util.stream.Collectors;

import static aoc2016day11.Element.P;
import static aoc2016day11.Element.X;
import static aoc2016day11.Element.R;
import static aoc2016day11.Element.S;
import static aoc2016day11.Element.T;
import static aoc2016day11.Item.generator;
import static aoc2016day11.Item.microchip;
import static java.util.Arrays.asList;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class Buildings {

    private Buildings() {}

    public static Building oneMoveFromWinning() {
        Floor.Factory ff = Floor.Factory.getInstance();
        Building b = new Building(1, asList(
                ff.empty(),
                ff.get("TM"),
                ff.get("TG", "SG", "SM")
        ));
        return b;
    }

    public static Building twoMovesFromWinning() {
        Floor.Factory ff = Floor.Factory.getInstance();
        Building b = new Building(2, asList(
                ff.empty(),
                ff.get("TM"),
                ff.get("TG", "SG", "SM")
        ));
        return b;
    }

/*
F4 .  .  .  .  LM
F3 E  HG HM LG .
F2 .  .  .  .  .
F1 .  .  .  .  .
 */
    public static Building threeMovesFromWinning() {
        Floor.Factory ff = Floor.Factory.getInstance();
        Building b = new Building(2, asList(
                ff.empty(),
                ff.empty(),
                ff.get("TM", "TG", "SG"),
                ff.get("SM")
        ));
        return b;
    }

    public static Building createBuildingWith4FloorsAndEverythingOnThirdFloor() {
        Floor.Factory floorFactory = Floor.Factory.getInstance();
        Building b = new Building(2,
                asList(
                        floorFactory.empty(),
                        floorFactory.empty(),
                        floorFactory.get(EnumSet.allOf(Item.class)),
                        floorFactory.empty()
                ));
        return b;
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
        Floor.Factory floorFactory = Floor.Factory.getInstance();
        Item PG = generator(P), PM = microchip(P);
        Item XG = generator(X), XM = microchip(X);
        Item RG = generator(R), RM = microchip(R);
        Item SG = generator(S), SM = microchip(S);
        Item TG = generator(T), TM = microchip(T);

        Building building = Building.onFirstFloor(asList(
                floorFactory.get(asList(TG, TM, PG, SG)),
                floorFactory.get(asList(PM, SM)),
                floorFactory.get(asList(XG, XM, RG, RM)),
                floorFactory.empty()
        ));
        return building;
    }

    public static Building createExampleBuilding() {
        /*
        The first floor contains a hydrogen-compatible microchip
            and a lithium-compatible microchip.
        The second floor contains a hydrogen generator.
        The third floor contains a lithium generator.
        The fourth floor contains nothing relevant.
         */
        Item hg = generator(P);
        Item hm = microchip(P);
        Item lg = generator(R);
        Item lm = microchip(R);
        Floor.Factory ff = Floor.Factory.getInstance();
        return Building.onFirstFloor(asList(ff.get(asList(hm, lm)), ff.get(asList(hg)), ff.get(asList(lg)), ff.empty()));
    }

}
