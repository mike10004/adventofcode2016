package aoc2016day11;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

public class NodeDepthFirstAgent extends Agent {

    public NodeDepthFirstAgent(int maxMoves, int minMoves) {
        super(maxMoves);
        checkArgument(minMoves >= 0, "minMoves >= 0");
    }

    @Override
    public Optional<List<Building>> play(Building start) {
        return Optional.ofNullable(play(Node.root(start)));
    }

    private List<Building> play(Node from) {
        List<Building> path = from.path();
        if (from.label.isWin()) {
            Collections.reverse(path);
            return path;
        }
        if (from.level + 1 > maxMoves) {
            return null;
        }
        List<Building> next = from.label.findValidMovesExcept(path).collect(Collectors.toList());
        for (Building child : next) {
            List<Building> result = play(new Node(child, from));
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
