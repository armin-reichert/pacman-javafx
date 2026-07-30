/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.world;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.actors.GameEntity;
import de.amr.pacmanfx.core.model.level.GameLevel;

public interface WorldMovementPolicy {

    boolean canAccessTile(GameLevel level, GameEntity actor, Vector2i tile);

    boolean canTurnBack(GameEntity actor);
}
