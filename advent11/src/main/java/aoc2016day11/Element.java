package aoc2016day11;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by mike on 12/11/16.
 */
enum Element {
    hydrogen, lithium, promethium, ruthenium, strontium, thulium;
    public static List<Element> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
    public final String symbol;

    Element() {
        symbol = String.valueOf(Character.toUpperCase(name().charAt(0)));
    }
}
