/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.Disposable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
        entitiesOfType(Disposable.class).forEach(Disposable::dispose);
        entities.clear();
    }

    public Stream<GameLevelEntity> entities() {
        return entities.stream();
    }

    public <T> Stream<T> entitiesOfType(Class<? extends T> type) {
        return entities.stream().filter(type::isInstance).map(type::cast);
    }

    public void clear() {
        entities.clear();
    }

    public void addEntity(GameLevelEntity entity) {
        requireNonNull(entity);
        entities.add(entity);
    }

    public void addAllEntities(Collection<? extends GameLevelEntity> entityCollection) {
        entities.addAll(entityCollection);
    }

    public void removeEntity(GameLevelEntity entity) {
        requireNonNull(entity);
        entities.remove(entity);
    }
}
