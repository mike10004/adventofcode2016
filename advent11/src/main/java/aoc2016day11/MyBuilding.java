package aoc2016day11;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Sets;
import com.google.common.math.IntMath;
import com.google.common.math.LongMath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class MyBuilding implements Building {

    private final int numFloors;
    private final int elevator;
    private final ImmutableList<Element> elements;
    private final ImmutableMultiset<Element> elementSet;
    private final ElementCache cache;

    public MyBuilding(int numFloors, int elevator, List<Element> elements, String s) {
        this(numFloors, elevator, elements, new ElementCache(numFloors));
    }

    public MyBuilding(int numFloors, int elevator, List<Element> elements, ElementCache elementCache) {
        this.numFloors = numFloors;
        checkArgument(numFloors > 0, "numFloors %s", numFloors);
        this.elevator = elevator;
        checkArgument(elevator >= 0 && elevator < numFloors, "elevator=%s, numFloors=%s", elevator, numFloors);
        this.elements = ImmutableList.copyOf(elements);
        checkArgument(!elements.isEmpty(), "no RTG/microchip pairs in building");
        checkArgument(this.elements.stream().allMatch(this::isInside));
        elementSet = ImmutableMultiset.copyOf(elements);
        this.cache = checkNotNull(elementCache);
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
        MyBuilding that = (MyBuilding) o;
        if (numFloors != that.numFloors) return false;
        if (elevator != that.elevator) return false;
        return elementSet.equals(that.elementSet);
    }

    @Override
    public int hashCode() {
        int result = numFloors;
        result = 31 * result + elevator;
        result = 31 * result + elementSet.hashCode();
        return result;
    }

    protected static final class MoveResult {

        final Move move;
        final MyBuilding building;
        final String reason;

        public static MoveResult elevatorOutOfBounds(Move move) {
            return new MoveResult(move, null, "elevator out of bounds");
        }

        public static MoveResult dangerous(Move move) {
            return new MoveResult(move, null, "dangerous");
        }

        public static MoveResult valid(Move move, MyBuilding building) {
            return new MoveResult(move, Objects.requireNonNull(building), "ok");
        }

        private MoveResult(Move move, MyBuilding building, String reason) {
            this.building = building;
            this.move = checkNotNull(move);
            this.reason = Objects.requireNonNull(reason);
        }

        public boolean isValid() {
            return building != null;
        }
    }

    protected Stream<Element> generatorsOnFloor(int floor) {
        return elements.stream().filter(e -> e.generator == floor);
    }

    protected boolean isSafe() {
        for (int j = 0; j < elements.size(); j++) {
            Element e = elements.get(j);
            if (e.generator != e.microchip) {
                for (int i = 0; i < elements.size(); i++) {
                    if (i != j && elements.get(i).generator == e.microchip) {
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

    protected boolean isInside(int floor) {
        return floor >= 0 && floor < numFloors;
    }

    protected boolean isInside(Element element) {
        return isInside(element.microchip) && isInside(element.generator);
    }

    static class Move {

        public final MyBuilding from;
        public final Direction direction;
        public final ImmutableSetMultimap<Integer, Kind> offsets;
        private final int numUniqueElements;
        public Move(MyBuilding from, Direction direction, Set<Target> offsets) {
            this.from = from;
            this.direction = direction;
            checkArgument(offsets.size() == 1 || offsets.size() == 2);
            ImmutableSetMultimap.Builder<Integer, Kind> b = ImmutableSetMultimap.builder();
            for (Target t : offsets) {
                b.put(t.elementIndex, t.kind);
            }
            this.offsets = b.build();
            checkArgument(offsets.size() == this.offsets.size(), "non-unique targets: %s", offsets);
            numUniqueElements = this.offsets.keySet().size();
        }

        @Override
        public String toString() {
            List<String> ss = offsets.entries().stream().map(e -> {
                char ch = getElementSymbol(e.getKey());
                return String.format("%s%s", ch, e.getValue());
            }).collect(Collectors.toList());
            return direction.toString().toLowerCase() + ss.toString();
        }

        public MoveResult perform() {
            int newElevatorPosition = from.elevator + direction.offset();
            if (newElevatorPosition < 0 || newElevatorPosition >= from.numFloors) {
                throw new IllegalStateException("illegal move: new elevator position " + newElevatorPosition);
            }
            if (offsets.size() == 2) {
                if (numUniqueElements > 1) {
                    Iterator<Kind> it = offsets.values().iterator();
                    Kind k1 = it.next(), k2 = it.next();
                    if (k1 != k2) {
                        throw new IllegalStateException("illegal move: dangerous combo in elevator");
                    }
                }
            }
            ImmutableList<Element> newElements = applyTargets();
            checkState(from.isAllInside(newElements));
            MyBuilding next = new MyBuilding(from.numFloors, from.elevator + direction.offset(), newElements, from.cache);
            if (!next.isSafe()) {
                return MoveResult.dangerous(this);
            } else {
                return MoveResult.valid(this, next);
            }
        }

        private ImmutableList<Element> applyTargets() {
            ImmutableList.Builder<Element> b = ImmutableList.builder();
            for (int i = 0; i < from.elements.size(); i++) {
                Element current = from.elements.get(i);
                Set<Kind> targetKinds = offsets.get(i);
                if (!targetKinds.isEmpty()) {
                    int microchipOffset = targetKinds.contains(Kind.M) ? direction.offset() : 0;
                    int generatorOffset = targetKinds.contains(Kind.G) ? direction.offset() : 0;
                    Element element = from.cache.get(current.microchip + microchipOffset, current.generator + generatorOffset);
                    b.add(element);
                } else {
                    b.add(current);
                }
            }
            return b.build();
        }

    }


    private boolean isAllInside(Iterable<Element> elements) {
        for (Element element : elements) {
            if (!isInside(element)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isWin() {
        int top = numFloors - 1;
        for (Element element : elements) {
            if (!element.isBothOn(top)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getNumFloors() {
        return numFloors;
    }

    private boolean canElevate(Direction direction) {
        return isInside(elevator + direction.offset());
    }

    protected List<Target> targetsOnFloor() {
        ImmutableList.Builder<Target> targets = ImmutableList.builder();
        for (int i = 0; i < elements.size(); i++) {
            Element element = elements.get(i);
            for (Kind kind : kinds_) {
                if (element.get(kind) == elevator) {
                    targets.add(new Target(i, kind));
                }
            }
        }
        return targets.build();
    }

    protected Stream<Move> findMoves(Direction direction) {
        List<Target> targets = targetsOnFloor();
        List<Set<Target>> carryables = new ArrayList<>(IntMath.checkedPow(2, targets.size()) - 1);
        for (int i = 0; i < targets.size(); i++) {
            Target t = targets.get(i);
            carryables.add(ImmutableSet.of(t)); // everything is safe in elevator by itself
            for (int j = i + 1; j < targets.size(); j++) {
                Target u = targets.get(j);
                if (t.elementIndex == u.elementIndex || t.kind == u.kind) {
                    carryables.add(ImmutableSet.of(t, u));
                }
            }
        }
        return carryables.stream().map(tlist -> new Move(this, direction, tlist));
    }

    private static final Kind[] kinds_ = Kind.values();
    private static final Direction[] directions_ = Direction.values();

    protected Stream<Move> findValidMoves() {
        Stream<Direction> directions = Stream.of(directions_).filter(this::canElevate);
        Stream<Move> moves = directions.map(this::findMoves).flatMap(x -> x);
        Stream<MoveResult> validResults = moves.map(Move::perform).filter(MoveResult::isValid);
        final Set<Building> uniqueResults = new HashSet<>();
        Stream<MoveResult> validUniqueResults = validResults.filter(r -> uniqueResults.add(r.building));
        if (uniqueResults.size() > 6) {
            throw new IllegalStateException("lots of moves");
        }
        return validUniqueResults.map(r -> r.move);
    }

    @Override
    public Stream<Building> computeReachable(Collection<Building> prohibited) {
//        Stream<Direction> directions = Stream.of(directions_).filter(this::canElevate);
//        Stream<Move> moves = directions.map(this::findMoves).flatMap(x -> x);
//        Stream<MoveResult> results = moves.map(Move::perform);
//        Stream<MoveResult> validResults = results.filter(MoveResult::isValid);
        Stream<Move> validMoves = findValidMoves();
        Stream<MoveResult> validResults = validMoves.map(Move::perform).filter(MoveResult::isValid);
        Stream<Building> validNotProhibitedResults = validResults.map(r -> (Building) r.building)
                .filter(b -> !prohibited.contains(b));
        return validNotProhibitedResults;
    }

    private static final char[] _charmap = "PXRST".toCharArray();

    private static char getElementSymbol(int elementIndex) {
        return _charmap[elementIndex];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        for (int floor = numFloors - 1; floor >= 0; floor--) {
            sb.append(String.format("%2d %2s ", floor + 1, floor == elevator ? "E" : "."));
            for (int i = 0; i < elements.size(); i++) {
                Element e = elements.get(i);
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

    public MyBuilding move(Direction direction, int element, Kind kind) {
        return move(direction, ImmutableSet.of(new Target(element, kind)));
    }

    public MyBuilding move(Direction direction, Set<Target> targets) {
        Move move = new Move(this, direction, targets);
        MoveResult result = move.perform();
        if (!result.isValid()) {
            throw new InvalidMoveException(result.reason);
        }
        return result.building;
    }

    public long countMaxPossibleStates() {
        long n = numFloors;
        for (int i = 0; i < elements.size(); i++) {
            n = LongMath.checkedMultiply(n, numFloors * numFloors);
        }
        return n;
    }
}
