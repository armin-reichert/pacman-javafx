/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.system;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.WorldMovementPolicy;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostWorldInfoComp;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.TerrainTile;
import org.tinylog.Logger;

import java.util.Set;

import static de.amr.basics.math.Direction.UP;
import static de.amr.pacmanfx.core.Validations.isOneOf;
import static java.util.Objects.requireNonNull;

public class GhostWorldMovementPolicy implements WorldMovementPolicy {

    private static final Set<GhostState> DOOR_PASSING_STATES = Set.of(GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE);
    private static final Set<GhostState> TURN_BACK_STATES = Set.of(GhostState.HUNTING_PAC, GhostState.FRIGHTENED);

    @Override
    public boolean canAccessTile(GameLevel level, GameEntity actor, Vector2i tile) {
        requireNonNull(level);
        requireNonNull(actor);
        requireNonNull(tile);

        if (!(actor instanceof Ghost ghost)) {
            throw new IllegalArgumentException("Actor is no ghost");
        }
        final TerrainLayer terrainLayer = level.worldMap().terrainLayer();

        // Portal tiles are the only tiles outside the world map that can be accessed
        if (terrainLayer.outOfBounds(tile)) {
            return terrainLayer.isTileInPortalSpace(tile);
        }

        final GhostWorldInfoComp worldPlacement = actor.reqComp(GhostWorldInfoComp.class);
        final Vector2i myTile = actor.pos().tile();

        // Hunting ghosts cannot enter some tiles in Pac-Man game from below
        // TODO: this is game-specific and does not belong here
        if (worldPlacement.specialTerrainTiles().contains(tile)
            && ghost.ghostStateEnum() == GhostState.HUNTING_PAC
            && terrainLayer.content(tile) == TerrainTile.ONE_WAY_DOWN.$
            && tile.equals(myTile.plus(UP.vector()))
        ) {
            Logger.debug("Hunting {} cannot move up to special tile {}", actor.name(), tile);
            return false;
        }
        if (worldPlacement.house() != null && worldPlacement.house().isDoorAt(tile)) {
            return isOneOf(ghost.ghostStateEnum(), DOOR_PASSING_STATES);
        }
        return !terrainLayer.isInaccessibleTerrainTile(tile);
    }

    @Override
    public boolean canTurnBack(GameEntity actor) {
        if (actor instanceof Ghost ghost) {
            return ghost.worldNavigation().isNewTileEntered() && isOneOf(ghost.ghostStateEnum(), TURN_BACK_STATES);
        }
        return false;
    }
}
