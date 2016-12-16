package aoc2016day11;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.Assert.*;

public class ItemTest {

    private static Item c(String code) {
        return Item.fromCode(code);
    }

    private List<Item> allItems() {
        List<Item> items = new ArrayList<>(Element.VALUES.size() * 2);
        for (Element element : Element.VALUES) {
            items.add(Item.microchip(element));
            items.add(Item.generator(element));
        }
        return items;
    }

    @Test
    public void of() {
        for (Element element : Element.VALUES) {
            for (Kind kind : Kind.values()) {
                Item item = Item.of(kind, element);
                assertEquals("kind", kind, item.kind);
                assertEquals("element", element, item.element);
            }
        }
    }

    @Test
    public void isSafe_symmetric() {
        List<Item> items = allItems();
        for (int i = 0; i < items.size(); i++) {
            Item a = items.get(i);
            for (int j = 0; j < items.size(); j++) {
                if (i != j) {
                    Item b = items.get(j);
                    boolean ab = new Safety(a).isSafeWith(b);
                    boolean ba = new Safety(b).isSafeWith(a);
                    assertEquals("symmetric", ab, ba);
                }
            }
        }

    }

    @Test
    public void areSafe() throws Exception {
        // [LG, HG, HM]
        boolean safe = Item.areSafe(Stream.of(Item.generator(Element.R),
                Item.generator(Element.P), Item.microchip(Element.P)).collect(Collectors.toSet()));
        assertTrue("[LG, HG, HM]", safe);
    }

    @Test
    public void isSafe() throws Exception {

        assertTrue("microchip with one other microchip", Safety.microchip(Element.P)
                .isSafeWith(Item.microchip(Element.R)));
        assertTrue("microchip with two other microchips", Safety.microchip(Element.P)
                .isSafeWith(Stream.of(Element.R, Element.S).map(Item::microchip)));
        assertTrue("generator with one other generator", Safety.generator(Element.P)
                .isSafeWith(Item.generator(Element.X)));
        assertTrue("generator with two other generators", Safety.generator(Element.P)
                .isSafeWith(Stream.of(Element.X, Element.R).map(Item::generator)));
        assertTrue("generator with its microchip", Safety.generator(Element.S)
                .isSafeWith(Item.microchip(Element.S)));
        assertTrue("generator with its microchip and another generator", Safety.generator(Element.S)
                .isSafeWith(Stream.of(Item.microchip(Element.S), Item.generator(Element.T))));
        assertTrue("microchip with its generator", Safety.microchip(Element.P)
                .isSafeWith(Item.generator(Element.P)));
        assertTrue("microchip with its generator and another generator", Safety.microchip(Element.P)
                .isSafeWith(Stream.of(Item.generator(Element.P), Item.generator(Element.R))));
        assertTrue("two microchips with their generators", Safety.microchip(Element.P)
                .isSafeWith(Stream.of(Item.generator(Element.P), Item.microchip(Element.R),
                        Item.generator(Element.R))));
        assertTrue("two microchips with their generators and another generator", Safety.microchip(Element.P)
                .isSafeWith(Stream.of(Item.generator(Element.P), Item.microchip(Element.R),
                        Item.generator(Element.R), Item.generator(Element.S))));
        assertTrue("LG safe with HG, HM (generator with another generator and its microchip)", Safety.generator(Element.R)
                .isSafeWith(Stream.of(Item.generator(Element.P), Item.microchip(Element.P))));

        assertFalse("microchip with other generator", Safety.microchip(Element.P)
                .isSafeWith(Item.generator(Element.R)));
        assertFalse("microchip with two other generators", Safety.microchip(Element.P)
                .isSafeWith(Stream.of(Item.generator(Element.S), Item.generator(Element.R))));
        assertFalse("generator with other microchip", Safety.generator(Element.P)
                .isSafeWith(Item.microchip(Element.R)));
        assertFalse("two microchips with their generators and another microchip", Safety.microchip(Element.P)
                .isSafeWith(Stream.of(Item.generator(Element.P), Item.microchip(Element.R),
                                      Item.generator(Element.R), Item.microchip(Element.S))));


    }

    private static class Safety {

        public static Safety microchip(Element element) {
            return new Safety(Item.microchip(element));
        }

        public static Safety generator(Element element) {
            return new Safety(Item.generator(element));
        }

        private final Item item;

        private Safety(Item item) {
            this.item = item;
        }
        public boolean isSafeWith(Item other) {
            checkArgument(!equals(other), "checking safety on self: %s", this);
            return isSafeWith(Collections.singletonList(other));
        }


        public boolean isSafeWith(Stream<Item> itemList) {
            return isSafeWith(itemList.collect(Collectors.toList()));
        }

        public boolean isSafeWith(Collection<Item> itemList) {
            return Item.areSafe(ImmutableSet.copyOf(Lists.asList(item, new ArrayList<>(itemList).toArray(new Item[0]))));
        }
    }
}