package aoc2016.day24;

import com.google.common.primitives.Chars;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

public class Duct {

    public final char content;

    public Duct(char content) {
        this.content = content;
        checkArgument(content == '#' || content == '.' || (content >= '0' && content <= '9'), "content: %s", content);
    }

    public static String toString(Stream<Duct> ducts) {
        return new String(Chars.toArray(ducts.map(d -> d.content).collect(Collectors.toList())));
    }

    public boolean isWall() {
        return content == '#';
    }

    public boolean isNumbered() {
        return !isWall() && content != '.';
    }

    @Override
    public String toString() {
        return String.valueOf(content);
    }

    public static class Passage {
        public final Duct from, to;
        public final int weight;

        public Passage(Duct from, Duct to) {
            this(from, to, 1);
        }

        public Passage(Duct from, Duct to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }

        public boolean allows(char content) {
            return from.content == content || to.content == content;
        }

        public String toString() {
            return String.format("(%s, %s)", from, to);
        }
    }
}
