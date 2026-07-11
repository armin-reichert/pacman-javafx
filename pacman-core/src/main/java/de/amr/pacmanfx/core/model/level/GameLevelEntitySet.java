/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.level;

import de.amr.basics.QuerySet;
import de.amr.pacmanfx.core.event.GameEventManager;

import static java.util.Objects.requireNonNull;

public class GameLevelEntitySet extends QuerySet<GameLevelEntity> {

    public void init(GameLevel level) {
        requireNonNull(level);
        selectAll().forEach(e -> e.init(level));
    }

    public void update(GameLevel level, GameEventManager eventManager) {
        requireNonNull(level);
        selectAll().forEach(e -> e.update(level, eventManager));
    }
}
