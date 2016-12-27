package aoc2016.day22;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.TreeTraverser;
import com.google.common.io.CharSource;
import org.junit.Test;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;
import static org.junit.Assert.*;

public class GridTest {
    @Test
    public void moves() throws Exception {
        List<Node> nodes = Node.parseAll(Play.puzzleInput, 1, 1);
        Grid grid = Grid.make(nodes);
        long numMoves = grid.moves().count();
        System.out.format("%d moves in grid of %d nodes%n", numMoves, nodes.size());
    }

    @Test
    public void test_toString() throws Exception {
        Grid g = Play.Grids.small();
        System.out.println("==============");
        System.out.println(g.toString());
        System.out.println("==============");
    }

    @Test
    public void smallGrid() throws Exception {
        Grid root = Play.Grids.small();
        final int maxMoves = 9;
        Grid win = root.findShortestWinningStrategy(new Point(0, 0), maxMoves);
        assertNotNull(win);
        assertEquals("moves", 7, win.level);
    }

    @Test
    public void moves2() throws Exception {
        Grid g = Play.Grids.shouldHaveSome();
        checkHasMoves(g);
    }

    private void checkHasMoves(Grid g) {
        System.out.println(g);
        List<Pair> moves = g.moves().collect(Collectors.toList());
        System.out.format("%d moves: %s%n", moves.size(), moves);
        assertTrue("moves should > 0", moves.size() > 0);
    }

    @Test
    public void move() {
        Node n1 = new Node(0, 0, 1, 100, false);
        Node n2 = new Node(1, 0, 1, 100, false),
                n3 = new Node(2, 0, 1, 100, false);
        Grid g = Grid.make(Arrays.asList(n1, n2, n3));
        Set<Pair> n3Pre = g.graph.outgoingEdgesOf(n3);
        Grid h = g.move(Pair.of(n1, n2));
        Set<Pair> n3Post = h.graph.outgoingEdgesOf(n3);
        assertEquals("moves for n3 before and after moving n1 data to n2", n3Pre.size(), n3Post.size());
    }

    @Test
    public void afterFirstMovesHasMoves() throws Exception {
        Node n1 = new Node(0, 0, 1, 100, false);
        Node n2 = new Node(1, 0, 1, 100, false),
             n3 = new Node(2, 0, 1, 100, false);
        Grid g = Grid.make(Arrays.asList(
                n1, n2, n3
        ));
        System.out.println(g);
        List<Pair> moves = g.moves().collect(Collectors.toList());
        System.out.format("%d moves: %s%n", moves.size(), moves);
        assertEquals("num moves", 4, moves.size());
        Grid h = g.move(Pair.of(n1, n2));
        System.out.println(h);
        moves = h.moves().collect(Collectors.toList());
        System.out.format("%d moves: %s%n", moves.size(), moves);
        assertEquals("num moves", 2, moves.size());
    }
}