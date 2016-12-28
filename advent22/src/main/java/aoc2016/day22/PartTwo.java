package aoc2016.day22;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.AStarShortestPath;
import org.jgrapht.alg.interfaces.AStarAdmissibleHeuristic;
import org.jgrapht.graph.AbstractGraph;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Actually did this one without programming, using this logic:
 *
 * <pre>
 * We start with a few possible moves around (35, 24).
 *  +   1 move to create an empty spot at (34, 24)
 *  +  27 moves to get the empty spot to (12, 19), left of the wall of overloaded nodes
 *  +  43 moves to get the empty spot to (36, 0)
 *  +   1 move of payload data from (37, 0) to (36, 0)
 *  + 180 moves to get the goal data over to (0, 0), using 36 5-move cycles
 * ------
 *    252 moves total
 * </pre>
 *
 * <p>The demo below plays out this strategy with the legality
 * of all moves made enforced.
 */
public class PartTwo {

    public static void main(String[] args) throws Exception {
        List<Node> nodes = Node.parseAll(Play.puzzleInput, Play.PUZZLE_INPUT_PAYLOAD_X, Play.PUZZLE_INPUT_PAYLOAD_Y);
        Point finalTarget = new Point(0, 0);
        Grid grid = Grid.make(nodes);
        AtomicInteger moveCounter = new AtomicInteger(0);
        grid.setMoveListener(move -> System.out.format("performed: %s -> %s%n", move.src, move.dst));
        System.out.println("initial grid:");
        System.out.println(grid.illustrate());
        System.out.println("creating empty node");
        grid = grid.move(Pair.of(grid.findNodeByPosition(34, 24).get(), grid.findNodeByPosition(35, 24).get()));
        Point emptySpot = grid.findEmptyNodeOrDie().position;
        checkState(emptySpot.x == 34 && emptySpot.y == 24, "empty spot in unexpected location: %s", emptySpot);
        moveCounter.incrementAndGet();
        System.out.println(grid.illustrate());
        System.out.println();
        Point waypoint = new Point(12, 19);
        grid = moveEmptySpotAlongShortestPath(grid, waypoint, moveCounter);
        emptySpot = grid.findEmptyNodeOrDie().position;
        System.out.format("empty spot currently at %s%n", emptySpot);
        Point nextToPayload = new Point(36, 0);
        grid = moveEmptySpotAlongShortestPath(grid, nextToPayload, moveCounter);

        try {
            int dx = -1, dy = 0;
            Queue<Pair> queue = new ArrayDeque<>();
            System.out.format("performing data move cycles until empty spot is at final target (%d, %d)%n", finalTarget.x, finalTarget.y);
            int numCycleMoves = 0;
            while (moveCounter.get() < DataMoveCycle.CYCLE_MOVES_LIMIT) {
                if (queue.isEmpty()) {
                    Pair movePayloadOver = grid.moves()
                            .filter(p -> p.getDirectionX() == dx && p.getDirectionY() == dy).findFirst()
                            .orElseThrow(() -> new IllegalStateException("no moves in correct direction"));
                    System.out.println("moving payload over one");
                    grid = grid.move(movePayloadOver);
                    numCycleMoves++;
                    moveCounter.incrementAndGet();
                    if (grid.isWin(finalTarget.x, finalTarget.y)) {
                        break;
                    }
//                    System.out.println(grid.illustrate());
                    queue.addAll(new DataMoveCycle(grid, dx, dy).getCycle());
                    System.out.println("performing 4-move cycle");
                }
                Pair move = queue.remove();
                grid = grid.move(move);
                moveCounter.incrementAndGet();
                numCycleMoves++;
            }
            System.out.format("%d cycle moves performed%n", numCycleMoves);
            System.out.println(grid.illustrate());
            boolean won = grid.isWin(finalTarget.x, finalTarget.y);
            System.out.format("finally a win? %s%n", won);
            System.out.println(moveCounter.get() + " moves");
            if (!won) {
                System.exit(2);
            }
        } catch (RuntimeException e) {
            System.out.println(e.toString());
            System.out.format("at %s%n", e.getStackTrace()[0]);
            System.exit(2);
        }
    }

    private static Grid moveEmptySpotAlongShortestPath(Grid grid, Point emptySpotDestination, AtomicInteger moveCounter) {
        Graph<GridVertex, Movement> g = new GridGraph();
        AStarShortestPath<GridVertex, Movement> algo = new AStarShortestPath<>(g);
        AStarAdmissibleHeuristic<GridVertex> taxicabMetric = (sourceVertex, targetVertex) -> {
            return Math.abs(sourceVertex.emptyNode.x - targetVertex.emptyNode.x)
                    + Math.abs(sourceVertex.emptyNode.y - targetVertex.emptyNode.y);
        };
        Point emptySpot = grid.findEmptyNodeOrDie().position;
        GridVertex start = new KnownGridVertex(emptySpot, grid);
        GridVertex target = new NoNextMoveGridVertex(emptySpotDestination);
        GraphPath<GridVertex, Movement> path = algo.getShortestPath(start, target, taxicabMetric);
        System.out.format("%s moves to get empty spot to %s: %s%n", path == null ? "N/A" : path.getLength(), emptySpotDestination, path);
        checkState(path != null, "no path found to move empty spot from %s to %s", emptySpot, emptySpotDestination);
        System.out.format("moving empty spot to %s in %d moves%n", emptySpotDestination, path.getLength());
        List<Pair> pathMoves = path.getEdgeList().stream().map(m -> m.pair).collect(Collectors.toList());
        for (Pair move : pathMoves) {
            grid = grid.move(move);
            moveCounter.incrementAndGet();
            emptySpot = move.src.position;
        }
        checkState(grid.findEmptyNodeOrDie().position.equals(emptySpot), "movements left unexpected empty spot in grid");
        checkState(emptySpot.equals(emptySpotDestination), "ended with empty spot %s not in expected location %s", emptySpot, emptySpotDestination);
        System.out.println(grid.trace(pathMoves));
        return grid;
    }

    private static class DataMoveCycle {

        private static final int CYCLE_MOVES_LIMIT = 5000;

        private final int directionX, directionY;
        private Point emptySpot;
        private Grid current;
        private Point initialEmptySpot;
        private List<Pair> moves;

        private DataMoveCycle(Grid start, int directionX, int directionY) {
            this.directionX = directionX;
            this.directionY = directionY;
            Node oppositeSide = start.findNodeByPosition(start.payload.x - directionX, start.payload.y - directionY).get();
            checkArgument(oppositeSide.used == 0, "node on opposite side %s is not empty: %s", oppositeSide, oppositeSide.encode());
            initialEmptySpot = oppositeSide.position;
            emptySpot = initialEmptySpot;
            current = start;
            moves = new ArrayList<>(5);
        }

        public List<Pair> getCycle() {
            if (moves.isEmpty()) {
                System.out.println("calculating cycle of next moves");
                Pair movePerpendicular = current.moves()
                        .filter(p -> !p.isParallel(directionX, directionY)).findFirst()
                        .orElseThrow(() -> new IllegalStateException("no moves perpendicular to direction " + directionX + ", " + directionY));
                perform(movePerpendicular);
                Point emptySpotTarget = new Point(emptySpot.x + (directionX * 2), emptySpot.y + (directionY * 2));
                while (!emptySpotTarget.equals(emptySpot)) {
                    Pair moveParallel = current.moves()
                            .filter(p -> p.isParallel(directionX, directionY) && p.isOppositeDirection(directionX, directionY))
                            .findFirst().orElseThrow(() -> new IllegalStateException("no parallel moves"));
                    perform(moveParallel);
                }
                Pair movePerpendicularReverse = current.moves().filter(p -> !p.isParallel(directionX, directionY) && (p.src.x == initialEmptySpot.x || p.src.y == initialEmptySpot.y)).findFirst().orElseThrow(() -> new IllegalStateException("no moves back to beside payload"));
                perform(movePerpendicularReverse);
                moves = ImmutableList.copyOf(moves);
                System.out.format("calculated cycle of next moves: %s%n", moves);
            }
            return moves;
        }

        private void perform(Pair move) {
//            System.out.format("calculated: %s%n", move);
            current = current.move(move, null);
            emptySpot = move.src.position;
            moves.add(move);
        }
    }

    private static class NoNextMoveGridVertex extends GridVertex {

        private NoNextMoveGridVertex(Point emptyNode) {
            super(emptyNode);
        }

        @Override
        public Set<Movement> produceMovements() {
            return ImmutableSet.of();
        }
    }

    private static final boolean debugStreams = false;

    private static <T> Stream<T> debugStream(Stream<T> stream) {
        if (debugStreams) {
            return stream.collect(Collectors.toList()).stream();
        } else {
            return stream;
        }
    }

    private static class KnownGridVertex extends GridVertex {

        private final Grid grid;
        private Set<Movement> movements;

        public KnownGridVertex(Point emptyNode, Grid grid) {
            super(emptyNode);
            this.grid = grid;
        }

        @Override
        public Set<Movement> produceMovements() {
            if (movements == null) {
                Stream<Pair> allMoves = debugStream(grid.moves());
                Stream<Pair> movesThatPutDataOntoEmptyNode = debugStream(allMoves.filter(p -> p.dst.isAtPosition(emptyNode)));
                movements = movesThatPutDataOntoEmptyNode
                        .map(p -> new Movement(emptyNode, p, this))
                        .collect(Collectors.toSet());
            }
            return movements;
        }
    }

    private static abstract class GridVertex {
        public final Point emptyNode;

        private GridVertex(Point emptyNode) {
            this.emptyNode = emptyNode;
        }

        public abstract Set<Movement> produceMovements();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GridVertex)) return false;

            GridVertex that = (GridVertex) o;

            return emptyNode.equals(that.emptyNode);
        }

        @Override
        public int hashCode() {
            return emptyNode.hashCode();
        }

        @Override
        public String toString() {
            return String.format("_(%d, %d)", emptyNode.x, emptyNode.y);
        }
    }

    private static class Movement {
        public final Pair pair;
        public final Point emptySpotFrom, emptySpotTo;
        public final GridVertex sourceVertex;
        public final GridVertex targetVertex;

        private Movement(Point emptySpotFrom, Pair pair, KnownGridVertex sourceVertex) {
            this.pair = pair;
            this.emptySpotFrom = emptySpotFrom;
            emptySpotTo = pair.src.position;
            this.sourceVertex = sourceVertex;
            this.targetVertex = new KnownGridVertex(emptySpotTo, sourceVertex.grid.move(pair, null));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Movement)) return false;

            Movement movement = (Movement) o;

            if (!emptySpotFrom.equals(movement.emptySpotFrom)) return false;
            return emptySpotTo.equals(movement.emptySpotTo);
        }

        @Override
        public int hashCode() {
            int result = emptySpotFrom.hashCode();
            result = 31 * result + emptySpotTo.hashCode();
            return result;
        }
    }

    /**
     * Graph implementation that only implements the methods necessary for {@link AStarShortestPath} to run.
     */
    private static class GridGraph extends AbstractGraph<GridVertex, Movement> implements DirectedGraph<GridVertex, Movement> {

        @Override
        public Set<Movement> getAllEdges(GridVertex sourceVertex, GridVertex targetVertex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Movement getEdge(GridVertex sourceVertex, GridVertex targetVertex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public EdgeFactory<GridVertex, Movement> getEdgeFactory() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Movement addEdge(GridVertex sourceVertex, GridVertex targetVertex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addEdge(GridVertex sourceVertex, GridVertex targetVertex, Movement pair) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addVertex(GridVertex gridVertex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsEdge(Movement pair) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsVertex(GridVertex gridVertex) {
            return true;
        }

        @Override
        public Set<Movement> edgeSet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Movement> edgesOf(GridVertex vertex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Movement removeEdge(GridVertex sourceVertex, GridVertex targetVertex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeEdge(Movement pair) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeVertex(GridVertex gridVertex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<GridVertex> vertexSet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public GridVertex getEdgeSource(Movement pair) {
            return pair.sourceVertex;
        }

        @Override
        public GridVertex getEdgeTarget(Movement pair) {
            return pair.targetVertex;
        }

        @Override
        public double getEdgeWeight(Movement pair) {
            return 0;

        }

        @Override
        public String toString() {
            return "GridGraph";
        }

        @Override
        public int inDegreeOf(GridVertex vertex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Movement> incomingEdgesOf(GridVertex vertex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int outDegreeOf(GridVertex vertex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<Movement> outgoingEdgesOf(GridVertex vertex) {
            return vertex.produceMovements();
        }
    }
}
