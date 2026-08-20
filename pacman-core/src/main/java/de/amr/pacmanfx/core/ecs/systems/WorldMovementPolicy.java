/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs.systems;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.level.GameLevel;

public interface WorldMovementPolicy<E extends GameEntity> {

    boolean canAccessTile(GameLevel level, E entity, Vector2i tile);

    boolean canTurnBack(E entity);
}
