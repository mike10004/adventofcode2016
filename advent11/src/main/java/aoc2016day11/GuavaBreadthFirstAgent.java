package aoc2016day11;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.TreeTraverser;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GuavaBreadthFirstAgent extends BreadthFirstAgent {

    public GuavaBreadthFirstAgent(int maxMoves) {
        super(maxMoves);
    }

    public Optional<List<Building>> play(Building building) {
        GameTreeTraverser t = new GameTreeTraverser();
        Iterator<TreeNode<Building>> nodes = t.breadthFirstTraversal(new TreeNode<>(building)).iterator();
        while (nodes.hasNext()) {
            TreeNode<Building> node = nodes.next();
            int level = reachedDepth(node.getLevel());
            if (level > maxMoves) {
                break;
            }
            Building state = node.getUserObject();
            if (state.isWin()) {
                List<Building> path = node.getUserObjectPath();
                return Optional.of(Collections.unmodifiableList(path));
            }
        }
        return Optional.empty();
    }

    private static class GameTreeTraverser extends TreeTraverser<TreeNode<Building>> {

        @Override
        public Iterable<TreeNode<Building>> children(TreeNode<Building> root) {
            final List<Building> strategy = root.getUserObjectPath();
            List<Building> children = root.getUserObject().findValidMovesExcept(strategy).collect(Collectors.toList());
            children.forEach(child -> {
                TreeNode<Building> childNode = new TreeNode<>(child);
                root.add(childNode);
                Args.checkState(childNode.getLevel() > 0, "expected node with level > 0");
            });
            return new FluentIterable<TreeNode<Building>>() {
                @Override
                public Iterator<TreeNode<Building>> iterator() {
                    return root.children();
                }
            };
        }
    }

    public static void main(String[] args) {
        attempt(new GuavaBreadthFirstAgent(32), Buildings.createPuzzleInputBuilding());
    }
}
