package aoc2016.day22;

import com.google.common.io.CharSource;

import java.io.IOException;
import java.util.List;

public class PartOne {

    public static void main(String[] args) throws Exception {
        int numViablePairs = countViablePairs(Play.puzzleInput);
        System.out.format("%d viable pairs%n", numViablePairs);
    }

    static int countViablePairs(CharSource input) throws IOException {
        List<Node> nodes = Node.parseAll(input, -1, -1);
        System.out.format("%d nodes; max x-coordinate: %d%n", nodes.size(), Grid.findMaxX(nodes.stream()));
        int numViablePairs = 0;
        for (Node a : nodes) {
            for (Node b : nodes) {
                if (a.canMoveDataTo(b)) {
                    numViablePairs++;
                }
            }
        }
        return numViablePairs;
    }
}
