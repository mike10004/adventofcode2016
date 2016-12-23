package aoc2016.day22;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class PartOneTest {

    @Test
    public void main() throws IOException {
        assertEquals("numViablePairs", 1038, PartOne.countViablePairs(Play.puzzleInput));
    }
}