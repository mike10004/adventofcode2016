package aoc2016day11;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import static aoc2016day11.Element.*;
import static aoc2016day11.Item.generator;
import static aoc2016day11.Item.microchip;

public class Agent {

    private final int maxMoves;
    private final Set<Integer> pathLengths;
    private long attemptCounter = 0;

    public Agent(int maxMoves) {
        this.maxMoves = maxMoves;
        pathLengths = new HashSet<>();
    }

    /**
     * Play until you get a win, or return null if winning is impossible.
     *
     * @param building
     * @return the path that won; absent if no path won in this agent's max moves
     */
    public Optional<List<Building>> play(Building building) {
        List<Building> winningPath = play(building, Collections.emptyList());
        return Optional.ofNullable(winningPath);
    }

    private static <E> List<E> append(List<E> previous, E tail) {
        List<E> next = new ArrayList<>(previous.size() + 1);
        next.addAll(previous);
        next.add(tail);
        return Collections.unmodifiableList(next);
    }

    private static final boolean debug = true;

    private void maybePrintAttempts(int period) {
        if (debug && attemptCounter % period == 0) {
            System.out.format("%d attempts so far%n", attemptCounter);
        }
    }

    private @Nullable List<Building> play(Building building, List<Building> path0) {
        attemptCounter++;
        maybePrintAttempts(1000000);
        final List<Building> path = append(path0, building);
        if (building.isWin()) {
            return path;
        }
        if (building.numMoves >= maxMoves) {
            return null;
        }
        List<Building> nextMoves = building.findValidMovesExcept(path).collect(Collectors.toList());
        for (Building move : nextMoves) {
            List<Building> winner = play(move, path);
            if (winner != null) {
                return winner;
            }
        }
//        System.out.format("examined paths of length %d%n", path0.size());
        return null;
    }

    public static void main(String[] args) throws Exception {
        /*
        The first floor contains
            a thulium generator,
            a thulium-compatible microchip,
            a plutonium generator, and
            a strontium generator.
        The second floor contains
            a plutonium-compatible microchip and
            a strontium-compatible microchip.
        The third floor contains
            a promethium generator,
            a promethium-compatible microchip,
            a ruthenium generator, and
            a ruthenium-compatible microchip.
        The fourth floor contains nothing relevant.
         */

        Building building = Building.onFirstFloor(Arrays.asList(
                new Floor(Arrays.asList(generator(thulium),
                        microchip(thulium),
                        generator(plutonium),
                        generator(strontium))),
                new Floor(Arrays.asList(microchip(plutonium), microchip(strontium))),
                new Floor(Arrays.asList(generator(promethium), microchip(promethium),
                        generator(ruthenium), microchip(ruthenium))),
                new Floor(Collections.emptyList())
        ));
        int maxMoves = 32;
        Agent agent = new Agent(maxMoves);
        Optional<List<Building>> strategy = agent.play(building);
        if (strategy.isPresent()) {
            List<Building> path = strategy.get();
            Building.dump(path, System.out);
            System.out.format("%d moves to win%n", Building.count(path));
        } else {
            System.out.format("no wins in max %d moves%n", maxMoves);
        }
    }
}
