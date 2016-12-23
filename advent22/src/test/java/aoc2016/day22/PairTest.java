package aoc2016.day22;

import org.junit.Test;

import static org.junit.Assert.*;

public class PairTest {
    @Test
    public void isReverse() throws Exception {
        Node src1 = new Node(4, 5, 10, 100, false);
        Node dst1 = new Node(4, 6, 10, 100, false);
        Pair first = new Pair(src1, dst1);
        Pair result1 = first.move();
        Pair reverse = new Pair(result1.dst, result1.src);
        assertTrue("reverse", reverse.isReverse(first));
        assertTrue("symmetric", first.isReverse(reverse));
    }

}