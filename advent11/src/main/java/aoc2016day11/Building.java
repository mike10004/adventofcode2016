package aoc2016day11;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;

import javax.annotation.Nullable;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

public final class Building {

    public static final AtomicLong counter = new AtomicLong(0L);
    private final int elevatorPosition;
    private final Floor.Factory floorFactory;
    public final ImmutableList<Floor> floors;
    public final int numMoves;

    public Building(int elevatorPosition, List<Floor> floors) {
        this(elevatorPosition, floors, 0);
    }

    Building(int elevatorPosition, List<Floor> floors, int numMoves) {
        this.numMoves = numMoves;
        floorFactory = Floor.Factory.getInstance();
        this.floors = ImmutableList.copyOf(floors);
        checkArgument(elevatorPosition >= 0 && elevatorPosition < this.floors.size(), "position %s invalid for %s-floor building", elevatorPosition, this.floors.size());
        checkArgument(!this.floors.isEmpty(), "building cannot be empty");
        this.elevatorPosition = elevatorPosition;
        if (floors.get(elevatorPosition).isEmpty()) {
            throw new IllegalArgumentException("can't start on floor with no items; elevatorPosition = " + elevatorPosition);
        }
        counter.incrementAndGet();
    }

    public static Building onFirstFloor(List<Floor> floors) {
        return new Building(0, floors);
    }

    private static <E> Predicate<E> in(Collection<E> collection) {
        return x -> !collection.contains(x);
    }

    private static <E> Set<E> concat(Collection<E> first, Collection<E> other) {
        ImmutableSet.Builder<E> b = ImmutableSet.builder();
        b.addAll(first);
        b.addAll(other);
        return b.build();
    }

    public boolean isEverythingAtTopFloor() {
        return floors.size() <= 1 || floors.subList(0, floors.size() - 1).stream().allMatch(Floor::isEmpty);
    }

    public boolean isElevatorAtTop() {
        return elevatorPosition == floors.size() - 1;
    }

    public boolean isWin() {
        return isEverythingAtTopFloor() && isElevatorAtTop();
    }

    public static final class MoveResult {
        final Building building;
        final String reason;

        public static MoveResult invalid(String why) {
            return new MoveResult(null, why);
        }

        public static MoveResult valid(Building building) {
            return new MoveResult(Objects.requireNonNull(building), "ok");
        }

        private MoveResult(Building building, String reason) {
            this.building = building;
            this.reason = Objects.requireNonNull(reason);
        }

        public boolean isValid() {
            return building != null;
        }
    }


    public @Nullable Building moveElevator(Direction direction, Set<Item> items) {
        return move(direction, items).building;
    }

    Building moveChecked(Direction direction, Set<Item> items) {
        MoveResult result = move(direction, items);
        if (!result.isValid()) {
            throw new IllegalStateException(result.reason);
        }
        return result.building;
    }

    private static <E> Set<E> toSet(Collection<E> collection) {
        return collection instanceof Set ? (Set<E>) collection : ImmutableSet.copyOf(collection);
    }

    private MoveResult move(Direction direction, Set<Item> itemCollection) {
        checkArgument(itemCollection.size() == 1 || itemCollection.size() == 2, "can't move %s items ", itemCollection.size());
        checkArgument(direction != null, "direction must be nonnull");
        Set<Item> items = toSet(itemCollection);
        checkArgument(items.size() == 1 || Item.areSafe(items), "item combo unsafe: %s", items);
        int newPos = elevatorPosition + direction.offset();
        if (newPos < 0 || newPos >= floors.size()) {
            return MoveResult.invalid(direction + " floor out of range: " + newPos);
        }
        Floor current = floors.get(elevatorPosition);
        Set<Item> currentItems = Item.removeFrom(items, current.items);
        if (!Item.areSafe(currentItems)) {
            return MoveResult.invalid("would leave unsafe combo of items on current floor " + elevatorPosition + ": " + currentItems);
        }
        Floor next = floors.get(newPos);
        Set<Item> itemsOnNextFloor = concat(next.items, items);
        if (!Item.areSafe(itemsOnNextFloor)) {
            return MoveResult.invalid("would create unsafe combo of items on next floor " + newPos + ": " + itemsOnNextFloor);
        }
        List<Floor> newBuildingFloors = repeat(null, floors.size());
        for (int i = 0; i < floors.size(); i++) {
            if (i == elevatorPosition) {
                newBuildingFloors.set(i, floorFactory.get(currentItems));
            } else if (i == newPos) {
                newBuildingFloors.set(i, floorFactory.get(itemsOnNextFloor));
            } else {
                newBuildingFloors.set(i, floors.get(i));
            }
        }
        return MoveResult.valid(new Building(newPos, newBuildingFloors, numMoves + 1));
    }

    static <E> boolean notSameItems(List<E> c1, List<E> c2) {
        if (c1.size() != c2.size()) {
            return true;
        }
        if (c1.isEmpty() && c2.isEmpty()) {
            return false;
        }
        for (E item : c1) {
            if (!c2.contains(item)) {
                return true;
            }
        }
        for (E item : c2) {
            if (!c1.contains(item)) {
                return true;
            }
        }
        return false;
    }
//
//    private Stream<Building> toMoves(Direction direction, Stream<ImmutableSet<Item>> carryables, Collection<Building> prohibited) {
//        checkArgument(direction != null, "direction must be nonnull");
//        return carryables
//                 .map(items -> move(direction, items))
//                .filter(MoveResult::isValid)
//                .map(result -> result.building)
//                .filter(next -> !prohibited.contains(next));
//    }

    public static class Move {
        public final Building from;
        public final Direction direction;
        public final ImmutableSet<Item> carrying;
        public Move(Building from, Direction direction, Collection<Item> carrying) {
            this.from = Objects.requireNonNull(from);
            this.direction = Objects.requireNonNull(direction);
            this.carrying = ImmutableSet.copyOf(carrying);
        }

        public String toString() {
            return direction.toString().toLowerCase() + carrying.toString();
        }

        public MoveResult perform() {
            MoveResult result = from.move(direction, carrying);
            return result;
        }
    }

    private List<ImmutableSet<Item>> listCarryables() {
        Floor current = floors.get(elevatorPosition);
        Stream<ImmutableSet<Item>> pairs = Item.pairs(current.items)
                .stream().filter(Item::areSafe);
        Stream<ImmutableSet<Item>> singletons = current.items.stream().map(ImmutableSet::of);
        List<ImmutableSet<Item>> carryables = Stream.concat(singletons, pairs)
                .collect(Collectors.toList());
        return carryables;
    }

    public Stream<Move> listValidMoves(Collection<Building> prohibited) {
        List<ImmutableSet<Item>> carryables = listCarryables();
        Predicate<Move> valid = m -> {
            MoveResult result = move(m.direction, m.carrying);
            return result.isValid() && !prohibited.contains(result.building);
        };
        Stream<Move> upMoves = carryables.stream().map(c -> new Move(this, Direction.UP, c)).filter(valid);
        Stream<Move> downMoves = carryables.stream().map(c -> new Move(this, Direction.DOWN, c)).filter(valid);
        return Stream.concat(upMoves, downMoves);
    }

    private static boolean isSameElement(Set<Item> items) {
        if (items.isEmpty() || items.size() == 1) {
            return true;
        }
        Iterator<Item> it = items.iterator();
        Element element = it.next().element;
        while (it.hasNext()) {
            if (element != it.next().element) {
                return false;
            }
        }
        return true;
    }

    private boolean isMoveChipAndGenOfSameElement(Move move) {
        Set<Item> items = move.carrying;
        return items.size() == 2 && isSameElement(items);
    }

    private static <E, C> ImmutableSetMultimap<C, E> partition(Set<E> superset, Function<E, C> classifier) {
        ImmutableSetMultimap.Builder<C, E> b = ImmutableSetMultimap.builder();
        for (E element : superset) {
            C eqClass = classifier.apply(element);
            b.put(eqClass, element);
        }
        return b.build();
    }

    private abstract static class MoveClassifier implements Predicate<Move> {

        private final int numItems;

        public MoveClassifier(int numItems) {
            this.numItems = numItems;
        }

        @Override
        public boolean test(Move move) {
            if (move.carrying.size() != numItems) {
                return false;
            }
            Floor destination = move.from.floors.get(move.from.elevatorPosition + move.direction.offset());
            return test(move.carrying, destination);
        }

        protected abstract boolean test(Set<Item> items, Floor destination);

        protected boolean test(Item item, Kind kind, Floor destination, boolean hasCounterpart) {
            return item.kind == kind && hasCounterpart == destination.items.contains(Item.of(kind.other(), item.element));
        }
    }

    private static abstract class OneMoveClassifier extends MoveClassifier {

        private final Kind kind;
        private final boolean hasCounterpart;

        public OneMoveClassifier(Kind kind, boolean hasCounterpart) {
            super(1);
            this.kind = kind;
            this.hasCounterpart = hasCounterpart;
        }

        @Override
        protected boolean test(Set<Item> items, Floor destination) {
            return test(items.iterator().next(), destination);
        }

        protected boolean test(Item item, Floor destination) {
            return test(item, kind, destination, hasCounterpart);
        }
    }

    private static abstract class TwoMoveClassifier extends MoveClassifier {

        public TwoMoveClassifier() {
            super(2);
        }

        @Override
        protected boolean test(Set<Item> items, Floor destination) {
            Iterator<Item> it = items.iterator();
            Item a = it.next(), b = it.next();
            if (!isCorrectKinds(a, b)) {
                return false;
            }
            return evaluateFirst(a, b) && evaluateSecond(a, b);
        }

        protected abstract boolean isCorrectKinds(Item a, Item b);
        protected abstract boolean evaluateFirst(Item a, Item b, Floor destination);
        protected abstract boolean evaluateSecond(Item a, Item b);
    }

    private static abstract class HomoTwoMoveClassifier extends TwoMoveClassifier {

        private final Kind kind;

        protected HomoTwoMoveClassifier(Kind kind) {
            this.kind = kind;
        }

        @Override
        protected boolean isCorrectKinds(Item a, Item b) {
            return a.kind == kind && b.kind == kind;
        }
    }

    private static abstract class HeteroTwoMoveClassifier extends TwoMoveClassifier {
        @Override
        protected boolean isCorrectKinds(Item a, Item b) {
            return a.kind != b.kind;
        }

        protected abstract boolean evaluateMicrochip(Item )
    }

    private enum MoveClass {
        unknown,
        chipAndGenOfSameElement,
        MoveMicrochipAloneToFloorWithItsGenerator,
        MoveMicrochipAloneToFloorWithoutItsGenerator,
        MoveTwoMicrochipsToFloorWithBothTheirGenerators,
        MoveTwoMicrochipsToFloorWithoutTheirGenerators,
        MoveGeneratorAloneToFloorWithItsMicrochip,
        MoveGeneratorAloneToFloorWithoutItsMicrochip,
        MoveTwoGeneratorsToFloorWithTheirMicrochips,
        MoveTwoGeneratorsToFloorWithoutTheirMicrochips;

        public static MoveClass[] known = { chipAndGenOfSameElement,
                MoveMicrochipAloneToFloorWithItsGenerator,
                MoveMicrochipAloneToFloorWithoutItsGenerator,
                MoveTwoMicrochipsToFloorWithBothTheirGenerators,
                MoveTwoMicrochipsToFloorWithoutTheirGenerators,
                MoveGeneratorAloneToFloorWithItsMicrochip,
                MoveGeneratorAloneToFloorWithoutItsMicrochip,
                MoveTwoGeneratorsToFloorWithTheirMicrochips,
                MoveTwoGeneratorsToFloorWithoutTheirMicrochips
        };
    }

    private boolean floorHas(int floor, Kind kind, Element element) {
        return floors.get(floor).items.contains(Item.of(kind, element));
    }

    private boolean isMoveItemAloneToFloorWithCounterpart(Move move, Kind kind, boolean hasGenerator) {
        if (move.carrying.size() == 1) {
            Item item = move.carrying.iterator().next();
            if (item.kind == kind) {
                return hasGenerator == floorHas(elevatorPosition + move.direction.offset(), kind.other(), item.element);
            }
        }
        return false;
    }

    private boolean isMoveTwoItemsToFloorWithTheirCounterparts(Move move, Kind kind, boolean hasCounterpart) {
        if (move.carrying.size() == 2) {
            Iterator<Item> items = move.carrying.iterator();
            Item a = items.next(), b = items.next();
            return a.kind == kind && b.kind == kind
                    && hasCounterpart == floorHas(elevatorPosition + move.direction.offset(), kind.other(), a.element)
                    && hasCounterpart == floorHas(elevatorPosition + move.direction.offset(), kind.other(), b.element);
        }
        return false;
    }

    private Function<Move, MoveClass> partitionClassifier() {
        return new Function<Move, MoveClass>() {
            @Override
            public MoveClass apply(Move move) {
                if (isMoveChipAndGenOfSameElement(move)) {
                    return MoveClass.chipAndGenOfSameElement;
                }
                if (isMoveItemAloneToFloorWithCounterpart(move, Kind.microchip, true)) {
                    return MoveClass.MoveMicrochipAloneToFloorWithItsGenerator;
                }
                if (isMoveItemAloneToFloorWithCounterpart(move, Kind.microchip, false)) {
                    return MoveClass.MoveMicrochipAloneToFloorWithoutItsGenerator;
                }
                if (isMoveItemAloneToFloorWithCounterpart(move, Kind.generator, true)) {
                    return MoveClass.MoveGeneratorAloneToFloorWithItsMicrochip;
                }
                if (isMoveItemAloneToFloorWithCounterpart(move, Kind.generator, false)) {
                    return MoveClass.MoveGeneratorAloneToFloorWithoutItsMicrochip;
                }
                if (isMoveTwoItemsToFloorWithTheirCounterparts(move, Kind.microchip, true)) {
                    return MoveClass.MoveTwoMicrochipsToFloorWithBothTheirGenerators;
                }
                if (isMoveTwoItemsToFloorWithTheirCounterparts(move, Kind.microchip, false)) {
                    return MoveClass.MoveTwoMicrochipsToFloorWithoutTheirGenerators;
                }
                if (isMoveTwoItemsToFloorWithTheirCounterparts(move, Kind.generator, true)) {
                    return MoveClass.MoveTwoGeneratorsToFloorWithTheirMicrochips;
                }
                if (isMoveTwoItemsToFloorWithTheirCounterparts(move, Kind.generator, false)) {
                    return MoveClass.MoveTwoGeneratorsToFloorWithoutTheirMicrochips;
                }
                return MoveClass.unknown;
            }
        };
    }

    public MoveStream findValidMovesExcept(Collection<Building> prohibited) {
        Set<Move> allValidMovesList = listValidMoves(prohibited).collect(Collectors.toSet());
        ImmutableSetMultimap<MoveClass, Move> mm = partition(allValidMovesList, partitionClassifier());
        Stream<Move> moveSt = mm.get(MoveClass.unknown).stream();
        for (MoveClass mc : MoveClass.known) {
            Set<Move> others = mm.get(mc);
            if (!others.isEmpty()) {
                moveSt = Stream.concat(moveSt, others.stream().limit(1));
            }
        }
        return new MoveStream(moveSt);
    }

    public class MoveStream extends DelegatingStream<Move> {

        public MoveStream(Stream<Move> inner) {
            super(inner);
        }

        public Stream<Building> perform() {
            return stream.map(m -> m.perform(Building.this)).filter(MoveResult::isValid).map(mr -> mr.building);
        }
    }

    private Stream<Move> filterEquivalenceClass(List<Move> moves, Predicate<Move> predicate) {
        if (moves.stream().filter(predicate).count() > 1) {
            Stream<Move> without = moves.stream().filter(predicate.negate());
            Stream<Move> only = moves.stream().filter(predicate);
            return Stream.concat(without, only.limit(1));
        } else {
            return moves.stream();
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static <E> ArrayList<E> repeat(E element, int count) {
        ArrayList<E> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(element);
        }
        return list;
    }

    public PrintStream dump(PrintStream out) {
        for (int i = floors.size() - 1; i >= 0; i--) {
            String label = String.valueOf(i + 1);
            out.format("F%s %s %s%n", label, i == elevatorPosition ? "E " : ". ", floors.get(i).toString());
        }
        return out;
    }

    /**
     *
     * @param moves
     * @param out
     */
    public static void dump(List<Building> moves, PrintStream out) {
        for (Building b : moves) {
            out.format("move %d:%n", b.numMoves);
            b.dump(out).println();
        }
    }

    public static Comparator<Building> moveNumberComparator() {
        return Comparator.comparingInt(o -> o.numMoves);
    }

    public static int count(Collection<Building> moves) {
        if (moves.isEmpty()) {
            return 0;
        }
        List<Building> ordered = new ArrayList<>(moves);
        ordered.sort(moveNumberComparator());
        for (int i = 1; i < ordered.size(); i++) {
            checkArgument(ordered.get(i).numMoves == ordered.get(i - 1).numMoves + 1, "moves not consecutive: %s", ordered);
        }

        return ordered.stream().max(moveNumberComparator())
                .orElseThrow(IllegalStateException::new).numMoves;
    }

    public Stream<Item> items() {
        return floors.stream().flatMap(f -> f.items.stream());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Building building = (Building) o;
        if (elevatorPosition != building.elevatorPosition) return false;
        return floors.equals(building.floors);
    }

    @Override
    public int hashCode() {
        int result = elevatorPosition;
        result = 31 * result + floors.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("Building{move=%d,numFloors=%d,numItems=%d}", numMoves, floors.size(), items().count());
    }
}
