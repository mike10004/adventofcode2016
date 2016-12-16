package aoc2016day11;

import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static aoc2016day11.Element.P;
import static aoc2016day11.Element.R;
import static aoc2016day11.Element.S;
import static aoc2016day11.Element.T;
import static aoc2016day11.Element.X;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public enum Item {

    PG(P, Kind.generator),
    PM(P, Kind.microchip),
    XG(X, Kind.generator),
    XM(X, Kind.microchip),
    RG(R, Kind.generator),
    RM(R, Kind.microchip),
    SG(S, Kind.generator),
    SM(S, Kind.microchip),
    TG(T, Kind.generator),
    TM(T, Kind.microchip);

    public final Kind kind;
    public final Element element;
    public final int placement;

    Item(Element element, Kind kind) {
        this.kind = Objects.requireNonNull(kind);
        this.element = Objects.requireNonNull(element);
        this.placement = ordinal();
    }

    public static Item fromCode(String code) {
        return valueOf(code);
    }

    private static final Item[] values = values();

    public static int getNumItems() {
        return values.length;
    }

    public static Stream<Item> streamItems() {
        return Stream.of(values);
    }

    public static Item of(Kind kind, Element element) {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(element, "element");
        return values[element.ordinal() * 2 + kind.ordinal()];
    }

    public static ImmutableSet<Item> removeFrom(Set<Item> subset, Set<Item> superset) {
        ImmutableSet.Builder<Item> filtered = ImmutableSet.builder();
        int n = 0;
        for (Item other : superset) {
            if (!subset.contains(other)) {
                filtered.add(other);
                n++;
            }
        }
        checkArgument(n == (superset.size() - subset.size()), "unexpected result set size %s should be %s - %s", n, superset.size(), subset.size());
        return filtered.build();
    }

    public static Item microchip(Element element) {
        return of(Kind.microchip, element);
    }

    public static Item generator(Element element) {
        return of(Kind.generator, element);
    }

    public String toString() {
        return element.symbol + kind.symbol;
    }

    public boolean isMicrochip() {
        return kind == Kind.microchip;
    }

    public boolean isGenerator() {
        return kind == Kind.generator;
    }

    public static boolean areSafe(Set<Item> itemList) {
        if (itemList.isEmpty() || itemList.size() == 1) {
            return true;
        }
        Item[] items = itemList.toArray(new Item[itemList.size()]);
        for (int i = 0; i < items.length; i++) {
            if (!isProtectedFromOthers(items, i)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasGenerator(Collection<Item> items, Element element) {
        for (Item item : items) {
            if (item.isGenerator() && element == item.element) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasGenerator(Item[] items, Element element) {
        return has(items, Kind.generator, element);
    }

    private static boolean has(Item[] items, Kind kind, Element element) {
        for (Item item : items) {
            if (item.kind == kind && element == item.element) {
                return true;
            }

        }
        return false;
    }
    private static boolean isProtectedFromOthers(Collection<Item> items, Item q) {
//        return  q.isGenerator() || hasGenerator(items, q.element);
        throw new UnsupportedOperationException();
    }

    private static boolean hasAnyOtherGenerator(Item[] items, Element element) {
        for (Item item : items) {
            if (item.isGenerator() && element != item.element) {
                return true;
            }
        }
        return false;
    }

    private static boolean isProtectedFromOthers(Item[] items, int k) {
        Item q = items[k];
        return q.isGenerator() || (!hasAnyOtherGenerator(items, q.element) || hasGenerator(items, q.element));
    }

    static @Nullable Item maybeFindItem(Stream<Item> items, Kind kind, Element element) {
        return items.filter(x -> x.kind == kind && x.element == element)
                .findFirst().orElse(null);
    }

    /**
     * Returns all unique pairings of the items in the given list. Every
     * list in the returned stream has length 2.
     * @return all pairs of the given items
     */
    public static ImmutableSet<ImmutableSet<Item>> pairs(ImmutableSet<Item> all) {
        if (all.size() < 2) {
            return ImmutableSet.of();
        }
        List<Item> copy = new ArrayList<>(all);
        if (all.size() == 2) {
            return ImmutableSet.of((all));
        }
        ImmutableSet.Builder<ImmutableSet<Item>> pairs = ImmutableSet.builder();
        for (int i = 0; i < copy.size(); i++) {
            for (int j = i + 1; j < copy.size(); j++) {
                pairs.add(ImmutableSet.of(copy.get(i), copy.get(j)));
            }
        }
        return pairs.build();
    }
}
