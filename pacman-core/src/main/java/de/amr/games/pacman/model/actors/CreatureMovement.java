/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.world.Portal;
import de.amr.games.pacman.model.world.World;
import org.tinylog.Logger;

import java.util.List;
import java.util.Optional;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.*;

/**
 * Functionality for moving creatures through the world.
 */
public interface CreatureMovement {

    Direction[] DIRECTION_PRIORITY = {UP, LEFT, DOWN, RIGHT};

    static Optional<Direction> computeTargetDirection(Creature creature, World world) {
        final var currentTile = creature.tile();
        Direction targetDir = null;
        float minDistance = Float.MAX_VALUE;
        for (var dir : DIRECTION_PRIORITY) {
            if (dir == creature.moveDir().opposite()) {
                continue; // reversing the move direction is not allowed
            }
            final var neighborTile = currentTile.plus(dir.vector());
            if (creature.canAccessTile(neighborTile, world)) {
                final float distance = neighborTile.euclideanDistance(creature.targetTile().get());
                if (distance < minDistance) {
                    minDistance = distance;
                    targetDir = dir;
                }
            }
        }
        return Optional.ofNullable(targetDir);
    }

    /**
     * Sets the new wish direction for reaching the target tile.
     */
    static void navigateTowardsTarget(Creature creature, World world) {
        if (creature.targetTile().isEmpty()) {
            return;
        }
        if (!creature.newTileEntered && creature.moveResult.moved) {
            // we don't need no navigation, dim dit diddit diddit...
            return;
        }
        if (world.belongsToPortal(creature.tile())) {
            return; // inside portal, no navigation happens
        }
        computeTargetDirection(creature, world).ifPresent(creature::setWishDir);
    }

    static void followTarget(Creature creature, World world, Vector2i targetTile, byte relSpeed) {
        creature.setPercentageSpeed(relSpeed);
        creature.setTargetTile(targetTile);
        navigateTowardsTarget(creature, world);
        tryMoving(creature, world);
    }


    static void roam(Creature creature, World world, byte relSpeed, Direction dir) {
        if (!world.belongsToPortal(creature.tile()) && (creature.isNewTileEntered() || !creature.moveResult.moved)) {
            while (dir == creature.moveDir().opposite() || !creature.canAccessTile(
                creature.tile().plus(dir.vector()), world)) {
                dir = dir.nextClockwise();
            }
            creature.setWishDir(dir);
        }
        creature.setPercentageSpeed(relSpeed);
        tryMoving(creature, world);
    }

    /**
     * Tries moving through the game world.
     * <p>
     * First checks if the creature can teleport, then if the creature can move to its wish direction. If this is not
     * possible, it keeps moving to its current move direction.
     */
    static void tryMoving(Creature creature, World world) {
        creature.moveResult.clear();
        tryTeleport(creature, world.portals());
        if (!creature.moveResult.teleported) {
            creature.executeReverseCommand();
            tryMoving(creature, world, creature.wishDir());
            if (creature.moveResult.moved) {
                creature.setMoveDir(creature.wishDir());
            } else {
                tryMoving(creature, world, creature.moveDir());
            }
        }
        if (creature.moveResult.teleported || creature.moveResult.moved) {
            Logger.trace("{}: {} {} {}", creature.name(), creature.moveResult, creature.moveResult.summary(), creature);
        }
    }

    static void tryTeleport(Creature creature, List<Portal> portals) {
        if (creature.canTeleport) {
            for (var portal : portals) {
                tryTeleport(creature, portal);
                if (creature.moveResult.teleported) {
                    return;
                }
            }
        }
    }

    static void tryTeleport(Creature creature, Portal portal) {
        var tile = creature.tile();
        var old_pos_x = creature.posX;
        var old_pos_y = creature.posY;
        if (tile.y() == portal.leftTunnelEnd().y() && creature.posX < portal.leftTunnelEnd().x() - portal.depth() * TS) {
            creature.centerOverTile(portal.rightTunnelEnd());
            creature.moveResult.teleported = true;
            creature.moveResult.addMessage(String.format("%s: Teleported from (%.2f,%.2f) to (%.2f,%.2f)",
                creature.name(), old_pos_x, old_pos_y, creature.posX, creature.posY));
        } else if (tile.equals(portal.rightTunnelEnd().plus(portal.depth(), 0))) {
            creature.centerOverTile(portal.leftTunnelEnd().minus(portal.depth(), 0));
            creature.moveResult.teleported = true;
            creature.moveResult.addMessage(String.format("%s: Teleported from (%.2f,%.2f) to (%.2f,%.2f)",
                creature.name(), old_pos_x, old_pos_y, creature.posX, creature.posY));
        }
    }

    static void tryMoving(Creature creature, World world, Direction dir) {
        final Vector2i tileBeforeMove = creature.tile();
        final Vector2f dirVector = dir.vector().toFloatVec();
        final Vector2f newVelocity = dirVector.scaled(creature.velocity().length());
        final Vector2f touchPosition = creature.center().plus(dirVector.scaled(HTS)).plus(newVelocity);
        final Vector2i touchedTile = tileAt(touchPosition);
        final boolean isTurn = !dir.sameOrientation(creature.moveDir());

        if (!creature.canAccessTile(touchedTile, world)) {
            if (!isTurn) {
                creature.centerOverTile(creature.tile()); // adjust over tile (would move forward against wall)
            }
            creature.moveResult.addMessage(String.format("Cannot move %s into tile %s", dir, touchedTile));
            return;
        }

        if (isTurn) {
            float offset = dir.isHorizontal() ? creature.offset().y() : creature.offset().x();
            boolean atTurnPosition = Math.abs(offset) <= 1; // TODO <= pixel-speed?
            if (atTurnPosition) {
                creature.centerOverTile(creature.tile()); // adjust over tile (starts moving around corner)
            } else {
                creature.moveResult.addMessage(String.format("Wants to take corner towards %s but not at turn position", dir));
                return;
            }
        }

        if (isTurn && creature.corneringSpeedUp > 0) {
            creature.setVelocity(newVelocity.plus(dirVector.scaled(creature.corneringSpeedUp)));
            Logger.trace("{} velocity around corner: {}", creature.name(), creature.velocity().length());
            creature.move();
            creature.setVelocity(newVelocity);
        } else {
            creature.setVelocity(newVelocity);
            creature.move();
        }

        Vector2i currentTile = creature.tile();

        creature.newTileEntered = !tileBeforeMove.equals(currentTile);
        creature.moveResult.moved = true;
        creature.moveResult.tunnelEntered = world.isTunnel(currentTile)
            && !world.isTunnel(tileBeforeMove)
            && !world.belongsToPortal(tileBeforeMove);
        creature.moveResult.tunnelLeft = !world.isTunnel(currentTile)
            && world.isTunnel(tileBeforeMove)
            && !world.belongsToPortal(currentTile);

        creature.moveResult.addMessage(String.format("%5s (%.2f pixels)", dir, newVelocity.length()));

        if (creature.moveResult.tunnelEntered) {
            Logger.trace("{} entered tunnel", creature.name());
        }
        if (creature.moveResult.tunnelLeft) {
            Logger.trace("{} left tunnel", creature.name());
        }
    }
}
