/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model.level;

import de.amr.basics.QuerySet;

import static java.util.Objects.requireNonNull;

public class GameLevelEntitySet extends QuerySet<GameLevelEntity> {

    public void init(GameLevel level) {
        requireNonNull(level);
        selectAll().forEach(e -> e.init(level));
    }

    public void update(GameLevel level) {
        requireNonNull(level);
        selectAll().forEach(e -> e.update(level));
    }
}
