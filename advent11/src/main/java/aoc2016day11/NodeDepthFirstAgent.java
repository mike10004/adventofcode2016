package aoc2016day11;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class NodeDepthFirstAgent extends Agent {

    public NodeDepthFirstAgent(int maxMoves) {
        super(maxMoves);
    }

    @Override
    public Optional<List<Building>> play(Building start) {
        return Optional.ofNullable(play(Node.root(start), maxMoves));
    }

    private static List<Building> play(Node from, int maxMoves) {
        List<Building> path = from.path();
        if (from.label.isWin()) {
            Collections.reverse(path);
            return path;
        }
        if (from.level >= maxMoves) {
            return null;
        }
        Optional<List<Building>> search = from.label.findValidMovesExcept(path)
                .map(b -> new Node(b, from))
                .map(n -> play(n, maxMoves))
                .filter(Objects::nonNull).findFirst();
        return search.orElse(null);
    }
}
