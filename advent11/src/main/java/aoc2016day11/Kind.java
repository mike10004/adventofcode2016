package aoc2016day11;

/**
 * Created by mike on 12/11/16.
 */
enum Kind {
    generator, microchip;
    public final String symbol;

    Kind() {
        symbol = String.valueOf(Character.toUpperCase(name().charAt(0)));
    }
}
