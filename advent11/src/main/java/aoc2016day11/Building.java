package aoc2016day11;

import com.google.common.math.LongMath;
import com.google.common.primitives.Ints;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class Building {

    private final int numFloors;
    private final int elevator;
    private final Element[] elements;
    public final long hash;
    private final ElementCache cache;

    public Building(int numFloors, int elevator, Element[] elements, ElementCache elementCache) {
        this.numFloors = numFloors;
        checkArgument(numFloors > 0, "numFloors %s", numFloors);
        this.elevator = elevator;
        checkArgument(elevator >= 0 && elevator < numFloors, "elevator=%s, numFloors=%s", elevator, numFloors);
        this.elements = (elements);
        checkArgument(elements.length != 0, "no RTG/microchip pairs in building");
        for (Element el : elements) {
            checkArgument(isInside(el), "some things are outside the building");
        }
        this.hash = computeHash(elements);
        this.cache = checkNotNull(elementCache);
    }

    final int selectHash(Element element) {
        return primes[1 + (element.generator * numFloors + element.microchip)];
    }

    final long computeHash(Element[] elements) {
         long h = LongMath.checkedPow(primes[0], elevator);
         for (Element element : elements) {
             h = LongMath.checkedMultiply(h, selectHash(element));
         }
         return h;
    }

    static class ElementCache {
        private final int NUM_FLOORS;
        private final Element[] items;

        public ElementCache(int numFloors) {
            this.items = buildItems(numFloors);
            this.NUM_FLOORS = numFloors;
        }

        private static Element[] buildItems(int NUM_FLOORS) {
            Element[] items_ = new Element[NUM_FLOORS * NUM_FLOORS];
            for (int microchip = 0; microchip < NUM_FLOORS; microchip++) {
                for (int generator = 0; generator < NUM_FLOORS; generator++) {
                    items_[generator * NUM_FLOORS + microchip] = new Element(microchip, generator);
                }
            }
            return items_;
        }

        public Element get(int microchip, int generator) {
            return items[generator * NUM_FLOORS + microchip];
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Building that = (Building) o;
        return numFloors == that.numFloors
                && elevator == that.elevator
                && hash == that.hash;
    }

    @Override
    public int hashCode() {
        return Ints.checkedCast(hash % Integer.MAX_VALUE);
    }

    protected boolean isSafe() {
        return isSafe(elements);
    }

    protected static boolean isSafe(Element[] elements) {
        for (int j = 0; j < elements.length; j++) {
            Element e = elements[j];
            if (e.generator != e.microchip) {
                for (int i = 0; i < elements.length; i++) {
                    if (i != j && elements[i].generator == e.microchip) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    protected boolean isElevatorInBuilding() {
        return elevator >= 0 && elevator < numFloors;
    }

    protected final boolean isInside(int floor) {
        return floor >= 0 && floor < numFloors;
    }

    protected final boolean isInside(Element element) {
        return isInside(element.microchip) && isInside(element.generator);
    }

    static class Move {

        public final Building from, to;
        public final Direction direction;
        public final Target target1;
        public final @Nullable Target target2;

        public Move(Building from, Direction direction, Target target1, @Nullable Target target2, Building to) {
            this.from = checkNotNull(from);
            this.to = checkNotNull(to);
            this.direction = direction;
            this.target1 = checkNotNull(target1);
            this.target2 = target2;
        }

        @Override
        public String toString() {
            String ss;
            if (target2 != null) {
                ss = "[" + target1 + ", " + target2 + "]";
            } else {
                ss = "[" + target1 + "]";
            }
            return direction.toString().toLowerCase() + ss;
        }

        @SuppressWarnings("Duplicates")
        public static Element[] applyTargets(Building from, Direction direction, Target target1, @Nullable Target target2) {
            Element[] b = new Element[from.elements.length];
            System.arraycopy(from.elements, 0, b, 0, b.length);
            for (int i = 0; i < from.elements.length; i++) {
                Element current = from.elements[i];
                if (i == target1.elementIndex || (target2 != null && i == target2.elementIndex)) {
                    int microchipOffset = 0, generatorOffset = 0;
                    if (target1.elementIndex == i) {
                        switch (target1.kind) {
                            case G:
                                generatorOffset += direction.offset();
                                break;
                            case M:
                                microchipOffset += direction.offset();
                                break;
                        }
                    }
                    if (target2 != null && target2.elementIndex == i) {
                        switch (target2.kind) {
                            case G:
                                generatorOffset += direction.offset();
                                break;
                            case M:
                                microchipOffset += direction.offset();
                                break;
                        }
                    }
                    Element element = from.cache.get(current.microchip + microchipOffset, current.generator + generatorOffset);
                    b[i] = element;
                }
            }
            return b;
        }

    }

    public boolean isWin() {
        int top = numFloors - 1;
        for (Element element : elements) {
            if (!element.isBothOn(top)) {
                return false;
            }
        }
        return true;
    }

    public int getNumFloors() {
        return numFloors;
    }

    private boolean canElevate(Direction direction) {
        return isInside(elevator + direction.offset());
    }

    protected List<Target> targetsOnFloor() {
        List<Target> targets = new ArrayList(elements.length * kinds_.length);
        for (int i = 0; i < elements.length; i++) {
            Element element = elements[i];
            for (Kind kind : kinds_) {
                if (element.get(kind) == elevator) {
                    targets.add(new Target(i, kind));
                }
            }
        }
        return targets;
    }

    protected void maybeAddMove(Collection<Move> moves, Direction direction, Target t1, @Nullable Target t2) {
        Element[] newElements = Move.applyTargets(this, direction, t1, t2);
        if (isSafe(newElements)) {
            Building to = new Building(numFloors, elevator + direction.offset(), newElements, cache);
            if (!containsMoveWithBuilding(moves, to)) {
                Move m = new Move(this, direction, t1, t2, to);
                moves.add(m);
            }
        }
    }

    private static boolean containsMoveWithBuilding(Collection<Move> moves, Building b) {
        for (Move m : moves) {
            if (b.equals(m.to)) {
                return true;
            }
        }
        return false;
    }

    protected void findMoves(Direction direction, Collection<Move> moves) {
        List<Target> targets = targetsOnFloor();
        for (int i = 0; i < targets.size(); i++) {
            Target t = targets.get(i);
            maybeAddMove(moves, direction, t, null); // everything is safe in elevator by itself
            for (int j = i + 1; j < targets.size(); j++) {
                Target u = targets.get(j);
                if (t.elementIndex == u.elementIndex || t.kind == u.kind) {
                    maybeAddMove(moves, direction, t, u);
                }
            }
        }
    }

    private static final Kind[] kinds_ = Kind.values();
    private static final Direction[] directions_ = Direction.values();

    protected List<Move> findValidMoves() {
        List<Move> moves = new ArrayList<>(16);
        for (Direction direction : directions_) {
            if (canElevate(direction)) {
                findMoves(direction, moves);
            }
        }
        return moves;
    }

    public Stream<Building> computeReachable(Collection<Building> prohibited) {
        return computeReachable(b -> !prohibited.contains(b));
    }

    public Stream<Building> computeReachable(Predicate<Building> allowed) {
        Stream<Building> validNotProhibitedResults = findValidMoves().stream()
                .map(m -> m.to)
                .filter(allowed);
        return validNotProhibitedResults;
    }

    private static final char[] _charmap = "PXRSTEDABCF".toCharArray();

    private static char getElementSymbol(int elementIndex) {
        return _charmap[elementIndex];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        for (int floor = numFloors - 1; floor >= 0; floor--) {
            sb.append(String.format("%2d %2s ", floor + 1, floor == elevator ? "E" : "."));
            for (int i = 0; i < elements.length; i++) {
                Element e = elements[i];
                char ch = getElementSymbol(i);
                boolean ghere = e.generator == floor, mhere = e.microchip == floor;
                sb.append(String.format("%s%s ", ghere ? ch : " ", ghere ? "G" : "."));
                sb.append(String.format("%s%s ", mhere ? ch : " ", mhere ? "M" : "."));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    static class InvalidMoveException extends IllegalArgumentException {
        public InvalidMoveException(String s) {
            super(s);
        }
    }

    public Building move(Direction direction, Target target1, @Nullable Target target2) {
        Element[] newElements = Move.applyTargets(this, direction, target1, target2);
        Building next = new Building(numFloors, elevator + direction.offset(), newElements, cache);
        if (!next.isSafe()) {
            throw new InvalidMoveException("next is unsafe");
        }
        return next;
    }

    public long countMaxPossibleStates() {
        return numFloors * LongMath.checkedPow(numFloors * numFloors, elements.length);
    }

    private static final int[] primes = {2, 3, 5, 7, 11,
                                        13, 17, 19, 23, 29,
                                        31, 37, 39, 41, 43,
                                        47, 53, 57, 59, 61,
                                        67, 71, 73, 79, 83,
            89, 97, 101, 103, 107, 109, 113, 127, 131, 137,
            139, 149, 151, 157, 163, 167, 173, 179, 181, 191,
            193, 197, 199, 211, 223, 227, 229, 233, 239, 241,
            251, 257, 263, 269, 271, 277, 281, 283, 293, 307,
            311, 313, 317, 331, 337, 347, 349};
}
