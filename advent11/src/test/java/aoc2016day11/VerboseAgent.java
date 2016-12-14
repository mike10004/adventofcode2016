package aoc2016day11;

class VerboseAgent extends Agent {
    public VerboseAgent(int maxMoves) {
        super(maxMoves);
    }

    @Override
    protected void maybePrintAttempts(long attempts) {
        if (attempts % 100000 == 0) {
            System.out.format("%d attempts%n", attempts);
        }
    }

    @Override
    protected void depthChanged(int newDepth) {
        System.out.format("examining at depth %d%n", newDepth);
    }
}
