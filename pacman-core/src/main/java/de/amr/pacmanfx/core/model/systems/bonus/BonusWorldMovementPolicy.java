/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.bonus;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.GameEntity;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.model.world.TerrainLayer;

import static java.util.Objects.requireNonNull;

public class BonusWorldMovementPolicy implements WorldMovementPolicy {

    @Override
    public boolean canTurnBack(GameEntity actor) {
        return false;
    }

    @Override
    public boolean canAccessTile(GameLevel level, GameEntity actor, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);

        final TerrainLayer terrain = level.worldMap().terrainLayer();
        if (terrain.outOfBounds(tile)) {
            return terrain.isTileInPortalSpace(tile);
        }
        if (terrain.optHouse().isPresent() && terrain.optHouse().get().contains(tile)) {
            return false;
        }
        return !terrain.isTileBlocked(tile);
    }
}
