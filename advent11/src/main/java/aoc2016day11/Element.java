package aoc2016day11;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

enum Element {
    hydrogen, lithium, plutonium, promethium, ruthenium, strontium, thulium;
    public static List<Element> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
    public final String symbol;

    Element() {
        symbol = String.valueOf(Character.toUpperCase(name().charAt(0)));
    }
}
