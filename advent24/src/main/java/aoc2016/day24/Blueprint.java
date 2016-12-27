package aoc2016.day24;

import com.google.common.base.Function;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.io.CharSource;
import com.google.common.math.IntMath;
import com.google.common.primitives.Ints;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;

public class Blueprint {

    final Duct[][] ducts;
    public final int numRows, numCols;
    private final String source;

    public Blueprint(Duct[][] ducts, String source) {
        this.ducts = ducts;
        numRows = ducts.length;
        this.source = source;
        numCols = ducts.length > 0 ? ducts[0].length : 0;
    }

    private static class Cell {
        public final int row, col;

        private Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    public Stream<Duct> neighborsOf(int row, int col) {
        return Stream.of(new Cell(row - 1, col), new Cell(row, col - 1), new Cell(row, col + 1), new Cell(row + 1, col))
                .filter(c -> c.row >= 0 && c.col < numCols && c.row < numRows && c.col >= 0)
                .map(c -> ducts[c.row][c.col]);
    }

    public static Blueprint parse(CharSource source) throws IOException {
        List<String> rows = source.readLines();
        if (!rows.isEmpty()) {
            StringBuilder sb = new StringBuilder(Ints.checkedCast(source.lengthIfKnown().or(1024L)));
            Duct[][] ducts = new Duct[rows.size()][rows.get(0).length()];
            for (int r = 0; r < rows.size(); r++) {
                String row = rows.get(r);
                sb.append(row).append('\n');
                for (int c = 0; c < row.length(); c++) {
                    ducts[r][c] = new Duct(row.charAt(c));
                }
            }
            return new Blueprint(ducts, sb.toString());
        } else {
            return new Blueprint(empty, "{}");
        }
    }

    private static final Duct[][] empty = {};

    @Override
    public String toString() {
        return source;
    }

    public Iterator<Duct> all() {
        return new AbstractIterator<Duct>() {

            private int c = 0;

            @Override
            protected Duct computeNext() {
                if (c >= (numRows * numCols)) {
                    endOfData();
                    return null;
                } else {
                    Duct d = ducts[c / numCols][c % numCols];
                    c++;
                    return d;
                }
            }
        };
    }

    public WeightedGraph<Duct, Duct.Passage> makePassageGraph() {
        EdgeFactory<Duct, Duct.Passage> ef = (d1, d2) -> new Duct.Passage(d1, d2);
        SimpleWeightedGraph<Duct, Duct.Passage>  g = new SimpleWeightedGraph<>(ef);
        Iterator<Duct> all = all();
        while (all.hasNext()) {
            Duct d = all.next();
            if (!d.isWall()) {
                g.addVertex(d);
            }
        }
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                Duct d = ducts[r][c];
                if (!d.isWall()) {
                    List<Duct> destinations = neighborsOf(r, c).filter(t -> !t.isWall()).collect(Collectors.toList());
                    destinations.forEach(t -> g.addEdge(d, t));
                }
            }
        }
        return g;
    }

    static int computeTotalLength(Iterable<GraphPath<Duct, Duct.Passage>> paths) {
        int total = 0;
        for (GraphPath<?, ?> path : paths) {
            total = IntMath.checkedAdd(path.getLength(), total);
        }
        return total;
    }

    public enum FinalPosition {
        WHEREVER,
        RETURN_TO_START
    }

    public List<GraphPath<Duct, Duct.Passage>> findShortestPathToNumberedDucts() {
        return findShortestPathToNumberedDucts(FinalPosition.WHEREVER);
    }

    public List<GraphPath<Duct, Duct.Passage>> findShortestPathToNumberedDucts(FinalPosition finalPosition) {
        Graph<Duct, Duct.Passage> g = makePassageGraph();
        Set<Duct> allNumberedDucts = g.vertexSet().stream().filter(Duct::isNumbered).collect(Collectors.toSet());
        Duct start = allNumberedDucts.stream().filter(d -> d.content == '0').findFirst().get();
        Set<Duct> nonStarters = allNumberedDucts.stream().filter(d -> d.content != '0').collect(Collectors.toSet());
        Collection<List<Duct>> nonStarterPermutations = Collections2.permutations(nonStarters);
        List<List<GraphPath<Duct, Duct.Passage>>> pathsets = new ArrayList<>();
        nonStarterPermutations.forEach(middleDucts -> {
            List<GraphPath<Duct, Duct.Passage>> pathset = new ArrayList<>();
            List<Duct> ducts = new ArrayList<>(middleDucts.size() + 2);
            ducts.add(start);
            ducts.addAll(middleDucts);
            if (finalPosition == FinalPosition.RETURN_TO_START) {
                ducts.add(start);
            }
            for (int i = 0; i < ducts.size() - 1; i++) {
                Duct first = ducts.get(i), last = ducts.get(i + 1);
                DijkstraShortestPath<Duct, Duct.Passage> algo = new DijkstraShortestPath<>(g, first, last);
                GraphPath<Duct, Duct.Passage> path = algo.getPath();
                checkState(path != null, "no path from %s to %s", first, last);
                pathset.add(path);
            }
            pathsets.add(pathset);
        });
        checkState(pathsets.size() > 0, "no path sets?");
        List<GraphPath<Duct, Duct.Passage>> shortest = pathsets.stream().min(Ordering.<Integer>natural().onResultOf(new Function<Iterable<GraphPath<Duct, Duct.Passage>>, Integer>(){
            @Override
            public Integer apply(Iterable<GraphPath<Duct, Duct.Passage>> input) {
                return computeTotalLength(input);
            }
        })).get();
        return shortest;
    }

}
