package aoc2016day11;

enum Kind {
    generator, microchip;
    public final String symbol;

    Kind() {
        symbol = String.valueOf(Character.toUpperCase(name().charAt(0)));
    }

    public Kind other() {
        return this == generator ? microchip : generator;
    }
}
