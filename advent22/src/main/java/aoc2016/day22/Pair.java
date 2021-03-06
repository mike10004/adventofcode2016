package aoc2016.day22;

import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public class Pair {
    public final Node src;
    public final Node dst;

    public Pair(Node src, Node dst) {
        this.src = src;
        this.dst = dst;
        checkArgument(getDirectionY() == 0 || getDirectionX() == 0, "diagonal move: %s -> %s", src, dst);
        checkArgument(getDirectionX() != 0 || getDirectionY() != 0, "no movement: %s -> %s", src, dst);
    }

    public static Pair of(Node src, Node dst) {
        return new Pair(src, dst);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d) -> (%d, %d)", src.x, src.y, dst.x, dst.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair pair = (Pair) o;

        if (!src.equals(pair.src)) return false;
        return dst.equals(pair.dst);
    }

    @Override
    public int hashCode() {
        int result = src.hashCode();
        result = 31 * result + dst.hashCode();
        return result;
    }

    public Pair move() {
        checkArgument(src.used > 0, "can't move from node %s with used == 0", src);
        checkArgument(dst.available >= src.used, "not enough free space in %s", dst);
        Node nsrc = new Node(src.x, src.y, 0, src.used + src.available, false);
        Node ndst = new Node(dst.x, dst.y, dst.used + src.used, dst.available - src.used, src.payload);
        return new Pair(nsrc, ndst);
    }

    public Stream<Node> stream() {
        return Stream.of(src, dst);
    }

    public boolean isReverse(Pair cause) {
        return cause != null
                && src.isSameLocation(cause.dst)
                && dst.isSameLocation(cause.src);
    }

    public boolean isParallel(int directionX, int directionY) {
        int xDelta = Math.abs(src.x - dst.x), yDelta = Math.abs(src.y - dst.y);
        return (Math.abs(directionX) + xDelta == 0) || (Math.abs(directionY) + yDelta == 0);
    }

    public int getDirectionX() {
        return dst.x - src.x;
    }

    public int getDirectionY() {
        return dst.y - src.y;
    }

    public boolean isOppositeDirection(int directionX, int directionY) {
        if (directionX == 0) {
            return getDirectionY() / Math.abs(getDirectionY()) == -directionY / Math.abs(directionY);
        }
        if (directionY == 0) {
            return getDirectionX() / Math.abs(getDirectionX()) == -directionX / Math.abs(directionX);
        }
        throw new IllegalArgumentException("x-direction or y-direction must be 0");
    }
}
