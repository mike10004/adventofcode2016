package aoc2016day11;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.TreeTraverser;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Agent {

    private final int maxMoves;

    public Agent(int maxMoves) {
        this.maxMoves = maxMoves;
    }

    /**
     * Play until you get a win, or return null if winning is impossible.
     *
     * @param building starting position
     * @return the path that won; absent if no path won in this agent's max moves
     */
    public Optional<List<Building>> playDepthFirst(Building building) {
        List<Building> winningPath = playDepthFirst(building, Collections.emptyList(), 0);
        return Optional.ofNullable(winningPath);
    }

    private int currentDepth = -1;

    private int reachedDepth(int depth) {
        if (currentDepth != depth) {
            depthChanged(depth);
        }
        currentDepth = depth;
        return depth;
    }

    public Optional<List<Building>> play(Building building) {
        GameTreeTraverser t = new GameTreeTraverser();
        Iterator<TreeNode<Building>> nodes = t.breadthFirstTraversal(new TreeNode<>(building)).iterator();
        while (nodes.hasNext()) {
            TreeNode<Building> node = nodes.next();
            int level = reachedDepth(node.getLevel());
            if (level > maxMoves) {
                break;
            }
            Building state = node.getUserObject();
            if (state.isWin()) {
                List<Building> path = node.getUserObjectPath();
                return Optional.of(Collections.unmodifiableList(path));
            }
        }
        return Optional.empty();
    }

    private static class GameTreeTraverser extends TreeTraverser<TreeNode<Building>> {

        @Override
        public Iterable<TreeNode<Building>> children(TreeNode<Building> root) {
            final List<Building> strategy = root.getUserObjectPath();
            List<Building> children = root.getUserObject().findValidMovesExcept(strategy).collect(Collectors.toList());
            children.forEach(child -> {
                TreeNode<Building> childNode = new TreeNode<>(child);
                root.add(childNode);
                Args.checkState(childNode.getLevel() > 0, "expected node with level > 0");
            });
            return new FluentIterable<TreeNode<Building>>() {
                @Override
                public Iterator<TreeNode<Building>> iterator() {
                    return root.children();
                }
            };
        }
    }

    private static <E> List<E> append(List<E> previous, E tail) {
        List<E> next = new ArrayList<>(previous.size() + 1);
        next.addAll(previous);
        next.add(tail);
        return Collections.unmodifiableList(next);
    }

    @SuppressWarnings("SameParameterValue")
    protected void maybePrintAttempts(long attempts) {
    }

    protected void depthChanged(int newDepth) {

    }

    private @Nullable List<Building> playDepthFirst(Building building, List<Building> path0, long attemptCounter) {
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

        Building building = Play.createPuzzleInputBuilding();
        int maxMoves = 32;
        Agent agent = new Agent(maxMoves) {
            @Override
            protected void depthChanged(int newDepth) {
                System.out.format("examining strategies at depth %d%n", newDepth);
            }
        };
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
