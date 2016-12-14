package aoc2016day11;

import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Item {

    private static final AtomicInteger placements = new AtomicInteger(0);

    public final Kind kind;
    public final Element element;
    public final int placement;
    private final int hashCode;

    private Item(Kind kind, Element element, int hashCode) {
        this.kind = Objects.requireNonNull(kind);
        this.element = Objects.requireNonNull(element);
        this.placement = placements.getAndIncrement();
        this.hashCode = hashCode;
    }

    private static Item[] microchips = new Item[Element.VALUES.size()];
    private static Item[] generators = new Item[Element.VALUES.size()];

    static {
        for (int i = 0; i < Element.VALUES.size(); i++) {
            Element element = Element.VALUES.get(i);
            Args.checkState(element.ordinal() == i);
            microchips[i] = new Item(Kind.microchip, element, i + 1);
            generators[i] = new Item(Kind.generator, element, Element.VALUES.size() + i + 1);
        }
    }

    public static int getNumItems() {
        return microchips.length + generators.length;
    }

    public static Stream<Item> streamItems() {
        return Stream.concat(Stream.of(microchips), Stream.of(generators));
    }

    public static Stream<Item> forElements(Set<Element> elements) {
        Stream<Item> items = Stream.concat(Stream.of(microchips), Stream.of(generators));
        return items.filter(x -> elements.contains(x.element));
    }

    public static Item of(Kind kind, Element element) {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(element, "element");
        int index = element.ordinal();
        return kind == Kind.generator ? generators[index] : microchips[index];
    }

    public static void removeOrDie(Item item, Collection<Item> items) {
        boolean removed = items.remove(item);
        if (!removed) {
            throw new IllegalArgumentException("tried to remove item " + item + " not in list " + items);
        }
    }

    public static Item microchip(Element element) {
        return of(Kind.microchip, element);
    }

    public static Item generator(Element element) {
        return of(Kind.generator, element);
    }

    @Override
    public int hashCode() {
        return hashCode;
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

    public static boolean areSafe(Collection<Item> itemList) {
        for (Item item : itemList) {
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

    public boolean isSafeWith(Collection<Item> itemList) {
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
    public static Stream<ImmutableSet<Item>> pairs(Set<Item> all) {
        if (all.size() < 2) {
            return Stream.empty();
        }
        List<Item> copy = new ArrayList<>(all);
        if (all.size() == 2) {
            return Stream.of(ImmutableSet.copyOf(all));
        }
        List<ImmutableSet<Item>> pairs = new ArrayList<>();
        for (int i = 0; i < copy.size(); i++) {
            for (int j = i + 1; j < copy.size(); j++) {
                pairs.add(ImmutableSet.of(copy.get(i), copy.get(j)));
            }
        }
        return pairs.stream();
    }
}
