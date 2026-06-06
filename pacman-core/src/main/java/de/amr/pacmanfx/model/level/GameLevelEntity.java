/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model.level;

import de.amr.pacmanfx.core.GameContext;

public interface GameLevelEntity {

    void init(GameContext gameContext, GameLevel level);

    void update(GameContext gameContext, GameLevel level);
}
