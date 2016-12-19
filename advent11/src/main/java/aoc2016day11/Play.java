package aoc2016day11;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Interactive game.
 */
public class Play {

    public static void main(String[] args) throws Exception {
//        MyBuilding b = (MyBuilding) Buildings.createBuildingWith4FloorsAndEverythingOnThirdFloor();
        MyBuilding b = (MyBuilding) Buildings.gameWith9NextMoves();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            while (!b.isWin()) {
                System.out.println(b);
                List<MyBuilding.Move> moves = b.findValidMoves().collect(Collectors.toList());
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
                    MyBuilding.Move move = moves.get(moveIndex);
                    b = b.move(move.direction, move.offsets.entries().stream().map(e -> new Target(e.getKey(), e.getValue())).collect(Collectors.toSet()));
                } else {
                    System.err.println("EOF on stdin");
                }
            }
        }

    }

}
