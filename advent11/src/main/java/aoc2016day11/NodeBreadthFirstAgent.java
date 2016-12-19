package aoc2016day11;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class NodeBreadthFirstAgent extends Agent {

    private static final int EXPECTED_QUEUE_SIZE = 10 * 1024 * 1024;

    public NodeBreadthFirstAgent(int maxMoves) {
        super(maxMoves);
    }

    @Override
    public Optional<List<Building>> play(Building start) {
        Queue<Node> queue = new ArrayDeque<>(EXPECTED_QUEUE_SIZE);
        Node root = Node.root(start);
        queue.add(root);
        List<Building> result = playBF(queue);
        return Optional.ofNullable(result);
    }

/* https://en.wikipedia.org/wiki/Breadth-first_search
Breadth-First-Search(Graph, root):

    for each node n in Graph:
        n.distance = INFINITY
        n.parent = NIL

    create empty queue Q

    root.distance = 0
    Q.enqueue(root)

    while Q is not empty:
        current = Q.dequeue()
        for each node n that is adjacent to current:
            if n.distance == INFINITY:
                n.distance = current.distance + 1
                n.parent = current
                Q.enqueue(n)

 */
    private @Nullable List<Building> playBF(Queue<Node> queue) {
        long maxPossibleStates = ((MyBuilding)(queue.peek().label)).countMaxPossibleStates();
        Set<Building> visited = new HashSet<>(EXPECTED_QUEUE_SIZE);
        while (!queue.isEmpty()) {
            maybePrintAttempts(queue.size());
            if (visited.size() > maxPossibleStates) {
                throw new IllegalStateException(String.format("%d states have been examined but max possible is %d", visited.size(), maxPossibleStates));
            }
            Node current = queue.remove();
            visited.add(current.label);
            reachedDepth(current.level, queue.size());
            if (current.label.isWin()) {
                List<Building> path = current.path();
                Collections.reverse(path);
                return path;
            }
            if (current.level + 1 <= maxMoves) {
                final int degree[] = new int[1];
                current.label.computeReachable(visited)
                        .forEach(b -> {
                            queue.add(new Node(b, current));
                            degree[0]++;
                        });
                if (degree[0] > MAX_DEGREE) {
//                    List<Building> reachable = current.label.computeReachable(visited).collect(Collectors.toList());
//                    ((MyBuilding)current.label).findValidMoves();
                    throw new IllegalStateException("degree " + degree[0] + " > max " + MAX_DEGREE);
                }
            }
        }
        return null;
    }
    private static final int MAX_DEGREE = 16;
    private int currentDepth = -1;

    protected int reachedDepth(int depth, int queueSize) {
        if (currentDepth != depth) {
            depthChanged(depth, queueSize);
        }
        currentDepth = depth;
        return depth;
    }

    protected void depthChanged(int newDepth, int queueSize) {
        if (isVerbose()) {
            System.out.format("depth changed %d; queue size = %d%n", newDepth, queueSize);
        }
    }

    public static void main(String[] args) {
        Building building = Buildings.createPuzzleInputBuilding();
        Agent agent = new NodeBreadthFirstAgent(32).toggleVerbose();
        attempt(agent, building);
    }

}
