package aoc2016day11;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Floor {

    public final ImmutableSet<Item> items;

    private Floor(Stream<Item> items) {
        this(items.collect(Collectors.toSet()));
    }

    private Floor(Iterable<Item> items) {
        this(ImmutableSet.copyOf(items));
    }

    private Floor(ImmutableSet<Item> items) {
        this.items = checkNotNull(items);
    }

    public static abstract class Factory {

        public abstract Floor get(Collection<Item> items);

        public Floor empty() {
            return get(ImmutableSet.of());
        }

        private static class Holder {
            private static final Factory instance = new NewEachTimeFactory(); //new PrefabFactory();
        }

        public static Factory getInstance() {
            return Holder.instance;
        }
    }

    private static class NewEachTimeFactory extends Factory {

        private final Floor empty = new Floor(ImmutableSet.of());

        @Override
        public Floor get(Collection<Item> items) {
            return new Floor(items);
        }

        @Override
        public Floor empty() {
            return empty;
        }
    }

    private static class PrefabFactory extends Factory {

        private final ImmutableBiMap<Set<Item>, Floor> floors;

        public PrefabFactory() {
            floors = fabricate();
        }

        private static ImmutableBiMap<Set<Item>, Floor> fabricate() {
            ImmutableBiMap.Builder<Set<Item>, Floor> b = ImmutableBiMap.builder();
            Set<Item> allItems = Item.streamItems().collect(Collectors.toSet());
            Set<Set<Item>> itemsPowerSet = Sets.powerSet(allItems); // 7 elements => 16384 different sets
            for (Set<Item> set : itemsPowerSet) {
                ImmutableSet<Item> immSet = ImmutableSet.copyOf(set);
                Floor f = new Floor(immSet);
                b.put(immSet, f);
            }
            return b.build();
        }

        @Override
        public Floor get(Collection<Item> items) {
            return floors.get(ImmutableSet.copyOf(items));
        }
    }

    private Item findItemByPlacement(int placement) {
        for (Item item : items) {
            if (item.placement == placement) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return toString(Item.getNumItems());
    }

    public String toString(int maxPlacement) {
        StringBuilder b = new StringBuilder(128);
        int numPrinted = 0;
        for (int pl = 0; pl <= maxPlacement; pl++) {
            if (pl > 0) {
                b.append(' ');
            }
            Item item = findItemByPlacement(pl);
            b.append(item == null ? ". " : item);
            numPrinted += (item == null ? 0 : 1);
        }
        if (numPrinted != items.size()) {
            throw new IllegalStateException("failed to represent all items on floor: " + b + " inconsistent with " + items);
        }
        return b.toString();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Floor
                && toSet(items).equals(toSet(((Floor)other).items));
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }

    private static <E> Set<E> toSet(Collection<E> c) {
        return c instanceof Set ? (Set<E>) c : new HashSet<>(c);
    }
}
