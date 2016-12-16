package aoc2016day11;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
    public void isSafe_symmetric() {
        List<Item> items = allItems();
        for (int i = 0; i < items.size(); i++) {
            Item a = items.get(i);
            for (int j = 0; j < items.size(); j++) {
                if (i != j) {
                    Item b = items.get(j);
                    assertEquals("symmetric", a.isSafeWith(b), b.isSafeWith(a));
                }
            }
        }

    }

    @Test
    public void areSafe() throws Exception {
        // [LG, HG, HM]
        boolean safe = Item.areSafe(Stream.of(Item.generator(Element.lithium), Item.generator(Element.hydrogen), Item.microchip(Element.hydrogen)));
        assertTrue("[LG, HG, HM]", safe);
    }

    @Test
    public void isSafe() throws Exception {

        assertTrue("microchip with one other microchip", Item.microchip(Element.hydrogen).isSafeWith(Item.microchip(Element.lithium)));
        assertTrue("microchip with two other microchips", Item.microchip(Element.hydrogen).isSafeWith(Stream.of(Element.lithium, Element.strontium).map(Item::microchip)));
        assertTrue("generator with one other generator", Item.generator(Element.hydrogen).isSafeWith(Item.generator(Element.promethium)));
        assertTrue("generator with two other generators", Item.generator(Element.hydrogen).isSafeWith(Stream.of(Element.promethium, Element.ruthenium).map(Item::generator)));
        assertTrue("generator with its microchip", Item.generator(Element.strontium).isSafeWith(Item.microchip(Element.strontium)));
        assertTrue("generator with its microchip and another generator", Item.generator(Element.strontium).isSafeWith(Stream.of(Item.microchip(Element.strontium), Item.generator(Element.thulium))));
        assertTrue("microchip with its generator", Item.microchip(Element.hydrogen).isSafeWith(Item.generator(Element.hydrogen)));
        assertTrue("microchip with its generator and another generator", Item.microchip(Element.hydrogen).isSafeWith(Stream.of(Item.generator(Element.hydrogen), Item.generator(Element.lithium))));
        assertTrue("two microchips with their generators", Item.microchip(Element.hydrogen).isSafeWith(Stream.of(Item.generator(Element.hydrogen), Item.microchip(Element.lithium), Item.generator(Element.lithium))));
        assertTrue("two microchips with their generators and another generator", Item.microchip(Element.hydrogen).isSafeWith(Stream.of(Item.generator(Element.hydrogen), Item.microchip(Element.lithium), Item.generator(Element.lithium), Item.generator(Element.strontium))));
        assertTrue("LG safe with HG, HM (generator with another generator and its microchip)", Item.generator(Element.lithium).isSafeWith(Stream.of(Item.generator(Element.hydrogen), Item.microchip(Element.hydrogen))));

        assertFalse("microchip with other generator", Item.microchip(Element.hydrogen).isSafeWith(Item.generator(Element.lithium)));
        assertFalse("microchip with two other generators", Item.microchip(Element.hydrogen).isSafeWith(Stream.of(Item.generator(Element.lithium), Item.generator(Element.ruthenium))));
        assertFalse("generator with other microchip", Item.generator(Element.hydrogen).isSafeWith(Item.microchip(Element.lithium)));
        assertTrue("two microchips with their generators and another microchip", Item.microchip(Element.hydrogen).isSafeWith(Stream.of(Item.generator(Element.hydrogen), Item.microchip(Element.lithium), Item.generator(Element.lithium), Item.microchip(Element.strontium))));


    }

}