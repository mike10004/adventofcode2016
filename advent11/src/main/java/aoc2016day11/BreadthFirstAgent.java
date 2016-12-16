package aoc2016day11;

public abstract class BreadthFirstAgent extends Agent {

    public BreadthFirstAgent(int maxMoves) {
        super(maxMoves);
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
