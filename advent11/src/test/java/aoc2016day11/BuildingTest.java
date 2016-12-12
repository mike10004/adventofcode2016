package aoc2016day11;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static aoc2016day11.Kind.generator;
import static aoc2016day11.Kind.microchip;
import static org.junit.Assert.*;
import static aoc2016day11.Element.*;
import static aoc2016day11.Item.generator;
import static aoc2016day11.Item.microchip;
import static aoc2016day11.Direction.UP;
import static aoc2016day11.Direction.DOWN;


@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class BuildingTest {

    @Test
    public void isEverythingAtTopFloor() throws Exception {
        assertTrue(building(2, floor()).isEverythingAtTopFloor());
        assertTrue(building(2, floor(), floor(), floor(generator(strontium))).isEverythingAtTopFloor());
        assertFalse(building(2, floor(), floor(generator(strontium)), floor()).isEverythingAtTopFloor());
    }

    @Test
    public void isElevatorAtTop() throws Exception {
        assertTrue(building(0, floor()).isElevatorAtTop());
        assertTrue(building(1, floor(), floor()).isElevatorAtTop());
        assertFalse(building(0, floor(), floor()).isElevatorAtTop());
    }

    @Test
    public void dump() throws Exception {
        example().dump(System.out);
    }

    @Test
    public void givenTestCase() throws Exception {
        Building b = example();
        Item HG = b.findItem(generator, hydrogen), HM = b.findItem(microchip, hydrogen);
        Item LG = b.findItem(generator, lithium), LM = b.findItem(microchip, lithium);
/*
1. Bring the Hydrogen-compatible Microchip to the second floor
2. Bring both Hydrogen-related items to the third floor
3. Leave the Hydrogen Generator on floor three, but bring the Hydrogen-compatible
   Microchip back down with you
4. At the first floor, grab the Lithium-compatible Microchip
5. Bring both Microchips up one floor
6. Bring both Microchips up again to floor three
7. Bring both Microchips to the fourth floor
8. Leave the Lithium-compatible microchip on the fourth floor, but bring the Hydrogen-compatible one so you can still use the elevator
9. Bring both Generators up to the fourth floor
10. Bring the Lithium Microchip with you to the third floor so you can use the elevator
11. Bring both Microchips to the fourth floor
 */
        System.out.println("start:");
        dump(b);
        b = b.moveChecked(UP, asList(HM));
        dump(b);
        b = b.moveChecked(UP, asList(HG, HM));
        dump(b);
        b = b.moveChecked(DOWN, asList(HM));
        dump(b);
        b = b.moveChecked(DOWN, asList(HM));
        dump(b);
        b = b.moveChecked(UP, asList(LM, HM));
        dump(b);
        b = b.moveChecked(UP, asList(LM, HM));
        dump(b);
        b = b.moveChecked(UP, asList(LM, HM));
        dump(b);
        b = b.moveChecked(DOWN, asList(HM));
        dump(b);
        b = b.moveChecked(UP, asList(LG, HG));
        dump(b);
        b = b.moveChecked(DOWN, asList(LM));
        dump(b);
        b = b.moveChecked(UP, asList(LM, HM));
        System.out.println("final:");
        b.dump(System.out);
        System.out.println();
        assertEquals("win", true, b.isWin());

    }

    private static void dump(Building b) {
        b.dump(System.out);
        System.out.println();
    }

    private static <E> List<E> asList(E e) {
        return Collections.singletonList(e);
    }

    private static <E> List<E> asList(E e1, E e2) {
        return Arrays.asList(e1, e2);
    }

    @Before
    public void resetPlacements() {
        Item.placements.set(0);
    }

    static Building example() {
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
        return building(0,
                floor(hm, lm),
                floor(hg),
                floor(lg),
                floor());
    }

    private static Building building(int elevatorPosition, Floor...floors) {
        return new Building(elevatorPosition, Arrays.asList(floors));
    }

    private static Floor floor(Item...items) {
        return new Floor(Stream.of(items));
    }
}