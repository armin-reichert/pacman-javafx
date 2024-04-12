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

    /** Order in which directions are selected when navigation decision is met. */
    Direction[] DIRECTION_PRIORITY = {UP, LEFT, DOWN, RIGHT};

    /**
     * Implements the rules by which a creature (ghost) decides how to reach its current target tile.
     *
     * @param creature a creature (ghost, moving bonus)
     * @param world the world/maze
     * @return optional direction the creature takes to reach its current target tile
     */
    static Optional<Direction> computeTargetDirection(Creature creature, World world) {
        if (creature.targetTile().isEmpty()) {
            return Optional.empty();
        }
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
     *
     * @param creature a creature (ghost, moving bonus)
     * @param world the world/maze
     */
    static void navigateTowardsTarget(Creature creature, World world) {
        if (creature.targetTile().isEmpty()) {
            return;
        }
        if (!creature.newTileEntered && creature.moveResult.moved) {
            return; // we don't need no navigation, dim dit diddit diddit...
        }
        if (world.belongsToPortal(creature.tile())) {
            return; // inside portal, no navigation happens
        }
        computeTargetDirection(creature, world).ifPresent(creature::setWishDir);
    }

    /**
     * Lets a creature follow the given target tile.
     *
     * @param creature a creature (ghost, moving bonus)
     * @param world the world/maze
     * @param targetTile the target tile e.g. Pac-Man's current tile
     * @param relSpeed the relative speed (in percentage of base speed)
     */
    static void followTarget(Creature creature, World world, Vector2i targetTile, byte relSpeed) {
        creature.setPercentageSpeed(relSpeed);
        creature.setTargetTile(targetTile);
        navigateTowardsTarget(creature, world);
        tryMoving(creature, world);
    }


    /**
     * Lets a creature randomly roam through the world.
     *
     * @param creature a creature (ghost, moving bonus)
     * @param world the world/maze
     * @param relSpeed the relative speed (in percentage of base speed)
     * @param dir the intended direction. If this direction cannot be taken, try the other directions in clockwise order
     */
    static void roam(Creature creature, World world, byte relSpeed, Direction dir) {
        Vector2i currentTile = creature.tile();
        if (!world.belongsToPortal(currentTile) && (creature.newTileEntered || !creature.moveResult.moved)) {
            while (dir == creature.moveDir().opposite()
                || !creature.canAccessTile(currentTile.plus(dir.vector()), world)) {
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
     *
     * @param creature a creature (ghost, moving bonus)
     * @param world the world/maze
     */
    static void tryMoving(Creature creature, World world) {
        creature.moveResult.clear();
        tryTeleport(creature, world.portals());
        if (!creature.moveResult.teleported) {
            if (creature.gotReverseCommand && creature.canReverse()) {
                creature.setWishDir(creature.moveDir().opposite());
                creature.gotReverseCommand = false;
                Logger.info("{}: [turned around]", creature.name());
            }
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

    /**
     * Tries to teleport a creature through a portal.
     *
     * @param creature a creature (ghost, moving bonus)
     * @param portals list of available portals
     */
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

    /**
     * Tries to teleport a creature through a portal.
     *
     * @param creature a creature (ghost, moving bonus)
     * @param portal a portal
     */
    static void tryTeleport(Creature creature, Portal portal) {
        var tile = creature.tile();
        var oldX = creature.posX;
        var oldY = creature.posY;
        if (tile.y() == portal.leftTunnelEnd().y() && creature.posX < portal.leftTunnelEnd().x() - portal.depth() * TS) {
            creature.centerOverTile(portal.rightTunnelEnd());
            creature.moveResult.teleported = true;
            creature.moveResult.addMessage(String.format("%s: Teleported from (%.2f,%.2f) to (%.2f,%.2f)",
                creature.name(), oldX, oldY, creature.posX, creature.posY));
        } else if (tile.equals(portal.rightTunnelEnd().plus(portal.depth(), 0))) {
            creature.centerOverTile(portal.leftTunnelEnd().minus(portal.depth(), 0));
            creature.moveResult.teleported = true;
            creature.moveResult.addMessage(String.format("%s: Teleported from (%.2f,%.2f) to (%.2f,%.2f)",
                creature.name(), oldX, oldY, creature.posX, creature.posY));
        }
    }

    /**
     * Tries to move a creature towards the given directory. Handles collisions with walls and moving around corners.
     *
     * @param creature a creature (ghost, moving bonus)
     * @param world the world/maze
     * @param dir the direction to move
     */
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
