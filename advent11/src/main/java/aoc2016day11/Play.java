/*
 * (c) 2016 Novetta
 *
 * Created by mike
 */
package aoc2016day11;

import aoc2016day11.Building.Move;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
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

public class Play {

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

    public static void main(String[] args) throws Exception {
        Building b = createExampleBuilding();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            while (!b.isWin()) {
                b.dump(System.out).println();
                List<Move> moves = b.listValidMoves().collect(Collectors.toList());
                if (moves.isEmpty()) {
                    System.out.println("game over: no valid moves");
                    break;
                }
                List<String> validInputs = new ArrayList<>(moves.size());
                for (int i = 0; i < moves.size(); i++) {
                    System.out.format("[%2d] %s%n", i, moves.get(i));
                    validInputs.add(String.valueOf(i));
                }
                validInputs.add("quit");
                String line;
                while (true) {
                    System.out.println();
                    System.out.print("Enter move number (or quit): ");
                    line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    if (validInputs.contains(line.trim())) {
                        break;
                    }
                }
                if (line != null) {
                    if (line.equalsIgnoreCase("quit")) {
                        System.out.println("quitting");
                        break;
                    }
                    int moveIndex = Integer.parseInt(line.trim());
                    Move move = moves.get(moveIndex);
                    b = b.moveChecked(move.direction, move.carrying);
                } else {
                    System.err.println("EOF on stdin");
                }
            }
        }

    }

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
