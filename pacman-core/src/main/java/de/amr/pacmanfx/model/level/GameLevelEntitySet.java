/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model.level;

import de.amr.basics.QuerySet;
import de.amr.pacmanfx.core.GameContext;

import static java.util.Objects.requireNonNull;

public class GameLevelEntitySet extends QuerySet<GameLevelEntity> {

    public void init(GameContext gameContext, GameLevel level) {
        requireNonNull(level);
        selectAll().forEach(e -> e.init(gameContext, level));
    }

    public void update(GameContext gameContext, GameLevel level) {
        requireNonNull(level);
        selectAll().forEach(e -> e.update(gameContext, level));
    }
}
