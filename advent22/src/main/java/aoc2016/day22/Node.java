package aoc2016.day22;

import com.google.common.io.CharSource;
import com.google.common.io.LineProcessor;

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
    public int used;
    public int available;

    public Node(int x, int y, int used, int available) {
        this.x = x;
        this.y = y;
        this.used = used;
        this.available = available;
    }

    private static final Pattern dfPattern =
            Pattern.compile("/dev/grid/node-x(\\d+)-y(\\d+)\\s+\\d+T\\s+(\\d+)T\\s+(\\d+)T\\s+\\S+\\s*");

    public static List<Node> parseAll(CharSource source) throws IOException {
        return source.readLines().stream()
                .map(Node::parse)
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static @Nullable Node parse(String line) {
        // /dev/grid/node-x0-y0     93T   68T    25T   73%
        Matcher m = dfPattern.matcher(line);
        if (m.find()) {
            int x = Integer.parseInt(m.group(1)), y = Integer.parseInt(m.group(2));
            int used = Integer.parseInt(m.group(3)), available = Integer.parseInt(m.group(4));
            return new Node(x, y, used, available);
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

}