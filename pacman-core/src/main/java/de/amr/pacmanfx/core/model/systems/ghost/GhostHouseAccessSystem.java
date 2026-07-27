/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.ghost;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.GhostState;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.common.Position;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.MovementSystem;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;
import de.amr.pacmanfx.core.model.systems.pac.PacPowerSystem;
import de.amr.pacmanfx.core.model.world.House;
import de.amr.pacmanfx.core.model.world.WorldMap;

import static de.amr.basics.math.Direction.*;
import static de.amr.basics.math.Direction.LEFT;
import static de.amr.basics.math.Direction.RIGHT;
import static de.amr.pacmanfx.core.Validations.differsAtMost;

public class GhostHouseAccessSystem {

    /**
     * In locked state, ghosts inside the house are bouncing up and down. They become blue when Pac-Man gets power
     * and start blinking when Pac-Man's power starts fading. After that, they return to their normal color.
     */
    public void stayInHouse(GameContext gameContext, Ghost ghost, float speed) {
        final MovementSystem movementSystem = gameContext.systems().movementSystem;
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;

        final House house = ghost.house();
        final Position position = ghost.position();

        if (house.isVisitedBy(ghost)) {
            // locked inside house: jumping
            final float minY = (house.minTile().y() + 1) * WorldMap.TS + WorldMap.HTS;
            final float maxY = (house.maxTile().y() - 1) * WorldMap.TS - WorldMap.HTS;
            if (position.y <= minY) {
                worldMovementSystem.setMoveDir(ghost, DOWN);
                worldMovementSystem.setWishDir(ghost, DOWN);
            }
            else if (position.y >= maxY) {
                worldMovementSystem.setMoveDir(ghost, UP);
                worldMovementSystem.setWishDir(ghost, UP);
            }
            position.setY(Math.clamp(position.y, minY, maxY));
            worldMovementSystem.setSpeed(ghost, speed);
            movementSystem.moveAccelerated(ghost);
        }
        else {
            // locked outside of house: standing still
            worldMovementSystem.setSpeed(ghost, 0);
        }

        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();
        if (isThreatenedByPac(gameContext, ghost, pac)) {
            ghost.playFrightenedAnimation(gameContext);
        } else {
            ghost.animations.select(CommonAnimationID.GHOST_NORMAL);
        }

    }

    /**
     * When a ghost leaves the house, he follows a specific route from his home/revival position to the house exit.
     * In the Arcade versions of Pac-Man and Ms.Pac-Man, the ghost first moves towards the vertical center of the house
     * and then raises up until he has passed the door on top of the house.
     * <p>
     * The ghost speed is slower than outside, but I do not know the exact value.
     */
    public void leaveHouse(GameContext gameContext, Ghost ghost, float speed) {
        final MovementSystem movementSystem = gameContext.systems().movementSystem;
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;
        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();

        final Position position = ghost.position();
        final Vector2f houseEntryPosition = ghost.house().entryPosition();
        if (position.y <= houseEntryPosition.y()) {
            // outside at house entry
            position.setY(houseEntryPosition.y());
            worldMovementSystem.setMoveDir(ghost, LEFT);
            worldMovementSystem.setWishDir(ghost, LEFT);
            ghost.worldMovement().setNewTileEntered(false); // don't change direction until new tile is entered by moving
            ghost.setState(isThreatenedByPac(gameContext, ghost, pac) ? GhostState.FRIGHTENED : GhostState.HUNTING_PAC);
        }
        else {
            // still inside house
            final float centerX = position.x + WorldMap.HTS;
            final float houseCenterX = ghost.house().center().x();
            if (differsAtMost(0.5f * speed, centerX, houseCenterX)) {
                // align horizontally and raise
                position.setX(houseCenterX - WorldMap.HTS);
                worldMovementSystem.setMoveDir(ghost, UP);
                worldMovementSystem.setWishDir(ghost, UP);
            } else {
                // move sidewards until center axis is reached
                worldMovementSystem.setMoveDir(ghost, centerX < houseCenterX ? RIGHT : LEFT);
                worldMovementSystem.setWishDir(ghost, centerX < houseCenterX ? RIGHT : LEFT);
            }
            worldMovementSystem.setSpeed(ghost, speed);

            movementSystem.moveAccelerated(ghost);

            if (isThreatenedByPac(gameContext, ghost, pac)) {
                ghost.playFrightenedAnimation(gameContext);
            } else {
                ghost.animations.select(CommonAnimationID.GHOST_NORMAL);
            }
        }
    }

    private boolean isThreatenedByPac(GameContext gameContext, Ghost ghost, Pac pac) {
        final PacPowerSystem pacPowerSystem = gameContext.systems().pacPowerSystem;
        final GameLevel level = gameContext.assertLevel();
        return pacPowerSystem.isPowerActive(pac) && !level.isInGhostKilledChain(ghost);
    }

    /**
     * When an eaten ghost has arrived at the ghost house door, he falls down to the center of the house,
     * then moves up again (if the house center is his revival position), or moves sidewards towards his revival position.
     */
    public void enterHouse(GameContext gameContext, Ghost ghost, float speed) {
        final MovementSystem movementSystem = gameContext.systems().movementSystem;
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;

        final Position position = ghost.position();
        final Vector2f revivalPosition = WorldMap.halfTileRightOf(ghost.house().ghostRevivalTile(ghost.personality()));
        final Vector2f positionVec = position.asVector2f();
        if (positionVec.roughlyEquals(revivalPosition, 0.5f * speed, 0.5f * speed)) {
            position.set(revivalPosition.x(), revivalPosition.y());
            worldMovementSystem.setMoveDir(ghost, UP);
            worldMovementSystem.setWishDir(ghost, UP);
            ghost.setState(GhostState.LOCKED);
            return;
        }
        if (position.y < revivalPosition.y()) {
            worldMovementSystem.setMoveDir(ghost, DOWN);
            worldMovementSystem.setWishDir(ghost, DOWN);
        }
        else if (position.x > revivalPosition.x()) {
            worldMovementSystem.setMoveDir(ghost, LEFT);
            worldMovementSystem.setWishDir(ghost, LEFT);
        }
        else if (position.x < revivalPosition.x()) {
            worldMovementSystem.setMoveDir(ghost, RIGHT);
            worldMovementSystem.setWishDir(ghost, RIGHT);
        }
        worldMovementSystem.setSpeed(ghost, speed);

        movementSystem.moveAccelerated(ghost);
    }
}
