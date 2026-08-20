/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.pac.system;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.ecs.systems.WorldMovementPolicy;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;

import static java.util.Objects.requireNonNull;

public class PacWorldMovementPolicy implements WorldMovementPolicy<Pac> {

    @Override
    public boolean canTurnBack(Pac pac) {
        final WorldNavigationComp worldNavigation = pac.reqComp(WorldNavigationComp.class);
        return worldNavigation.isNewTileEntered();
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
        final House house = level.entities().house();
        if (house != null && house.contains(tile)) {
            return false; // Schieb ab, Alter!
        }
        return !terrain.isInaccessibleTerrainTile(tile);
    }
}
