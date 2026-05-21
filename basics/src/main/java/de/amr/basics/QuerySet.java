/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class QuerySet<E> implements Disposable {

    private final Set<E> entries = new HashSet<>();

    public int size() {
        return entries.size();
    }

    public boolean contains(E entity) {
        return entries.contains(entity);
    }

    @Override
    public void dispose() {
        for (E e : entries) {
            if (e instanceof Disposable disposable) {
                disposable.dispose();
            }
        }
        entries.clear();
    }

    public Stream<E> selectAll() {
        return entries.stream();
    }

    public <T> Stream<T> selectAllOfType(Class<T> type) {
        requireNonNull(type);
        return selectAll().filter(type::isInstance).map(type::cast);
    }

    public <T> Stream<T> selectWhere(Class<T> type, Predicate<T> condition) {
        requireNonNull(type);
        requireNonNull(condition);
        return selectAllOfType(type).filter(condition);
    }

    public <T> void removeWhere(Class<T> type, Predicate<T> condition) {
        requireNonNull(type);
        requireNonNull(condition);
        entries.removeIf(e -> type.isInstance(e) && condition.test(type.cast(e)));
    }

    public <T> Optional<T> anyOfType(Class<T> type) {
        requireNonNull(type);
        for (var e : entries) {
            if (type.isInstance(e)) {
                return Optional.of(type.cast(e));
            }
        }
        return Optional.empty();
    }

    /**
     * @param type entity class
     * @return the single entity with given class in entity set. If there is none,
     *         a {@link java.util.NoSuchElementException} exception is thrown.
     * @param <T> type of entity
     */
    public <T> T uniqueOfType(Class<T> type) {
        requireNonNull(type);
        T found = null;
        for (E e : entries) {
            if (type.isInstance(e)) {
                if (found != null) {
                    throw new NoSuchElementException("More than one entity of type '%s'".formatted(type.getSimpleName()));
                }
                found = type.cast(e);
            }
        }
        if (found == null) {
            throw new NoSuchElementException("No entity of type '%s'".formatted(type.getSimpleName()));
        }
        return found;
    }

    public void clear() {
        entries.clear();
    }

    public void add(E entity) {
        requireNonNull(entity);
        entries.add(entity);
    }

    public void addAll(Collection<? extends E> entityCollection) {
        requireNonNull(entityCollection);
        entries.addAll(entityCollection);
    }

    public void remove(E entity) {
        requireNonNull(entity);
        entries.remove(entity);
    }
}
