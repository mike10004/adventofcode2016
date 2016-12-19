package aoc2016day11;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Trajectory {

    public static void main(String[] args) {
        int maxNumElements = 5, numFloors = 4, maxMoves = 64;
        List<Integer> moveCounts = new ArrayList<>();
        for (int numElements = 1; numElements < maxNumElements; numElements++) {
            Buildings.Builder bb = Buildings.build(numFloors);
            for (int i = 0; i < numElements; i++) {
                bb.add(0, 0);
            }
            Building building = bb.finish(0);
            Agent agent = new BreadthFirstAgent(maxMoves);
            System.out.format("%splaying with %d elements...", building, numElements);
            Optional<List<Building>> strategy = agent.play(building);
            if (!strategy.isPresent()) {
                System.err.println("no winning strategy with " + numElements + " elements");
                break;
            }
            int count = Buildings.countMoves(strategy.get());
            System.out.format("%d moves to win%n", count);
            moveCounts.add(count);
        }
        System.out.println("trajectory: " + moveCounts);
    }

}
