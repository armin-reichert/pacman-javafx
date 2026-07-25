/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.level;

import de.amr.basics.QuerySet;
import de.amr.pacmanfx.core.GameContext;

import static java.util.Objects.requireNonNull;

public class GameLevelEntitySet extends QuerySet<GameEntity> {

    public void init(GameContext gameContext) {
        requireNonNull(gameContext);
        selectAll().forEach(e -> e.init(gameContext));
    }

    public void update(GameContext gameContext) {
        requireNonNull(gameContext);
        selectAll().forEach(e -> e.update(gameContext));
    }
}
