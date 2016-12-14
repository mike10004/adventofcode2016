package aoc2016day11;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        Args.check(elevatorPosition >= 0 && elevatorPosition < this.floors.size(), "position %s invalid for %s-floor building", elevatorPosition, this.floors.size());
        Args.check(!this.floors.isEmpty(), "building cannot be empty");
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

    private static <E> List<E> concat(Collection<E> first, Collection<E> other) {
        List<E> catted = new ArrayList<>(first.size() + other.size());
        catted.addAll(first);
        catted.addAll(other);
        return catted;
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


    public @Nullable Building moveElevator(Direction direction, Collection<Item> items) {
        return move(direction, items).building;
    }

    Building moveChecked(Direction direction, Collection<Item> items) {
        MoveResult result = move(direction, items);
        if (!result.isValid()) {
            throw new IllegalStateException(result.reason);
        }
        return result.building;
    }

    private static <E> List<E> toList(Collection<E> collection) {
        return collection instanceof List ? (List<E>) collection : new ArrayList<>(collection);
    }

    private MoveResult move(Direction direction, Collection<Item> itemCollection) {
        Args.check(itemCollection.size() == 1 || itemCollection.size() == 2, "can't move %s items ", itemCollection.size());
        Args.check(direction != null, "direction must be nonnull");
        List<Item> items = toList(itemCollection);
        Args.check(items.size() == 1 || items.get(0).isSafeWith(items.get(1)), "item combo unsafe: %s", items);
        int newPos = elevatorPosition + direction.offset();
        if (newPos < 0 || newPos >= floors.size()) {
            return MoveResult.invalid(direction + " floor out of range: " + newPos);
        }
        Floor current = floors.get(elevatorPosition);
        List<Item> currentItems = new ArrayList<>(current.items);
        for (Item item : items) {
            Item.removeOrDie(item, currentItems);
        }
        if (!Item.areSafe(currentItems.stream())) {
            return MoveResult.invalid("would leave unsafe combo of items on current floor " + elevatorPosition + ": " + currentItems);
        }
        Floor next = floors.get(newPos);
        List<Item> nextItems = concat(next.items, items);
        if (!Item.areSafe(nextItems.stream())) {
            return MoveResult.invalid("would create unsafe combo of items on next floor " + newPos + ": " + nextItems);
        }
        List<Floor> newBuildingFloors = repeat(null, floors.size());
        for (int i = 0; i < floors.size(); i++) {
            if (i == elevatorPosition) {
                newBuildingFloors.set(i, floorFactory.get(currentItems));
            } else if (i == newPos) {
                newBuildingFloors.set(i, floorFactory.get(nextItems));
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

    private Stream<Building> toMoves(Direction direction, Stream<ImmutableSet<Item>> carryables, Collection<Building> prohibited) {
        Args.check(direction != null, "direction must be nonnull");
        return carryables
                 .map(items -> move(direction, items))
                .filter(MoveResult::isValid)
                .map(result -> result.building)
                .filter(next -> !prohibited.contains(next));
    }

    public static final class Move {
        public final Direction direction;
        public final ImmutableSet<Item> carrying;

        public Move(Direction direction, Collection<Item> carrying) {
            this.direction = Objects.requireNonNull(direction);
            this.carrying = ImmutableSet.copyOf(carrying);
        }

        public String toString() {
            return direction + " with " + carrying;
        }

        public MoveResult perform(Building from) {
            return from.move(direction, carrying);
        }
    }

    private List<ImmutableSet<Item>> listCarryables() {
        Floor current = floors.get(elevatorPosition);
        Stream<ImmutableSet<Item>> pairs = Item.pairs(current.items).filter(Item::areSafe);
        Stream<ImmutableSet<Item>> singletons = current.items.stream().map(ImmutableSet::of);
        List<ImmutableSet<Item>> carryables = Stream.concat(singletons, pairs)
                .collect(Collectors.toList());
        return carryables;
    }

    public Stream<Move> listValidMoves() {
        List<ImmutableSet<Item>> carryables = listCarryables();
        Predicate<Move> valid = m -> move(m.direction, m.carrying).isValid();
        Stream<Move> upMoves = carryables.stream().map(c -> new Move(Direction.UP, c)).filter(valid);
        Stream<Move> downMoves = carryables.stream().map(c -> new Move(Direction.DOWN, c)).filter(valid);
        return Stream.concat(upMoves, downMoves);
    }

    public Stream<Building> findValidMovesExcept(Collection<Building> prohibited) {
        List<ImmutableSet<Item>> carryables = listCarryables();
        Stream<Building> upMoves = toMoves(Direction.UP, carryables.stream(), prohibited);
        Stream<Building> downMoves = toMoves(Direction.DOWN, carryables.stream(), prohibited);
        Stream<Building> allMoves = Stream.concat(upMoves, downMoves);
        return allMoves;
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
        int maxPlacement = items().map(x -> x.placement).max(Integer::compare).orElse(0);
        for (int i = floors.size() - 1; i >= 0; i--) {
            String label = String.valueOf(i + 1);
            out.format("F%s %s %s%n", label, i == elevatorPosition ? "E " : ". ", floors.get(i).toString(maxPlacement));
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
        return new Comparator<Building>() {
            @Override
            public int compare(Building o1, Building o2) {
                return o1.numMoves - o2.numMoves;
            }
        };
    }

    public static int count(Collection<Building> moves) {
        if (moves.isEmpty()) {
            return 0;
        }
        List<Building> ordered = new ArrayList<>(moves);
        ordered.sort(moveNumberComparator());
        for (int i = 1; i < ordered.size(); i++) {
            Args.check(ordered.get(i).numMoves == ordered.get(i - 1).numMoves + 1, "moves not consecutive: %s", ordered);
        }
        return ordered.stream().max(moveNumberComparator()).get().numMoves;
//        if (ordered.get(0).numMoves == 0) {
//            return ordered.size() - 1;
//        } else {
//            throw new IllegalArgumentException("expected moves collection include numMoves=[0..n], not " + ordered);
//        }
    }

    public Stream<Item> items() {
        return floors.stream().flatMap(f -> f.items.stream());
    }

    public Item findItem(Kind kind, Element element) {
        @Nullable Item item = Item.maybeFindItem(items(), kind, element);
        if (item == null) {
            throw new NoSuchElementException(element.symbol + " " + kind.symbol);
        }
        return item;
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
