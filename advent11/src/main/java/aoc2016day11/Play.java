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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static aoc2016day11.Element.plutonium;
import static aoc2016day11.Element.promethium;
import static aoc2016day11.Element.ruthenium;
import static aoc2016day11.Element.strontium;
import static aoc2016day11.Element.thulium;
import static aoc2016day11.Item.generator;
import static aoc2016day11.Item.microchip;

public class Play {

    public static void main(String[] args) throws Exception {
        Building b = Building.onFirstFloor(Arrays.asList(
                Floor.empty(),
                Floor.empty(),
                new Floor(Item.forElements(EnumSet.of(plutonium, promethium, ruthenium, strontium, thulium))),
                Floor.empty()
        ));
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

    static Building createPuzzleInputBuilding() {
        Item PG = generator(plutonium), PM = microchip(plutonium);
        Item XG = generator(promethium), XM = microchip(promethium);
        Item RG = generator(ruthenium), RM = microchip(ruthenium);
        Item SG = generator(strontium), SM = microchip(strontium);
        Item TG = generator(thulium), TM = microchip(thulium);

        Building building = Building.onFirstFloor(Arrays.asList(
                new Floor(Arrays.asList(TG, TM, PG, SG)),
                new Floor(Arrays.asList(PM, SM)),
                new Floor(Arrays.asList(XG, XM, RG, RM)),
                Floor.empty()
        ));
        return building;
    }

}
