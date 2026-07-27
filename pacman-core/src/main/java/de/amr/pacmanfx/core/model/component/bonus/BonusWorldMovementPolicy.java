/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.component.bonus;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.component.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.TerrainLayer;

import static java.util.Objects.requireNonNull;

public class BonusWorldMovementPolicy implements WorldMovementPolicy {

    @Override
    public void reset() {
    }

    @Override
    public boolean canTurnBack(Actor actor) {
        return false;
    }

    @Override
    public boolean canAccessTile(GameContext gameContext, Actor actor, Vector2i tile) {
        requireNonNull(gameContext);
        requireNonNull(tile);

        final GameLevel level = gameContext.assertLevel();
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
