/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.simulation;

import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.level.GameLevel;

public record GamePlayContext(
    GameLevel level,
    GameModel model,
    GameEventManager eventManager
) {}
