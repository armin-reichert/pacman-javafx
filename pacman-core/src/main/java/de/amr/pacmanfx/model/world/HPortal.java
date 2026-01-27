/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.world;

import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.actors.MovingActor;
import org.tinylog.Logger;

/**
 * A horizontal portal connects two border tiles on the left and right map border. Traveling through the portal
 * corresponds to moving <code>portalDepth</code> tiles into the "portal space", wrapping around and traveling
 * <code>portalDepth</code> tiles back into the maze. So an actor moving through a portal moves in total
 * <code>2*portalDepth - 1</code> tiles outside the maze.
 */
public record HPortal(Vector2i leftBorderEntryTile, Vector2i rightBorderEntryTile, int depth) {

    public HPortal {
        if (leftBorderEntryTile.y() != rightBorderEntryTile.y()) {
            throw new IllegalArgumentException("Left portal entry tile y (%d) is different from right portal entry tile y (%d)"
                    .formatted(leftBorderEntryTile.y(), rightBorderEntryTile.y()));
        }
    }

    public boolean contains(Vector2i tile) {
        for (int numTiles = 1; numTiles <= depth; ++numTiles) {
            Vector2i leftPortalTile = leftBorderEntryTile.minus(numTiles, 0);
            Vector2i rightPortalTile = rightBorderEntryTile.plus(numTiles, 0);
            if (tile.equals(leftPortalTile) || tile.equals(rightPortalTile)) {
                return true;
            }
        }
        return false;
    }

    public boolean tryTeleporting(MovingActor actor) {
        final Vector2i actorTile = actor.tile();
        final Vector2f offset = actor.offset();
        if (actorTile.y() != leftBorderEntryTile().y()) {
            return false;
        }
        final Vector2i leftWrappingTile = leftBorderEntryTile().minus(depth, 0);
        final Vector2i rightWrappingTile = rightBorderEntryTile().plus(depth, 0);
        switch (actor.moveDir()) {
            case LEFT -> {
                if (actorTile.equals(leftWrappingTile) && offset.x() == 0) {
                    actor.placeAtTile(rightWrappingTile.x(), rightWrappingTile.y(), -1, 0);
                    Logger.info("{} teleported from {} to {}", actor.name(), actorTile, rightWrappingTile);
                    return true;
                }
            }
            case RIGHT -> {
                if (actorTile.equals(rightWrappingTile) && offset.x() == 0) {
                    actor.placeAtTile(leftWrappingTile.x(), leftWrappingTile.y(), 1, 0);
                    Logger.info("{} teleported from {} to {}", actor.name(), actorTile, leftWrappingTile);
                    return true;
                }
            }
            default -> throw new IllegalStateException("Actor moving %s cannot be teleported horizontally".formatted(actor.moveDir()));
        }
        return false;
    }
}