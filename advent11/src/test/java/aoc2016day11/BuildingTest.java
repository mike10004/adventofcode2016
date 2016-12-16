package aoc2016day11;

import org.junit.Test;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import static aoc2016day11.Direction.DOWN;
import static aoc2016day11.Direction.UP;
import static aoc2016day11.Element.P;
import static aoc2016day11.Element.R;
import static aoc2016day11.Element.S;
import static aoc2016day11.Item.generator;
import static aoc2016day11.Kind.generator;
import static aoc2016day11.Kind.microchip;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class BuildingTest {

    @Test
    public void isEverythingAtTopFloor() throws Exception {
        assertTrue(building(0, floor(generator(S))).isEverythingAtTopFloor());
        assertTrue(building(2, floor(), floor(), floor(generator(S))).isEverythingAtTopFloor());
        assertFalse(building(1, floor(), floor(generator(S)), floor()).isEverythingAtTopFloor());
    }

    @Test
    public void isElevatorAtTop() throws Exception {
        assertTrue(building(0, floor(generator(S))).isElevatorAtTop());
        assertTrue(building(1, floor(), floor(generator(S))).isElevatorAtTop());
        assertFalse(building(0, floor(generator(S)), floor()).isElevatorAtTop());
    }

    @Test
    public void dump() throws Exception {
        Buildings.createExampleBuilding().dump(System.out);
    }

    private static Item findItem(Building b, Kind kind, Element element) {
        @Nullable Item item = Item.maybeFindItem(b.items(), kind, element);
        if (item == null) {
            throw new NoSuchElementException(element.symbol + " " + kind.symbol);
        }
        return item;
    }
    @Test
    public void givenTestCase() throws Exception {
        Building b = Buildings.createExampleBuilding();
        Item HG = findItem(b, generator, P), HM = findItem(b, microchip, P);
        Item LG = findItem(b, generator, R), LM = findItem(b, microchip, R);
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

    @Test
    public void equivalence() {
        Building b = Building.onFirstFloor(Arrays.asList(
                floor(Item.PG, Item.PM, Item.SG, Item.SM),
                floor()
        ));
        Building.MoveStream moves = b.findValidMovesExcept(Collections.emptyList());
        List<Building.Move> moveList = moves.collect(Collectors.toList());
        System.out.format("%d moves: %s%n", moveList.size(), moveList);
        assertEquals("num moves", 4, moveList.size());
    }

    private static <E> Set<E> asList(E e) {
        return Collections.singleton(e);
    }

    private static <E extends Enum<E>> Set<E> asList(E e1, E e2) {
        return EnumSet.of(e1, e2);
    }

    private static Building building(int elevatorPosition, Floor...floors) {
        return new Building(elevatorPosition, Arrays.asList(floors));
    }

    private static Floor floor(Item...items) {
        return Floor.Factory.getInstance().get(Arrays.asList(items));
    }
}