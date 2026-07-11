/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.level;

import de.amr.pacmanfx.core.event.GameEventManager;

public interface GameLevelEntity {

    void init(GameLevel level);

    void update(GameLevel level, GameEventManager eventManager);
}
