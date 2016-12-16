package aoc2016day11;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

public class NodeBreadthFirstAgent extends BreadthFirstAgent {

    public NodeBreadthFirstAgent(int maxMoves) {
        super(maxMoves);
    }

    @Override
    public Optional<List<Building>> play(Building start) {
        Queue<Node> queue = new ArrayDeque<>();
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
        while (!queue.isEmpty()) {
            Node current = queue.remove();
            reachedDepth(current.level);
            List<Building> path = current.path();
            if (current.label.isWin()) {
                Collections.reverse(path);
                return path;
            }
            List<Building> next = current.label.findValidMovesExcept(path).collect(Collectors.toList());
            for (Building child : next) {
                queue.add(new Node(child, current));
            }
        }
        return null;
    }

    public static void main(String[] args) {
        Building building = Buildings.createPuzzleInputBuilding();
        Agent agent = new NodeBreadthFirstAgent(32).toggleVerbose();
        attempt(agent, building);
    }

}
