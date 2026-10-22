/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class QuerySet<E> extends HashSet<E> implements Disposable {

    @SafeVarargs
    public final void addAll(E... elements) {
        addAll(List.of(elements));
    }

    @Override
    public void dispose() {
        for (E e : this) {
            if (e instanceof Disposable disposable) {
                disposable.dispose();
            }
        }
        clear();
    }

    public <T extends E> Stream<T> elementsOfType(Class<T> type) {
        requireNonNull(type);
        return stream().filter(type::isInstance).map(type::cast);
    }

    public <T extends E> Stream<T> ofTypeWhere(Class<T> type, Predicate<T> condition) {
        requireNonNull(type);
        requireNonNull(condition);
        return elementsOfType(type).filter(condition);
    }

    public <T extends E> void removeWhere(Class<T> type, Predicate<T> condition) {
        requireNonNull(type);
        requireNonNull(condition);
        removeIf(e -> type.isInstance(e) && condition.test(type.cast(e)));
    }

    public <T extends E> Optional<T> anyOfType(Class<T> type) {
        return Optional.ofNullable(anyOfTypeOrNull(type));
    }

    public <T extends E> T anyOfTypeOrNull(Class<T> type) {
        requireNonNull(type);
        for (var e : this) {
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
        for (E e : this) {
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
