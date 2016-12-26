package aoc2016.day22;

import java.awt.*;
import java.util.List;

public class PartTwo {

    public static void main(String[] args) throws Exception {
        List<Node> nodes = Node.parseAll(Play.puzzleInput, 37, 0);
        Grid grid = Grid.make(nodes);
        System.out.println(grid);
        System.out.println();
        Grid win = grid.findShortestWinningStrategy(new Point(0, 0), 256);
        if (win == null) {
            System.out.println("no winning strategy found");
        } else {
            System.out.format("%d moves to win%n", win.level);
        }
    }
}
