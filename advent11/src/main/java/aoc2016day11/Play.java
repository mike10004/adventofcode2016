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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interactive game.
 */
public class Play {

    public static void main(String[] args) throws Exception {
        Building b = Buildings.createBuildingWith4FloorsAndEverythingOnThirdFloor();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            while (!b.isWin()) {
                b.dump(System.out).println();
                List<Move> moves = b.listValidMoves(Collections.emptySet()).collect(Collectors.toList());
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

}
