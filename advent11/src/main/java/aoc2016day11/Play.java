package aoc2016day11;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Interactive game.
 */
public class Play {

    public static void main(String[] args) throws Exception {
//        MyBuilding b = (MyBuilding) Buildings.createBuildingWith4FloorsAndEverythingOnThirdFloor();
        Building b = (Building) Buildings.gameWith4NextMoves();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            while (!b.isWin()) {
                System.out.println(b);
                List<Building.Move> moves = b.findValidMoves();
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
                    Building.Move move = moves.get(moveIndex);
                    b = b.move(move.direction, move.target1, move.target2);
                } else {
                    System.err.println("EOF on stdin");
                }
            }
        }

    }

}
