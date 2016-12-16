package aoc2016day11;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DepthFirstAgent extends Agent {

    public DepthFirstAgent(int maxMoves) {
        super(maxMoves);
    }

    public Optional<List<Building>> play(Building building) {
        List<Building> winningPath = playDepthFirst(building, Collections.emptyList(), 0);
        return Optional.ofNullable(winningPath);
    }

    private @Nullable
    List<Building> playDepthFirst(Building building, List<Building> path0, long attemptCounter) {
        maybePrintAttempts(attemptCounter);
        final List<Building> path = append(path0, building);
        if (building.isWin()) {
            return path;
        }
        if (building.numMoves >= maxMoves) {
            return null;
        }
        Optional<List<Building>> winnerOpt = building.findValidMovesExcept(path)
                .map(move -> playDepthFirst(move, path, attemptCounter + 1))
                .filter(Objects::nonNull).findFirst();
        return winnerOpt.orElse(null);
    }

    public static void main(String[] args) {
        Building b =       Buildings.createExampleBuilding();
        attempt(new DepthFirstAgent(12), b);
    }
}
