/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class QuerySet<E> implements Iterable<E>, Disposable {

    private final Set<E> elements = new HashSet<>();

    public void clear() {
        elements.clear();
    }

    public void add(E e) {
        requireNonNull(e);
        elements.add(e);
    }

    public void addAll(Collection<? extends E> collection) {
        requireNonNull(collection);
        elements.addAll(collection);
    }

    @SafeVarargs
    public final void addAll(E... elements) {
        this.elements.addAll(List.of(elements));
    }

    public void remove(E e) {
        requireNonNull(e);
        elements.remove(e);
    }

    public int size() {
        return elements.size();
    }

    public boolean contains(E entity) {
        return elements.contains(entity);
    }

    @Override
    public Iterator<E> iterator() {
        return elements.iterator();
    }

    @Override
    public void dispose() {
        for (E e : elements) {
            if (e instanceof Disposable disposable) {
                disposable.dispose();
            }
        }
        elements.clear();
    }

    public Stream<E> all() {
        return elements.stream();
    }

    public <T extends E> Stream<T> ofType(Class<T> type) {
        requireNonNull(type);
        return all().filter(type::isInstance).map(type::cast);
    }

    public <T extends E> Stream<T> ofTypeWhere(Class<T> type, Predicate<T> condition) {
        requireNonNull(type);
        requireNonNull(condition);
        return ofType(type).filter(condition);
    }

    public <T extends E> void removeWhere(Class<T> type, Predicate<T> condition) {
        requireNonNull(type);
        requireNonNull(condition);
        elements.removeIf(e -> type.isInstance(e) && condition.test(type.cast(e)));
    }

    public <T extends E> Optional<T> anyOfType(Class<T> type) {
        return Optional.ofNullable(anyOfTypeOrNull(type));
    }

    public <T extends E> T anyOfTypeOrNull(Class<T> type) {
        requireNonNull(type);
        for (var e : elements) {
            if (type.isInstance(e)) {
                return type.cast(e);
            }
        }
        return null;
    }

    /**
     * @param type entity class
     * @return the single element with given class in element set. If there is none,
     *         a {@link java.util.NoSuchElementException} exception is thrown.
     * @param <T> type of element
     */
    public <T extends E> T theOne(Class<T> type) {
        requireNonNull(type);
        T found = null;
        for (E e : elements) {
            if (type.isInstance(e)) {
                if (found != null) {
                    throw new NoSuchElementException("More than one element of type '%s'".formatted(type.getSimpleName()));
                }
                found = type.cast(e);
            }
        }
        if (found == null) {
            throw new NoSuchElementException("No element of type '%s'".formatted(type.getSimpleName()));
        }
        return found;
    }
}
