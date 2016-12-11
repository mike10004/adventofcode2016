package aoc2016day11;

public enum Direction {
    UP, DOWN;

    public int offset() {
        return this == UP ? 1 : -1;
    }
}
