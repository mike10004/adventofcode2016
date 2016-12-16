package aoc2016day11;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

public class DelegatingStream<E> {

    protected final Stream<E> stream;

    public DelegatingStream(Stream<E> stream) {
        this.stream = checkNotNull(stream);
    }

    public Stream<E> filter(Predicate<? super E> predicate) {
        return stream.filter(predicate);
    }

    public <R> Stream<R> map(Function<? super E, ? extends R> mapper) {
        return stream.map(mapper);
    }

    public IntStream mapToInt(ToIntFunction<? super E> mapper) {
        return stream.mapToInt(mapper);
    }

    public LongStream mapToLong(ToLongFunction<? super E> mapper) {
        return stream.mapToLong(mapper);
    }

    public DoubleStream mapToDouble(ToDoubleFunction<? super E> mapper) {
        return stream.mapToDouble(mapper);
    }

    public <R> Stream<R> flatMap(Function<? super E, ? extends Stream<? extends R>> mapper) {
        return stream.flatMap(mapper);
    }

    public IntStream flatMapToInt(Function<? super E, ? extends IntStream> mapper) {
        return stream.flatMapToInt(mapper);
    }

    public LongStream flatMapToLong(Function<? super E, ? extends LongStream> mapper) {
        return stream.flatMapToLong(mapper);
    }

    public DoubleStream flatMapToDouble(Function<? super E, ? extends DoubleStream> mapper) {
        return stream.flatMapToDouble(mapper);
    }

    public Stream<E> distinct() {
        return stream.distinct();
    }

    public Stream<E> sorted() {
        return stream.sorted();
    }

    public Stream<E> sorted(Comparator<? super E> comparator) {
        return stream.sorted(comparator);
    }

    public Stream<E> peek(Consumer<? super E> action) {
        return stream.peek(action);
    }

    public Stream<E> limit(long maxSize) {
        return stream.limit(maxSize);
    }

    public Stream<E> skip(long n) {
        return stream.skip(n);
    }

    public void forEach(Consumer<? super E> action) {
        stream.forEach(action);
    }

    public void forEachOrdered(Consumer<? super E> action) {
        stream.forEachOrdered(action);
    }

    public Object[] toArray() {
        return stream.toArray();
    }

    public <A> A[] toArray(IntFunction<A[]> generator) {
        return stream.toArray(generator);
    }

    public E reduce(E identity, BinaryOperator<E> accumulator) {
        return stream.reduce(identity, accumulator);
    }

    public Optional<E> reduce(BinaryOperator<E> accumulator) {
        return stream.reduce(accumulator);
    }

    public <U> U reduce(U identity, BiFunction<U, ? super E, U> accumulator, BinaryOperator<U> combiner) {
        return stream.reduce(identity, accumulator, combiner);
    }

    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super E> accumulator, BiConsumer<R, R> combiner) {
        return stream.collect(supplier, accumulator, combiner);
    }

    public <R, A> R collect(Collector<? super E, A, R> collector) {
        return stream.collect(collector);
    }

    public Optional<E> min(Comparator<? super E> comparator) {
        return stream.min(comparator);
    }

    public Optional<E> max(Comparator<? super E> comparator) {
        return stream.max(comparator);
    }

    public long count() {
        return stream.count();
    }

    public boolean anyMatch(Predicate<? super E> predicate) {
        return stream.anyMatch(predicate);
    }

    public boolean allMatch(Predicate<? super E> predicate) {
        return stream.allMatch(predicate);
    }

    public boolean noneMatch(Predicate<? super E> predicate) {
        return stream.noneMatch(predicate);
    }

    public Optional<E> findFirst() {
        return stream.findFirst();
    }

    public Optional<E> findAny() {
        return stream.findAny();
    }

    public Iterator<E> iterator() {
        return stream.iterator();
    }

    public Spliterator<E> spliterator() {
        return stream.spliterator();
    }

    public boolean isParallel() {
        return stream.isParallel();
    }

    public Stream<E> sequential() {
        return stream.sequential();
    }

    public Stream<E> parallel() {
        return stream.parallel();
    }

    public Stream<E> unordered() {
        return stream.unordered();
    }

    public Stream<E> onClose(Runnable closeHandler) {
        return stream.onClose(closeHandler);
    }

    public void close() {
        stream.close();
    }
}
