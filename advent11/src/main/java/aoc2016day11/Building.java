package aoc2016day11;

import java.util.Collection;
import java.util.stream.Stream;

public interface Building {

    boolean isWin();
    int getNumFloors();
    Stream<Building> computeReachable(Collection<Building> prohibited);

}
