package aoc2016.day22;

import com.google.common.io.CharSource;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class NodeTest {
    @Test
    public void parseAll() throws Exception {
        String input = "root@ebhq-gridcenter# df -h\n" +
                "Filesystem              Size  Used  Avail  Use%\n" +
                "/dev/grid/node-x0-y0     93T   68T    25T   73%\n" +
                "/dev/grid/node-x0-y1     91T   69T    22T   75%";

        List<Node> nodes = Node.parseAll(CharSource.wrap(input));
        assertEquals("count", 2, nodes.size());
        Node n = nodes.get(0);
        assertEquals("x", 0, n.x);
        assertEquals("y", 0, n.y);
        assertEquals("used", 68, n.used);
        assertEquals("available", 25, n.available);
    }

    private static Node n(int x, int y, int used, int available) {
        return new Node(x, y, used, available);
    }

    @Test
    public void canMoveDataTo() {
        assertTrue(n(0, 0, 1, 0).canMoveDataTo(n(1, 1, 4, 3)));
        assertTrue(n(0, 0, 1, 0).canMoveDataTo(n(1, 1, 4, 1)));
        assertTrue(n(0, 0, 1, 0).canMoveDataTo(n(1, 1, 4, 1)));
        assertFalse(n(0, 0, 1, 0).canMoveDataTo(n(0, 0, 4, 1)));
        assertFalse(n(0, 0, 0, 1).canMoveDataTo(n(1, 1, 4, 5)));
        assertFalse(n(0, 0, 3, 1).canMoveDataTo(n(1, 1, 4, 2)));
    }
}