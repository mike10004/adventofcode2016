package aoc2016day11;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Item {

    static final AtomicInteger placements = new AtomicInteger(0);

    public final Kind kind;
    public final Element element;
    public final int placement;

    public Item(Kind kind, Element element) {
        this.kind = Objects.requireNonNull(kind);
        this.element = Objects.requireNonNull(element);
        this.placement = placements.getAndIncrement();
    }

    public static void removeOrDie(Item item, Collection<Item> items) {
        boolean removed = items.remove(item);
        if (!removed) {
            throw new IllegalArgumentException("tried to remove item " + item + " not in list " + items);
        }
    }

    public static Item microchip(Element element) {
        return new Item(Kind.microchip, element);
    }

    public static Item generator(Element element) {
        return new Item(Kind.generator, element);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        if (kind != item.kind) return false;
        return element == item.element;
    }

    @Override
    public int hashCode() {
        int result = kind.hashCode();
        result = 31 * result + element.hashCode();
        return result;
    }

    public String toString() {
        return element.symbol + kind.symbol;
    }

    public boolean isSafeWith(Item other) {
        Args.check(!equals(other), "checking safety on self: %s", this);
        return isSafeWith(Collections.singletonList(other));
    }

    public boolean isMicrochip() {
        return kind == Kind.microchip;
    }

    public boolean isGenerator() {
        return kind == Kind.generator;
    }


    public static boolean areSafe(Stream<Item> items) {
        List<Item> itemList = items.collect(Collectors.toList());
        return areSafe(itemList);
    }

    public static boolean areSafe(List<Item> itemList) {
        for (int i = 0; i < itemList.size(); i++) {
            Item item = itemList.get(i);
            Stream<Item> others = itemList.stream().filter(Predicate.isEqual(item).negate());
            if (!item.isSafeWith(others)) {
                return false;
            }
        }
        return true;
    }

    public boolean isSafeWith(Stream<Item> items) {
        List<Item> itemList = items.collect(Collectors.toList());
        return isSafeWith(itemList);
    }

    static @Nullable Item maybeFindItem(Stream<Item> items, Kind kind, Element element) {
        return items.filter(x -> x.kind == kind && x.element == element).findFirst().orElse(null);
    }

    static boolean has(Stream<Item> items, Kind kind, Element element) {
        return maybeFindItem(items, kind, element) != null;
    }

    public boolean isSafeWith(List<Item> itemList) {
        if (kind == Kind.generator) {
            Stream<Item> microchipsOfOtherElements = itemList.stream()
                    .filter(Item::isMicrochip).filter(m -> m.element != element);
            return microchipsOfOtherElements.allMatch(m -> has(itemList.stream(), Kind.generator, m.element));
        } else {
            assert kind == Kind.microchip;
            return itemList.stream().filter(Item::isGenerator).anyMatch(g -> g.element == element)
                || itemList.stream().filter(Item::isGenerator).noneMatch(g -> g.element != element);
        }
    }

    /**
     * Returns all unique pairings of the items in the given list. Every
     * list in the returned stream has length 2.
     * @return all pairs of the given items
     */
    public static Stream<List<Item>> pairs(List<Item> all) {
        if (all.size() < 2) {
            return Stream.empty();
        }
        List<Item> copy = new ArrayList<>(all);
        if (all.size() == 2) {
            return Stream.of(copy);
        }
        List<List<Item>> pairs = new ArrayList<>();
        for (int i = 0; i < copy.size(); i++) {
            for (int j = i + 1; j < copy.size(); j++) {
                pairs.add(Arrays.asList(copy.get(i), copy.get(j)));
            }
        }
        return pairs.stream();
    }
}
