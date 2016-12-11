package aoc2016day11;

public class Args {

    @SuppressWarnings("SameParameterValue")
    public static void check(boolean condition, Object message) {
        if (!condition) {
            throw new IllegalArgumentException(String.valueOf(message));
        }
    }

    public static void check(boolean condition, String template, Object...args) {
        if (!condition) {
            throw new IllegalArgumentException(String.format(template, args));
        }
    }

}
