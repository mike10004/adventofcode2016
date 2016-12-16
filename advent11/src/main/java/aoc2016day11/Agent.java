package aoc2016day11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class Agent {

    protected final int maxMoves;
    private boolean verbose;
    private int attemptCountPrintInterval = 1000000;

    public Agent(int maxMoves) {
        this.maxMoves = maxMoves;
        checkArgument(maxMoves >= 0, "maxMoves >= 0");
    }

    /**
     * Play until you get a win, or return empty if winning is impossible.
     *
     * @param start starting position
     * @return the path that won; absent if no path won in this agent's max moves
     */
    public abstract Optional<List<Building>> play(Building start);

    protected static <E> List<E> append(List<E> previous, E tail) {
        List<E> next = new ArrayList<>(previous.size() + 1);
        next.addAll(previous);
        next.add(tail);
        return Collections.unmodifiableList(next);
    }

    public boolean isVerbose() {
        return verbose;
    }

    public Agent toggleVerbose() {
        verbose = !verbose;
        return this;
    }

    public void setAttemptCountPrintInterval(int attemptCountPrintInterval) {
        this.attemptCountPrintInterval = attemptCountPrintInterval;
    }

    @SuppressWarnings("SameParameterValue")
    protected final void maybePrintAttempts(long attempts) {
        if (attempts % attemptCountPrintInterval == 0) {
            System.out.format("%d attempts so far%n", attempts);
        }
    }

    protected static void attempt(Agent agent, Building building) {
        Optional<List<Building>> strategy = agent.play(building);
        if (strategy.isPresent()) {
            List<Building> path = strategy.get();
            Building.dump(path, System.out);
            System.out.format("%d moves to win%n", Building.count(path));
        } else {
            System.out.format("no wins in max %d moves%n", agent.maxMoves);
        }
    }

    protected static class Node {
        public final Building label;
        public @Nullable  final Node parent;
        public final int level;

        public Node(Building label, @Nullable Node parent) {
            this(label, parent, parent == null ? 0 : parent.level + 1);
        }

        private Node(Building label, @Nullable Node parent, int level) {
            this.label = checkNotNull(label);
            this.parent = parent;
            this.level = level;
        }

        public static Node root(Building building) {
            return new Node(building, null, 0);
        }

        public List<Building> path() {
            List<Building> path = new ArrayList<>();
            Node current = this;
            while (current != null) {
                path.add(current.label);
                current = current.parent;
            }
            return path;
        }
    }

    private int currentDepth = -1;

    protected int reachedDepth(int depth) {
        if (currentDepth != depth) {
            depthChanged(depth);
        }
        currentDepth = depth;
        return depth;
    }

    protected void depthChanged(int newDepth) {
        if (isVerbose()) {
            System.out.format("depth changed %d%n", newDepth);
        }
    }

}
