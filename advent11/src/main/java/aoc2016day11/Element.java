package aoc2016day11;

import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

class Element {

    public final int microchip;
    public final int generator;

    public Element(int microchip, int generator) {
        this.microchip = microchip;
        this.generator = generator;
    }

    public int get(Kind kind) {
        return checkNotNull(kind) == Kind.G ? generator : microchip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Element element = (Element) o;
        if (microchip != element.microchip) return false;
        return generator == element.generator;
    }

    @Override
    public int hashCode() {
        int result = microchip;
        result = 31 * result + generator;
        return result;
    }

    public String toString() {
        return String.format("(M=%d, G=%d)", microchip, generator);
    }

    public boolean isBothOn(int floor) {
        return microchip == floor && generator == floor;
    }
}
