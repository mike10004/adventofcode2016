package aoc2016day11;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

enum Element {
    P, X, R, S, T;
    public static List<Element> VALUES = ImmutableList.copyOf(values());
    public final String symbol;

    Element() {
        this.symbol = name();
    }

    public static Element fromSymbol(String symbol) {
        return valueOf(symbol);
    }
}
