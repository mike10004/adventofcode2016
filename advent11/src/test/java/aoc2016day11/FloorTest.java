package aoc2016day11;

import org.junit.Test;

import static org.junit.Assert.*;

public class FloorTest {

    @Test
    public void test_toString() throws Exception {
        Floor f = Floor.Factory.getInstance().get("PG", "PM");
        System.out.println(f.toString());
    }

}