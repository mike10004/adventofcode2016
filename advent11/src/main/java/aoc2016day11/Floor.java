package aoc2016day11;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Floor {

    public final List<Item> items;

    Floor(Stream<Item> items) {
        this(items.collect(Collectors.toList()));
    }

    public Floor(List<Item> items) {
        this.items = Collections.unmodifiableList(items);
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
        return toString(Item.placements.get());
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

}
