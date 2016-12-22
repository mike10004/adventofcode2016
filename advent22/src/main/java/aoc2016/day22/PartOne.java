package aoc2016.day22;

import com.google.common.io.CharSource;
import com.google.common.io.Files;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PartOne {
    public static void main(String[] args) throws Exception {
        CharSource input = Files.asCharSource(new File(args.length > 0 ? args[0] : "input.txt"), StandardCharsets.UTF_8);
        List<Node> nodes = Node.parseAll(input);
        int numViablePairs = 0;
        for (Node a : nodes) {
            for (Node b : nodes) {
                if (a.canMoveDataTo(b)) {
                    numViablePairs++;
                }
            }
        }
        System.out.format("%d viable pairs (out of %d nodes)%n", numViablePairs, nodes.size());
    }
}
