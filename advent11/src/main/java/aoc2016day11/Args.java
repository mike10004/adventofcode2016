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

    public static <T> T checkNotNull(T thing) throws NullPointerException {
        if (thing == null) {
            throw new NullPointerException();
        }
        return thing;
    }

    public static <T> T checkNotNull(T thing, Object message) throws NullPointerException {
        if (thing == null) {
            throw new NullPointerException(String.valueOf(message));
        }
        return thing;
    }

    public static void checkState(boolean condition) {
        if (!condition) {
            throw new IllegalStateException();
        }
    }

    public static void checkState(boolean condition, Object message) {
        if (!condition) {
            throw new IllegalStateException(String.valueOf(message));
        }
    }
}
