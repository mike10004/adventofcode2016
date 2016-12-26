package aoc2016.day24;

import aoc2016.day24.Duct.Passage;
import com.google.common.io.CharSource;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.WeightedGraph;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class BlueprintTest {
    @org.junit.Test
    public void neighborsOf() throws Exception {
        Blueprint b = Blueprint.parse(CharSource.wrap(EXAMPLE));
        String neighbors = Duct.toString(b.neighborsOf(1, 1));
        assertEquals("neighbors", "##..", neighbors);
        assertEquals("neighbors", Arrays.asList(
                b.ducts[0][1],
                b.ducts[1][0],
                b.ducts[1][2],
                b.ducts[2][1]
        ), b.neighborsOf(1, 1).collect(Collectors.toList()));
    }

    @org.junit.Test
    public void parse() throws Exception {
        Blueprint b = Blueprint.parse(CharSource.wrap(EXAMPLE));
        assertEquals(5, b.ducts.length);
        assertEquals(11, b.ducts[0].length);
    }

    @Test
    public void makePassageGraph_example() throws Exception {
        Graph<Duct, Duct.Passage> g = Blueprint.parse(CharSource.wrap(EXAMPLE)).makePassageGraph();
        assertEquals("num vertices", 20, g.vertexSet().size());
    }

    private static final String EXAMPLE =   "###########\n" +
                                            "#0.1.....2#\n" +
                                            "#.#######.#\n" +
                                            "#4.......3#\n" +
                                            "###########";

    @Test
    public void makePassageGraph() throws Exception {
        String simple = "#####\n" +
                        "#0.1#\n" +
                        "#####";
        WeightedGraph<Duct, Passage> g = Blueprint.parse(CharSource.wrap(simple)).makePassageGraph();
        assertEquals("num vertices", 3, g.vertexSet().size());
        assertEquals("num edges", 2, g.edgeSet().size());
        assertEquals(1L, g.edgeSet().stream().filter(p -> p.allows('.') && p.allows('0')).count());
        assertEquals(1L, g.edgeSet().stream().filter(p -> p.allows('.') && p.allows('1')).count());
    }

    @Test
    public void makePassageGraph2() throws Exception {
        String simple = "#####\n" +
                        "#0.1#\n" +
                        "#.2.#\n" +
                        "#####";
        WeightedGraph<Duct, Passage> g = Blueprint.parse(CharSource.wrap(simple)).makePassageGraph();
        assertEquals("num vertices", 6, g.vertexSet().size());
        Set<Passage> edges = g.edgeSet();
        System.out.format("edges: %s%n", edges);
        assertEquals("num edges", 7, edges.size());
        assertEquals(2L, g.edgeSet().stream().filter(p -> p.allows('.') && p.allows('0')).count());
        assertEquals(2L, g.edgeSet().stream().filter(p -> p.allows('.') && p.allows('1')).count());
        assertEquals(3L, g.edgeSet().stream().filter(p -> p.allows('.') && p.allows('2')).count());
    }

    @Test
    public void findShortest() throws Exception {
        List<GraphPath<Duct, Duct.Passage>> paths = Blueprint.parse(CharSource.wrap(EXAMPLE)).findShortestPathToNumberedDucts();
        int length = Blueprint.computeTotalLength(paths);
        assertEquals("path length", 14, length);
    }
}