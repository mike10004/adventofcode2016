package aoc2016.day22;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeTraverser;
import com.google.common.math.IntMath;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.SimpleDirectedGraph;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.PrintStream;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public class Grid {

    public final Pair cause;
    final DirectedGraph<Node, Pair> graph;
    public final int level;
    public final Point payload;
    private MoveListener moveListener;

    public Grid(Pair cause, int level, DirectedGraph<Node, Pair> graph, int payloadX, int payloadY) {
        this(cause, level, graph, payloadX, payloadY, null);
    }

    public Grid(Pair cause, int level, DirectedGraph<Node, Pair> graph, int payloadX, int payloadY, MoveListener moveListener) {
        this(cause, level, graph, new Point(payloadX, payloadY), moveListener);
    }

    public interface MoveListener {
        void performed(Pair move);
    }

    public Grid(Pair cause, int level, DirectedGraph<Node, Pair> graph, Point payload) {
        this(cause, level, graph, payload, null);
    }

    public Grid(Pair cause, int level, DirectedGraph<Node, Pair> graph, Point payload, MoveListener moveListener) {
        this.graph = graph;
        this.level = level;
        this.cause = cause;
        this.payload = payload;
        this.moveListener = moveListener;
    }

    public void setMoveListener(MoveListener moveListener) {
        this.moveListener = moveListener;
    }

    public static int findMaxX(Stream<Node> nodes) {
        return nodes.map(n -> n.x).max(Integer::compareTo).orElse(-1);
    }

    private static final EdgeFactory<Node, Pair> ef = Pair::new;

    public boolean isWin(int targetX, int targetY) {
        return payload.x == targetX && payload.y == targetY;
    }

    public Optional<Node> findNodeByPosition(int x, int y) {
        return findNode(n -> n.isAtPosition(x, y));
    }

    public Optional<Node> findNode(Predicate<? super Node> predicate) {
        return orderedNodeList().stream().filter(predicate).findFirst();
    }

    public Optional<Node> findEmptyNode() {
        return findNode(n -> n.used == 0);
    }

    public Node findEmptyNodeOrDie() {
        return findEmptyNode().orElseThrow(() -> new IllegalStateException("no empty node in grid"));
    }

    public static Grid make(List<Node> nodes) {

        SimpleDirectedGraph<Node, Pair> g = new SimpleDirectedGraph<>(ef);
        int payloadX = -1, payloadY = -1;
        for (Node node : nodes) {
            g.addVertex(node);
            if (node.payload) {
                payloadX = node.x;
                payloadY = node.y;
            }
        }
        for (Node a : nodes) {
            addPossibleMoves(g, a, nodes, false);
        }
        return new Grid(null, 0, g, payloadX, payloadY);
    }

    private static void addPossibleMoves(SimpleDirectedGraph<Node, Pair> g, Node a, Iterable<Node> nodes, boolean symmetric) {
        for (Node b : nodes) {
            if (a.isAdjacent(b) && a.canMoveDataTo(b)) {
                g.addEdge(a, b);
            }
            if (symmetric && b.isAdjacent(a) && b.canMoveDataTo(a)) {
                g.addEdge(b, a);
            }
        }
    }

    public Stream<Pair> moves() {
        Set<Node> nodes = graph.vertexSet();
        List<Pair> pairs = new ArrayList<>(nodes.size() * 4);
        for (Node n : nodes) {
            Set<Pair> moves = graph.outgoingEdgesOf(n);
            Stream<Pair> productive = moves.stream().filter(p -> !p.isReverse(cause));
            productive.forEach(pairs::add);
        }
        return pairs.stream();
    }

    private SimpleDirectedGraph<Node, Pair> copyGraph() {
        List<Node> gn = new ArrayList<Node>();
        gn.addAll(graph.vertexSet());
        List<Pair> ge = new ArrayList<Pair>();
        ge.addAll(graph.edgeSet());
        SimpleDirectedGraph<Node, Pair> g = new SimpleDirectedGraph<>(ef);
        gn.forEach(g::addVertex);
        ge.forEach(p -> g.addEdge(p.src, p.dst, p));
        return g;
    }

    public Grid move(Pair pair) {
        return move(pair, this.moveListener);
    }

    public Grid move(Pair pair, MoveListener moveListener) {
        SimpleDirectedGraph<Node, Pair> g = copyGraph();
        g.removeVertex(pair.src);
        g.removeVertex(pair.dst);
        Pair altered = pair.move();
        g.addVertex(altered.src);
        g.addVertex(altered.dst);
        Set<Node> nodes = g.vertexSet();
        addPossibleMoves(g, altered.src, nodes, true);
        addPossibleMoves(g, altered.dst, nodes, true);
        Grid grid = new Grid(pair, level + 1, g, altered.dst.payload ? altered.dst.position : payload, moveListener);
        if (moveListener != null) {
            moveListener.performed(pair);
        }
        checkState(grid.findNodeByPosition(pair.src.x, pair.src.y).get().used == 0, "src node in new grid is not empty");
        return grid;
    }

    private static final Ordering<Node> nodeOrdering = Ordering.<Integer>natural()
            .<Node>onResultOf(n -> n.y)
            .compound(Ordering.<Integer>natural().<Node>onResultOf(n -> n.x));

    private ImmutableList<Node> orderedNodeList() {
        Set<Node> set = graph.vertexSet();
        return nodeOrdering.immutableSortedCopy(set);
    }

    private static Function<Node, Stream<Pair>> movesFrom(Collection<Pair> moves) {
        return n -> moves.stream().filter(p -> p.src.equals(n));
    }

    private static final float WALL_THRESHOLD = 0.90f;

    static char represent(Node n, Stream<Pair> moves, List<Pair> path) {
        if (!path.isEmpty() && path.stream().anyMatch(p -> p.src.isSameLocation(n))) {
            return '*';
        }
        if (n.used == 0) {
            return '_';
        }
        if (n.payload) {
            return 'G';
        }
        if (n.proportionUsed() > WALL_THRESHOLD) {
            return '#';
        } else {
            if (moves.count() > 0) {
                return '^';
            } else {
                return '.';
            }
        }

    }

    public String illustrate() {
        return illustrate(moves().collect(Collectors.toList()));
    }

    public String illustrate(Collection<Pair> moves) {
        return illustrate(moves, Collections.emptyList());
    }

    public String trace(List<Pair> path) {
        return illustrate(Collections.emptySet(), path);
    }

    private String illustrate(Collection<Pair> moves, List<Pair> path) {
        List<Node> nodes = orderedNodeList();
        int maxX = Grid.findMaxX(nodes.stream());
        StringBuilder b = new StringBuilder(256);
        int y = 0;
        Function<Node, Stream<Pair>> mf = movesFrom(moves);
        for (int i = 0; i < nodes.size(); i++) {
            if (i % (maxX + 1) == 0) {
                if (i > 0) {
                    b.append('\n');
                }
                if (i + 1 < nodes.size()) {
                    b.append(String.format("%2d ", y));
                }
                y++;
            }
            Node n = nodes.get(i);
            b.append(represent(n, mf.apply(n), path));
            b.append(' ');
        }
        return createXAxisLabels(maxX, "   ") + b.toString();
    }

    static boolean isLeadingZero(int p, int i, int v) {
        // (2, 0, 0) -> true
        // (2, 10, 1) -> false
        if (p == 1 && i == 0) {
            return false;
        }
        return v == 0 && i < IntMath.pow(10, p - 1);
    }

    static String createXAxisLabels(int maxX, String indent) {

        StringBuilder b = new StringBuilder(256);
        for (int p = IntMath.log10(maxX, RoundingMode.CEILING); p >= 1; p--) {
            b.append(indent);
            for (int i = 0; i <= maxX; i++) {
                int q = IntMath.pow(10, p - 1);
                int v = IntMath.divide(i, q, RoundingMode.FLOOR) % 10;
                if (isLeadingZero(p, i, v)) {
                    b.append(' ');
                } else {
                    b.append(v);
                }
                b.append(' ');
            }
            b.append('\n');
        }
        return b.toString();
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        List<Node> nodes = orderedNodeList();
        int maxX = Grid.findMaxX(nodes.stream());
        int y = 0;
        b.append("   ");
        for (int x = 0; x <= maxX; x++) {
            b.append(String.format("%8d  ", x));
        }
        b.append('\n');
        for (int i = 0; i < nodes.size(); i++) {
            if (i % (maxX + 1) == 0) {
                if (i > 0) {
                    b.append('\n');
                }
                if (i + 1 < nodes.size()) {
                    b.append(String.format("%2d ", y));
                }
                y++;
            }
            Node n = nodes.get(i);
            b.append(n.encode()).append(' ');
        }
        return b.toString();
    }

    public void df(PrintStream out) {
        out.println("root@ebhq-gridcenter# df -h\n" +
                "Filesystem              Size  Used  Avail  Use%");
        for (Node n : graph.vertexSet()) {
            out.format("%-24s%4s  %4s  %5s  %.0f%%%n", String.format("/dev/grid/node-x%d-y%d", n.x, n.y), n.used + n.available, n.used, n.available, n.used * 100 / (float)(n.used + n.available));
        }
    }

    public interface FindShortestCallback {
        boolean dequeued(java.util.Queue<Grid> queue, Grid element);
    }

    public @Nullable Grid findShortestWinningStrategy(Point target, final int maxMoves) {
        return findShortestWinningStrategy(target, maxMoves, alwaysTrueCallback);
    }

    private static final FindShortestCallback alwaysTrueCallback = (q, e) -> true;

    public @Nullable Grid findShortestWinningStrategy(Point target, final int maxMoves, FindShortestCallback callback) {
        final java.util.Queue<Grid> queue = new ArrayDeque<>(1024 * 1024);
        queue.add(this);
        while (!queue.isEmpty()) {
            Grid node = queue.remove();
            if (!callback.dequeued(queue, node)) {
                break;
            }
            if (node.isWin(target.x, target.y)) {
                return node;
            }
            if (node.level + 1 <= maxMoves) {
                node.moves().map(node::move).forEach(queue::add);
            }
        }
        return null;
    }
}
