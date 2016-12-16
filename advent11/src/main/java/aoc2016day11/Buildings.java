package aoc2016day11;

import java.util.EnumSet;
import java.util.stream.Collectors;

import static aoc2016day11.Element.hydrogen;
import static aoc2016day11.Element.lithium;
import static aoc2016day11.Element.plutonium;
import static aoc2016day11.Element.promethium;
import static aoc2016day11.Element.ruthenium;
import static aoc2016day11.Element.strontium;
import static aoc2016day11.Element.thulium;
import static aoc2016day11.Item.generator;
import static aoc2016day11.Item.microchip;
import static java.util.Arrays.asList;

public class Buildings {

    private Buildings() {}

    public static Building createBuildingWith4FloorsAndEverythingOnThirdFloor() {
        Floor.Factory floorFactory = Floor.Factory.getInstance();
        Building b = new Building(2,
                asList(
                        floorFactory.empty(),
                        floorFactory.empty(),
                        floorFactory.get(Item.forElements(EnumSet.of(plutonium, promethium, ruthenium, strontium, thulium)).collect(Collectors.toSet())),
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
        Item PG = generator(plutonium), PM = microchip(plutonium);
        Item XG = generator(promethium), XM = microchip(promethium);
        Item RG = generator(ruthenium), RM = microchip(ruthenium);
        Item SG = generator(strontium), SM = microchip(strontium);
        Item TG = generator(thulium), TM = microchip(thulium);

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
        Item hg = generator(hydrogen);
        Item hm = microchip(hydrogen);
        Item lg = generator(lithium);
        Item lm = microchip(lithium);
        Floor.Factory ff = Floor.Factory.getInstance();
        return Building.onFirstFloor(asList(ff.get(asList(hm, lm)), ff.get(asList(hg)), ff.get(asList(lg)), ff.empty()));
    }

}
