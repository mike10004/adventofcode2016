package aoc2016.day22;

import com.google.common.io.CharSource;
import com.google.common.io.LineProcessor;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class Node {

    public final int x, y;
    public final int used;
    public final int available;
    public final boolean payload;
    public final Point position;

    public Node(int x, int y, int used, int available, boolean payload) {
        this.x = x;
        this.y = y;
        this.position = new Point(x, y);
        this.used = used;
        this.available = available;
        this.payload = payload;
    }

    private static final Pattern dfPattern =
            Pattern.compile("/dev/grid/node-(?:x)?(\\d+)-(?:y)?(\\d+)\\s+\\d+T?\\s+(\\d+)T?\\s+(\\d+)T?(?:\\s+\\S+)?\\s*");

    public static List<Node> parseAll(CharSource source, int payloadX, int payloadY) throws IOException {
        return source.readLines().stream()
                .map(line -> parse(line, payloadX, payloadY))
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static @Nullable Node parse(String line, int payloadX, int payloadY) {
        // /dev/grid/node-x0-y0     93T   68T    25T   73%
        Matcher m = dfPattern.matcher(line);
        if (m.find()) {
            int x = Integer.parseInt(m.group(1)), y = Integer.parseInt(m.group(2));
            int used = Integer.parseInt(m.group(3)), available = Integer.parseInt(m.group(4));
            return new Node(x, y, used, available, x == payloadX && y == payloadY);
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (x != node.x) return false;
        if (y != node.y) return false;
        if (used != node.used) return false;
        return available == node.available;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + used;
        result = 31 * result + available;
        return result;
    }

    public boolean isSameLocation(Node other) {
        return x == other.x && y == other.y;
    }

    public boolean canMoveDataTo(Node other) {
        return !isSameLocation(other)
                && used > 0
                && other.available >= used;
    }

    public boolean isAdjacent(Node other) {
        return (x == other.x && Math.abs(y - other.y) == 1)
                || (y == other.y && Math.abs(x - other.x) == 1);
    }

    public String encode() {
        return String.format("%s%2d/%2d%s", payload ? '[' : ' ', used, used + available, payload ? ']' : ' ');
    }

    @Override
    public String toString() {
        return String.format("(%d, %d) %d/%d", x, y, used, used + available);
    }
}