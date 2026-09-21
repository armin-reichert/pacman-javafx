/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.actor.bonus;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.systems.WorldMovementPolicy;
import de.amr.pacmanfx.core.entities.world.house.House;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;

import static java.util.Objects.requireNonNull;

public class BonusWorldMovementPolicy implements WorldMovementPolicy<Bonus> {

    @Override
    public boolean canTurnBack(Bonus bonus) {
        return false;
    }

    @Override
    public boolean canAccessTile(GameLevel level, Bonus bonus, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);

        final TerrainLayer terrain = level.worldMap().terrainLayer();
        if (terrain.outOfBounds(tile)) {
            return terrain.isTileInPortalSpace(tile);
        }
        final House house = level.entitySet().entities().theOne(House.class);
        if (house != null && house.contains(tile)) {
            return false;
        }
        return !terrain.isInaccessibleTile(tile);
    }
}
