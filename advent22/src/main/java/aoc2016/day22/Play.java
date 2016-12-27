package aoc2016.day22;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharSource;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;

import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;

public class Play {

    public static final CharSource puzzleInput = Resources.asCharSource(PartOne.class.getResource("/input.txt"), StandardCharsets.UTF_8);
    public static final int PUZZLE_INPUT_PAYLOAD_X = 37, PUZZLE_INPUT_PAYLOAD_Y = 0;

    private static final ImmutableSet<String> quitters = ImmutableSet.of("quit", "q", "exit");

    private Grid current;
    private final Point target;
    private final Map<String, Pair> moves;

    public Play(Grid start, int targetX, int targetY) {
        this.current = start;
        this.target = new Point(targetX, targetY);
        moves = new TreeMap<>();
        reload();
    }

    public void perform(Pair move) {
        current = current.move(move);
        reload();
    }

    private void reload() {
        moves.clear();
        AtomicInteger index = new AtomicInteger(1);
        List<Pair> nextMoves = current.moves().collect(Collectors.toList());;
        nextMoves.forEach(p -> moves.put(String.valueOf(index.getAndIncrement()), p));
    }

    private static final Pattern showCommand = Pattern.compile("show\\s+(\\d+)(?:\\D+)?(\\d+)\\D*");

    private boolean isCommand(String line) {
        return quitters.contains(line)
                || showCommand.matcher(line).find();
    }

    private boolean processCommand(String line) {
        if (quitters.contains(line)) {
            return false;
        }
        Matcher m = showCommand.matcher(line);
        if (m.find()) {
            try {
                int x = Integer.parseInt(m.group(1));
                int y = Integer.parseInt(m.group(2));
                Node n = current.findNodeByPosition(x, y).orElse(null);
                if (n == null) {
                    System.err.format("no node at %d, %d%n", x, y);
                } else {
                    System.out.format("(%d, %d) = %s%n", x, y, n.encode());
                }
            } catch (NumberFormatException e) {
                System.err.format("failed to parse input: %s (%s)%n", line, e);
            }
            return true;
        }
        System.err.format("unrecognized command: %s%n", line);
        return true;
    }

    public LineProcessor<Grid> asLineProcessor() {
        return new LineProcessor<Grid>() {
            @Override
            public boolean processLine(String line) throws IOException {
                line = line.toLowerCase().trim();
                if (isCommand(line)) {
                    return processCommand(line);
                }
                Pair move = moves.get(line);
                if (move == null) {
                    System.err.format("invalid move: %s%n", line);
                    return true;
                } else {
                    perform(move);
                    boolean win = current.isWin(target.x, target.y);
                    if (!win) {
                        present(System.out);
                    }
                    if (!win && moves.isEmpty()) {
                        System.out.println("no more legal moves in grid:");
                        current.df(System.out);
                    }
                    return !win && !moves.isEmpty();
                }

            }

            @Override
            public Grid getResult() {
                return current;
            }
        };
    }

    private void present(PrintStream out) {
        out.println(current.illustrate(moves.values()));
        System.out.println();
        for (String index : moves.keySet()) {
            Pair move = moves.get(index);
            System.out.format("%2s %s%n", index, move);
        }
        System.out.println();
        System.out.print("Enter move: ");
    }

    static class Grids {
        private Grids() {}

        public static Grid puzzleInput() {
            try {
                return Grid.make(Node.parseAll(puzzleInput, PUZZLE_INPUT_PAYLOAD_X, PUZZLE_INPUT_PAYLOAD_Y));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static Grid small() throws IOException {
            String input = "Filesystem            Size  Used  Avail  Use%\n" +
                    "/dev/grid/node-x0-y0   10T    8T     2T   80%\n" +
                    "/dev/grid/node-x0-y1   11T    6T     5T   54%\n" +
                    "/dev/grid/node-x0-y2   32T   28T     4T   87%\n" +
                    "/dev/grid/node-x1-y0    9T    7T     2T   77%\n" +
                    "/dev/grid/node-x1-y1    8T    0T     8T    0%\n" +
                    "/dev/grid/node-x1-y2   11T    7T     4T   63%\n" +
                    "/dev/grid/node-x2-y0   10T    6T     4T   60%\n" +
                    "/dev/grid/node-x2-y1    9T    8T     1T   88%\n" +
                    "/dev/grid/node-x2-y2    9T    6T     3T   66%";
            Grid root = Grid.make(Node.parseAll(CharSource.wrap(input), 2, 0));
            return root;
        }

        public static Grid shouldHaveSome() {
            List<Node> nodes = Arrays.asList(
                    new Node(0, 0, 8, 2, false),
                    new Node(1, 0, 0, 9, false),
                    new Node(2, 0, 6, 4, true),
                    new Node(0, 1, 6, 5, false),
                    new Node(1, 1, 7, 1, false),
                    new Node(2, 1, 8, 1, false),
                    new Node(0, 2, 28, 4, false),
                    new Node(1, 2, 7, 4, false),
                    new Node(2, 2, 6, 3, false)
            );
            return Grid.make(nodes);
        }
    }

    public static void main(String[] args) throws Exception {
        Point t = new Point(0, 0);
        Grid start = Grids.puzzleInput();
        Play play = new Play(start, t.x, t.y);
        play.present(System.out);
        CharSource stdin = new CharSource() {
            @Override
            public Reader openStream() throws IOException {
                return new InputStreamReader(System.in, StandardCharsets.UTF_8);
            }
        };
        Grid last = stdin.readLines(play.asLineProcessor());
        System.out.format("%nYou %s%n", last.isWin(t.x, t.y) ? "won" : "lost");
    }

}
