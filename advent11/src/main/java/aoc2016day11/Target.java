package aoc2016day11;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

public class Target {

    public final int elementIndex;
    public final Kind kind;

    public Target(int elementIndex, Kind kind) {
        this.elementIndex = elementIndex;
        this.kind = checkNotNull(kind);
    }

//    public ImmutableList<Element> apply(Direction direction, ImmutableList<Element> elements) {
//        ImmutableList.Builder<Element> b = ImmutableList.builder();
//        for (int i = 0; i < elements.size(); i++) {
//            if (elementIndex == i) {
//                b.add(elements.get(i).apply(this, direction));
//            } else {
//                b.add(elements.get(i));
//            }
//        }
//        return b.build();
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Target target = (Target) o;

        if (elementIndex != target.elementIndex) return false;
        return kind == target.kind;
    }

    @Override
    public int hashCode() {
        int result = elementIndex;
        result = 31 * result + kind.hashCode();
        return result;
    }
}
