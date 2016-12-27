package aoc2016.day22;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PartTwo {

    public static void main(String[] args) throws Exception {
        List<Node> nodes = Node.parseAll(Play.puzzleInput, 37, 0);
        Grid grid = Grid.make(nodes);
        System.out.println(grid);
        System.out.println();
        Grid.FindShortestCallback callback = new Grid.FindShortestCallback() {

            private int level = -1;
            private final int messageInterval = 10000;
            @Override
            public boolean dequeued(Queue<Grid> queue, Grid element) {
                if (element.level > level) {
                    level = element.level;
                    System.out.format("searching at level %d%n", level);
                }
                int size = queue.size();
                if (size > 0 && size % messageInterval == 0) {
                    System.out.format("%d nodes in queue%n", size);
                }
                return true;
            }
        };
//        Grid win = grid.findShortestWinningStrategy(new Point(0, 0), 256, callback);
//        if (win == null) {
//            System.out.println("no winning strategy found");
//        } else {
//            System.out.format("%d moves to win%n", win.level);
//        }
    }
}
