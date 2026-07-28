/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.component.ghost;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.GhostState;
import de.amr.pacmanfx.core.model.component.world.WorldNavigation;
import de.amr.pacmanfx.core.model.component.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;
import de.amr.pacmanfx.core.model.world.TerrainLayer;
import de.amr.pacmanfx.core.model.world.TerrainTile;
import org.tinylog.Logger;

import java.util.Set;

import static de.amr.basics.math.Direction.UP;

public class GhostWorldMovementPolicy implements WorldMovementPolicy {

    @Override
    public void reset() {
    }

    @Override
    public boolean canAccessTile(GameContext gameContext, Actor actor, Vector2i tile) {
        if (!(actor instanceof Ghost ghost)) {
            throw new IllegalArgumentException("Actor is not a Ghost");
        }
        final GameLevel level = gameContext.assertLevel();
        final TerrainLayer terrainLayer = level.worldMap().terrainLayer();

        // Portal tiles are the only tiles outside the world map that can be accessed
        if (terrainLayer.outOfBounds(tile)) {
            return terrainLayer.isTileInPortalSpace(tile);
        }
        final Vector2i myTile = WorldMovementSystem.computeTile(actor);
        // Hunting ghosts cannot enter some tiles in Pac-Man game from below
        // TODO: this is game-specific and does not belong here
        if (ghost.specialTerrainTiles().contains(tile)
            && ghost.state() == GhostState.HUNTING_PAC
            && terrainLayer.content(tile) == TerrainTile.ONE_WAY_DOWN.$
            && tile.equals(myTile.plus(UP.vector()))
        ) {
            Logger.debug("Hunting {} cannot move up to special tile {}", actor.name(), tile);
            return false;
        }
        if (ghost.house() != null && ghost.house().isDoorAt(tile)) {
            return ghost.inAnyOfStates(Set.of(GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE));
        }
        return !terrainLayer.isTileBlocked(tile);
    }

    @Override
    public boolean canTurnBack(Actor actor) {
        if (!(actor instanceof Ghost ghost)) {
            throw new IllegalArgumentException("Actor is not a Ghost");
        }
        final WorldNavigation worldNavigation = actor.assertComponent(WorldNavigation.class);
        return worldNavigation.isNewTileEntered()
            && ghost.inAnyOfStates(Set.of(GhostState.HUNTING_PAC, GhostState.FRIGHTENED));
    }
}
