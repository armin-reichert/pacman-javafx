/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.Disposable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class GameLevelEntitySet implements GameLevelEntity, Disposable {

    private final Set<GameLevelEntity> entities = new HashSet<>();

    @Override
    public void init(GameLevel level) {
        requireNonNull(level);
        entities.forEach(e -> e.init(level));
    }

    @Override
    public void update(GameLevel level) {
        requireNonNull(level);
        entities.forEach(e -> e.update(level));
    }

    @Override
    public void dispose() {
        all(Disposable.class).forEach(Disposable::dispose);
        entities.clear();
    }

    public Stream<GameLevelEntity> all() {
        return entities.stream();
    }

    public <T> Stream<T> all(Class<T> type) {
        requireNonNull(type);
        return all().filter(type::isInstance).map(type::cast);
    }

    public <T> Optional<T> first(Class<T> type) {
        for (var e : entities) {
            if (type.isInstance(e)) {
                return Optional.of(type.cast(e));
            }
        }
        return Optional.empty();
    }

    /**
     * @param type entity class
     * @return first entity with given class in entity set. If there is none,
     *         a {@link java.util.NoSuchElementException} exception is thrown.
     * @param <T> type of entity
     */
    public <T> T first$$$(Class<T> type) {
        return first(type).orElseThrow();
    }

    public void clear() {
        entities.clear();
    }

    public void add(GameLevelEntity entity) {
        requireNonNull(entity);
        entities.add(entity);
    }

    public void addAll(Collection<? extends GameLevelEntity> entityCollection) {
        requireNonNull(entityCollection);
        entities.addAll(entityCollection);
    }

    public void remove(GameLevelEntity entity) {
        requireNonNull(entity);
        entities.remove(entity);
    }
}
