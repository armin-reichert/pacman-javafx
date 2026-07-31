/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.level;

import de.amr.basics.QuerySet;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameEntity;
import de.amr.pacmanfx.core.model.UpdatableEntity;

import static java.util.Objects.requireNonNull;

public class GameLevelEntitySet extends QuerySet<GameEntity> {

    public void init(GameContext gameContext) {
        requireNonNull(gameContext);
        selectAll()
            .filter(UpdatableEntity.class::isInstance).map(UpdatableEntity.class::cast)
            .forEach(e -> e.init(gameContext));
    }

    public void update(GameContext gameContext) {
        requireNonNull(gameContext);
        selectAll()
            .filter(UpdatableEntity.class::isInstance).map(UpdatableEntity.class::cast)
            .forEach(e -> e.update(gameContext));
    }
}
