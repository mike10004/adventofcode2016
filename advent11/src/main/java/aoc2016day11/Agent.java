package aoc2016day11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class Agent {

    protected final int maxMoves;
    private boolean verbose;
    private int attemptCountPrintInterval = 1000000;

    public Agent(int maxMoves) {
        this.maxMoves = maxMoves;
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
}
