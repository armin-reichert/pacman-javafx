/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.pac.system;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.systems.WorldMovementPolicy;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.TerrainTile;

import static java.util.Objects.requireNonNull;

public class PacWorldMovementPolicy implements WorldMovementPolicy<Pac> {

    @Override
    public boolean canTurnBack(Pac pac) {
        return pac.worldNavigation().isNewTileEntered();
    }

    @Override
    public boolean canAccessTile(GameLevel level, Pac pac, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(pac);
        requireNonNull(tile);

        final TerrainLayer terrain = level.worldMap().terrainLayer();

        // Portal tiles are the only tiles outside the world that can be accessed
        if (terrain.outOfBounds(tile)) {
            return terrain.isTileInPortalSpace(tile);
        }
        if (terrain.content(tile) == TerrainTile.DOOR.$) {
            return false;
        }
        return !terrain.isInaccessibleTile(tile);
    }
}
