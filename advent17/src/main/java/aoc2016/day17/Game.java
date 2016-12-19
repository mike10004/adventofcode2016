package aoc2016.day17;

import com.google.common.collect.TreeTraverser;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.primitives.UnsignedBytes;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class Game {

    public final String passcode;
    public final int gridWidth, gridHeight;
    public final com.google.common.hash.HashFunction algorithm;
    private final Charset charset = StandardCharsets.US_ASCII;

    public Game(String passcode, int gridWidth, int gridHeight, HashFunction algorithm) {
        this.passcode = checkNotNull(passcode);
        this.algorithm = checkNotNull(algorithm);
        this.gridHeight = gridHeight;
        this.gridWidth = gridWidth;
    }

    public Game(String passcode) {
        this(passcode, 4, 4, Hashing.md5());
    }

    public interface SearchCallback {
        void won(State winner);
    }

    public static class LongestPathCallback implements SearchCallback {
        private State actual;

        @Override
        public void won(State winner) {
            if (actual == null || winner.trail.length > actual.trail.length) {
                actual = winner;
            }
        }
        public Optional<State> getState() {
            return Optional.ofNullable(actual);
        }
    }

    public Optional<State> findLongestPath(int maxDepth) {
        LongestPathCallback callback = new LongestPathCallback();
        search(maxDepth, callback);
        return callback.getState();
    }

    public enum SearchOrder {
        BREADTH_FIRST, PREORDER, POSTORDER;
        public <E> Iterable<E> iterate(TreeTraverser<E> tree, E root) {
            switch (this) {
                case BREADTH_FIRST: return tree.breadthFirstTraversal(root);
                case PREORDER: return tree.preOrderTraversal(root);
                case POSTORDER: return tree.postOrderTraversal(root);
                default:
                    throw new IllegalStateException(this.toString());
            }
        }
    }

    public void search(int maxDepth, SearchCallback callback) {
        search(maxDepth, callback, SearchOrder.PREORDER);
    }

    public void search(int maxDepth, SearchCallback callback, SearchOrder searchOrder) {
        TreeTraverser<State> traverser = treeTraverser(maxDepth);
        Iterable<State> states = searchOrder.iterate(traverser, root());
        for (State state : states) {
            if (state.isWin()) {
                callback.won(state);
            }
        }
    }

    public State play(int maxDepth) {
        TreeTraverser<State> traverser = treeTraverser(maxDepth);
        Iterable<State> bfs = traverser.breadthFirstTraversal(root());
        for (State state : bfs) {
            if (state.isWin()) {
                return state;
            }
        }
        return null;
    }

    State root() {
        return new State(null, '\0');
    }

    State state(State previous, char direction) {
        return new State(previous, direction);
    }

    private TreeTraverser<State> treeTraverser(final int maxDepth) {
        return new TreeTraverser<State>() {
            @Override
            public Iterable<State> children(State root) {
                if (root.level >= maxDepth || root.isWin()) {
                    return Collections.emptyList();
                } else {
                    return Arrays.asList(root.findNext());
                }
            }
        };
    }

    @SuppressWarnings("WeakerAccess")
    public class State {
        public final int x, y;
        public final State previous;
        public final char direction;
        public final char[] trail;
        public final int level;
        public State(State previous, char direction) {
            this.previous = previous;
            this.direction = direction;
            if (previous != null) {
                level = previous.level + 1;
                x = previous.x + shiftX(direction);
                y = previous.y + shiftY(direction);
                trail = new char[previous.trail.length + 1];
                System.arraycopy(previous.trail, 0, trail, 0, previous.trail.length);
                trail[trail.length - 1] = direction;
            } else {
                level = 0;
                x = 1;
                y = 1;
                trail = new char[0];
            }
        }

        public String concatTrail() {
            return new String(trail);
        }

        public boolean isWin() {
            return x == gridWidth && y == gridWidth;
        }

        public State[] findNext() {
            char[] open = findOpen();
            State[] states = new State[open.length];
            for (int i = 0; i < open.length; i++) {
                states[i] = move(open[i]);
            }
            return states;
        }

        public State move(char direction) {
            return new State(this, direction);
        }

        protected String computeHash() {
            Hasher h = algorithm.newHasher(passcode.length() + trail.length);
            h.putString(passcode, charset);
            for (int i = 0; i < trail.length; i++) {
                h.putByte(UnsignedBytes.checkedCast(trail[i]));
            }
            HashCode code = h.hash();
            return code.toString();
        }

        public char[] findOpen() {
            String code = computeHash();
            String codeStr = code.substring(0, legend.length);
            char[] buffer = new char[legend.length];
            int nValid = 0;
            for (int i = 0; i < legend.length; i++) {
                char direction = legend[i];
                boolean inBounds = inBounds(direction);
                if (inBounds) {
                    char hashChar = codeStr.charAt(i);
                    boolean open = isHashCharMeansOpen(hashChar);
                    if (open) {
                        buffer[nValid++] = direction;
                    }
                }
            }
            char[] open = Arrays.copyOf(buffer, nValid);
            return open;
        }

        private int countOpenAndInBounds(String codeStr) {
            int numOpen = 0;
            for (int i = 0; i < codeStr.length(); i++) {
                char hashChar = codeStr.charAt(i);
                char direction = legend[i];
                if (isHashCharMeansOpen(hashChar) && inBounds(direction)) {
                    numOpen++;
                }
            }
            return numOpen;
        }

        private int shiftY(char direction) {
            int offset = 0;
            if (direction == 'U') {
                offset = -1;
            } else if (direction == 'D') {
                offset  = 1;
            }
            return y + offset;
        }

        private int shiftX(char direction) {
            int offset = 0;
            if (direction == 'L') {
                offset = -1;
            } else if (direction == 'R') {
                offset  = 1;
            }
            return x + offset;
        }

        private boolean inBounds(char direction) {
            int newX = shiftX(direction), newY = shiftY(direction);
            return Game.this.inBounds(newX, newY);
        }

        @Override
        public String toString() {
            return "State{" +
                    "x=" + x +
                    ", y=" + y +
                    ", trail=" + concatTrail() +
                    ", level=" + level +
                    '}';
        }
    }

    private boolean inBounds(int x, int y) {
        return x >= 1 && x <= gridWidth && y >= 1 && y <= gridHeight;
    }

    private static boolean isHashCharMeansOpen(char ch) {
        ch = Character.toLowerCase(ch);
        return ch >= 'b' && ch <= 'f';
    }

    private static final char[] legend = {'U', 'D', 'L', 'R'};

    public static void main(String[] args) {
        String passcode = "bwnlcvfs";
        Game game = new Game(passcode);
        State win = game.play(128);
        if (win == null) {
            System.err.println("did not win game");
            System.exit(2);
        }
        System.out.format("shortest path%s%n", win);
        Optional<State> longest = game.findLongestPath(1024 * 1024);
        if (longest.isPresent()) {
            System.out.format("longest path: %d moves%n", longest.get().trail.length);
        } else {
            System.err.println("no longest path found");
            System.exit(2);
        }

    }
}
