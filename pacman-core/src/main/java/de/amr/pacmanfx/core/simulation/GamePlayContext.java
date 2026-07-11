/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.simulation;

import de.amr.pacmanfx.core.event.GameEventManager;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.level.GameLevel;

public record GamePlayContext(
    GameLevel level,
    GameModel model,
    GameEventManager eventManager
) {}
