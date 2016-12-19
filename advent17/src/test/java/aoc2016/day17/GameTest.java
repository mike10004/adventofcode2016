package aoc2016.day17;

import com.google.common.collect.Ordering;
import com.google.common.math.IntMath;
import org.junit.Test;

import static org.junit.Assert.*;
import aoc2016.day17.Game.State;
import org.junit.experimental.theories.suppliers.TestedOn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class GameTest {
/*
For example, suppose the passcode is hijkl. Initially, you have taken no steps, and so your path is empty: you simply
find the MD5 hash of hijkl alone. The first four characters of this hash are ced9, which indicate that up is open (c),
down is open (e), left is open (d), and right is closed and locked (9). Because you start in the top-left corner, there
are no "up" or "left" doors to be open, so your only choice is down.

Next, having gone only one step (down, or D), you find the hash of hijklD. This produces f2bc, which indicates that you
can go back up, left (but that's a wall), or right. Going right means hashing hijklDR to get 5745 - all doors closed and
locked. However, going up instead is worthwhile: even though it returns you to the room you started in, your path would
then be DU, opening a different set of doors.

After going DU (and then hashing hijklDU to get 528e), only the right door is open; after going DUR, all doors lock.
(Fortunately, your actual passcode is not hijkl).
 */

    @Test
    public void test1() {
        Game game = new Game("hijkl");
        State state = game.root();
        char[] open = state.findOpen();
        assertArrayEquals("open 1", "D".toCharArray(), open);
    }

    @Test
    public void test2() {
        Game game = new Game("hijkl");
        State state = game.root();
        char[] open;
        state = state.move('D');
        open = state.findOpen();
        assertArrayEquals("open 2", "UR".toCharArray(), open);
    }

    @Test
    public void test3() {
        Game game = new Game("hijkl");
        State state = game.root();
        char[] open;
        state = state.move('D');
        state = state.move('U');
        open = state.findOpen();
        assertArrayEquals("open 3", "R".toCharArray(), open);
    }

    @Test
    public void test4() {
        Game game = new Game("hijkl");
        State state = game.root();
        state = state.move('D');
        state = state.move('U');
        state = state.move('R');
        char[] open = state.findOpen();
        assertArrayEquals("open 4", new char[0], open);

    }

    @Test
    public void testHash() {
        // hijklDU to get 528e
        State state = new Game("hijkl").root().move('D').move('U');
        String hash = state.computeHash();
        assertEquals("hash chars", "528e", hash.substring(0, 4));
    }

    @Test
    public void play1() {
        // If your passcode were ihgpwlah, the shortest path would be DDRRRD.
        testPlay("ihgpwlah", "DDRRRD");
    }

    private void testPlay(String passcode, String shortestPath) {
        Game game = new Game(passcode);
        State finalState = game.play(shortestPath.length() + 1);
        System.out.format("starting with '%s', final state = %s%n", passcode, finalState);
        assertNotNull("did not win game", finalState);
        assertEquals("shortest path", shortestPath, finalState.concatTrail());
    }

    @Test
    public void play2() {
        // With kglvqrro, the shortest path would be DDUDRLRRUDRD.
        testPlay("kglvqrro", "DDUDRLRRUDRD");
    }

    @Test
    public void play3() {
        // With ulqzkmiv, the shortest would be DRURDRUDDLLDLUURRDULRLDUUDDDRR.
        testPlay("ulqzkmiv", "DRURDRUDDLLDLUURRDULRLDUUDDDRR");
    }

    private void testSearch(String passcode, int longestPathLength) {
        int maxDepth = IntMath.checkedAdd(longestPathLength, 5);
        Game game = new Game(passcode);
        Optional<State> actual = game.findLongestPath(maxDepth);
        System.out.format("longest path found: %s%n", actual.orElse(null));
        assertEquals("path", longestPathLength, actual.get().trail.length);
    }

    @Test
    public void search1() {
//        If your passcode were ihgpwlah, the longest path would take 370 steps.
//                With kglvqrro, the longest path would be 492 steps long.
//                With ulqzkmiv, the longest path would be 830 steps long.
        testSearch("ihgpwlah", 370);
    }

    @Test
    public void search2() {
//                With kglvqrro, the longest path would be 492 steps long.
        testSearch("kglvqrro", 492);
    }

    @Test
    public void search3() {

//                With ulqzkmiv, the longest path would be 830 steps long.
        testSearch("ulqzkmiv", 830);
    }
}