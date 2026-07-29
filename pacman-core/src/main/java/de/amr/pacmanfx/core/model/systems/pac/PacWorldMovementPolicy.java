/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.pac;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.systems.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.model.component.world.WorldNavigation;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.TerrainLayer;

import static java.util.Objects.requireNonNull;

public class PacWorldMovementPolicy implements WorldMovementPolicy {

    @Override
    public boolean canTurnBack(Actor actor) {
        final WorldNavigation worldNavigation = actor.assertComponent(WorldNavigation.class);
        return worldNavigation.isNewTileEntered();
    }

    @Override
    public boolean canAccessTile(GameLevel level, Actor actor, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(tile);
        requireNonNull(tile);

        final TerrainLayer terrain = level.worldMap().terrainLayer();

        // Portal tiles are the only tiles outside the world that can be accessed
        if (terrain.outOfBounds(tile)) {
            return terrain.isTileInPortalSpace(tile);
        }
        if (terrain.optHouse().isPresent() && terrain.optHouse().get().contains(tile)) {
            return false; // Schieb ab, Alter!
        }
        return !terrain.isTileBlocked(tile);
    }
}
