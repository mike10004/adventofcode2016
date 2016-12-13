package aoc2016day11;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

enum Element {
    hydrogen("H"), lithium("L"), plutonium("P"), promethium("X"), ruthenium("R"), strontium("S"), thulium("T");
    public static List<Element> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
    public final String symbol;

    Element(String symbol) {
        this.symbol = Objects.requireNonNull(symbol);
        Args.check(!symbol.isEmpty(), "must be nonempty");
    }

    public static Element fromSymbol(String symbol) {
        for (Element element : VALUES) {
            if (element.symbol.equals(symbol)) {
                return element;
            }
        }
        throw new IllegalArgumentException(symbol);
    }
}
