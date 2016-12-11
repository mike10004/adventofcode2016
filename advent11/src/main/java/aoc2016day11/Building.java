package aoc2016day11;

import javax.annotation.Nullable;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Building {

    public static final AtomicLong counter = new AtomicLong(0L);
    private final int elevatorPosition;
    public final List<Floor> floors;
    public final int numMoves;

    Building(int elevatorPosition, List<Floor> floors) {
        this(elevatorPosition, floors, 0);
    }

    Building(int elevatorPosition, List<Floor> floors, int numMoves) {
        this.numMoves = numMoves;
        this.floors = Collections.unmodifiableList(new ArrayList<>(floors));
        Args.check(elevatorPosition >= 0 && elevatorPosition < this.floors.size(), "position %s invalid for %s-floor building", elevatorPosition, this.floors.size());
        Args.check(!this.floors.isEmpty(), "building cannot be empty");
        this.elevatorPosition = elevatorPosition;
        counter.incrementAndGet();
    }

    public Building onFirstFloor(List<Floor> floors) {
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

    public static class MoveResult {
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


    public @Nullable Building moveElevator(Direction direction, List<Item> items) {
        return move(direction, items).building;
    }

    Building moveChecked(Direction direction, List<Item> items) {
        MoveResult result = move(direction, items);
        if (!result.isValid()) {
            throw new IllegalStateException(result.reason);
        }
        return result.building;
    }

    private MoveResult move(Direction direction, List<Item> items) {
        Args.check(items.size() == 1 || items.size() == 2, "can't move %s items ", items.size());
        Args.check(direction != null, "direction must be nonnull");
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
                newBuildingFloors.set(i, new Floor(currentItems));
            } else if (i == newPos) {
                newBuildingFloors.set(i, new Floor(nextItems));
            } else {
                newBuildingFloors.set(i, floors.get(i));
            }
        }
        return MoveResult.valid(new Building(newPos, newBuildingFloors, numMoves + 1));
    }


    @SuppressWarnings("SameParameterValue")
    private static <E> ArrayList<E> repeat(E element, int count) {
        ArrayList<E> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(element);
        }
        return list;
    }

    public void dump(PrintStream out) {
        int maxPlacement = items().map(x -> x.placement).max(Integer::compare).orElse(0);
        for (int i = floors.size() - 1; i >= 0; i--) {
            String label = String.valueOf(i + 1);
            out.format("F%s %s %s%n", label, i == elevatorPosition ? "E " : ". ", floors.get(i).toString(maxPlacement));
        }
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
}
