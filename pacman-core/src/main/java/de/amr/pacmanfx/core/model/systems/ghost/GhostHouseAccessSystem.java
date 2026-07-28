/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.ghost;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.GhostState;
import de.amr.pacmanfx.core.model.component.common.Position;
import de.amr.pacmanfx.core.model.systems.common.MovementSystem;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;
import de.amr.pacmanfx.core.model.world.House;
import de.amr.pacmanfx.core.model.world.WorldMap;

import static de.amr.basics.math.Direction.*;
import static de.amr.pacmanfx.core.Validations.differsAtMost;

public class GhostHouseAccessSystem {

    /**
     * In locked state, ghosts inside the house are bouncing up and down. They become blue when Pac-Man gets power
     * and start blinking when Pac-Man's power starts fading. After that, they return to their normal color.
     */
    public void stayInHouse(GameContext gameContext, Ghost ghost, float speed) {
        final MovementSystem motor = gameContext.systems().motor;
        final WorldMovementSystem navigator = gameContext.systems().navigator;

        final House house = ghost.house();
        final Position position = ghost.position();

        if (house.isVisitedBy(ghost)) {
            // locked inside house: jumping
            final float minY = (house.minTile().y() + 1) * WorldMap.TS + WorldMap.HTS;
            final float maxY = (house.maxTile().y() - 1) * WorldMap.TS - WorldMap.HTS;
            if (position.y <= minY) {
                navigator.setMoveDir(ghost, DOWN);
                navigator.setWishDir(ghost, DOWN);
            }
            else if (position.y >= maxY) {
                navigator.setMoveDir(ghost, UP);
                navigator.setWishDir(ghost, UP);
            }
            position.setY(Math.clamp(position.y, minY, maxY));
            navigator.setSpeed(ghost, speed);
            motor.moveAccelerated(ghost);
        }
        else {
            // locked outside of house: standing still
            navigator.setSpeed(ghost, 0);
        }
    }

    /**
     * When a ghost leaves the house, he follows a specific route from his home/revival position to the house exit.
     * In the Arcade versions of Pac-Man and Ms.Pac-Man, the ghost first moves towards the vertical center of the house
     * and then raises up until he has passed the door on top of the house.
     * <p>
     * The ghost speed is slower than outside, but I do not know the exact value.
     */
    public boolean leaveHouse(GameContext gameContext, Ghost ghost, float speed) {
        final MovementSystem motor = gameContext.systems().motor;
        final WorldMovementSystem navigator = gameContext.systems().navigator;

        final Position position = ghost.position();
        final Vector2f houseEntryPosition = ghost.house().entryPosition();

        if (position.y <= houseEntryPosition.y()) {
            position.setY(houseEntryPosition.y());
            navigator.setMoveDir(ghost, LEFT);
            navigator.setWishDir(ghost, LEFT);

            // don't change direction directly when outside house
            ghost.worldNavigation().setNewTileEntered(false);

            return true;
        }
        else {
            final float centerX = position.x + WorldMap.HTS;
            final float houseCenterX = ghost.house().center().x();
            if (differsAtMost(0.5f * speed, centerX, houseCenterX)) {
                // align horizontally and raise
                position.setX(houseCenterX - WorldMap.HTS);
                navigator.setMoveDir(ghost, UP);
                navigator.setWishDir(ghost, UP);
            }
            else {
                // move sidewards until center axis is reached
                navigator.setMoveDir(ghost, centerX < houseCenterX ? RIGHT : LEFT);
                navigator.setWishDir(ghost, centerX < houseCenterX ? RIGHT : LEFT);
            }

            navigator.setSpeed(ghost, speed);
            motor.moveAccelerated(ghost);

            return false;
        }
    }

    /**
     * When an eaten ghost has arrived at the ghost house door, he falls down to the center of the house,
     * then moves up again (if the house center is his revival position), or moves sidewards towards his revival position.
     */
    public void enterHouse(GameContext gameContext, Ghost ghost, float speed) {
        final MovementSystem motor = gameContext.systems().motor;
        final WorldMovementSystem navigator = gameContext.systems().navigator;
        final GhostStateSystem ghostStateSystem = gameContext.systems().ghostState;

        final Position position = ghost.position();
        final Vector2f revivalPosition = WorldMap.halfTileRightOf(ghost.house().ghostRevivalTile(ghost.personality()));
        final Vector2f positionVec = position.asVector2f();
        if (positionVec.roughlyEquals(revivalPosition, 0.5f * speed, 0.5f * speed)) {
            position.set(revivalPosition.x(), revivalPosition.y());
            navigator.setMoveDir(ghost, UP);
            navigator.setWishDir(ghost, UP);

            ghostStateSystem.changeState(gameContext, ghost, GhostState.LOCKED);
            return;
        }
        if (position.y < revivalPosition.y()) {
            navigator.setMoveDir(ghost, DOWN);
            navigator.setWishDir(ghost, DOWN);
        }
        else if (position.x > revivalPosition.x()) {
            navigator.setMoveDir(ghost, LEFT);
            navigator.setWishDir(ghost, LEFT);
        }
        else if (position.x < revivalPosition.x()) {
            navigator.setMoveDir(ghost, RIGHT);
            navigator.setWishDir(ghost, RIGHT);
        }
        navigator.setSpeed(ghost, speed);

        motor.moveAccelerated(ghost);
    }

    public void reachHouse(GameContext gameContext, Ghost ghost, float speed) {
        final GhostStateSystem ghostStateSystem = gameContext.systems().ghostState;
        final WorldMovementSystem navigator = gameContext.systems().navigator;

        final Position position = ghost.position();
        final Vector2f houseEntry = ghost.house().entryPosition();
        //TODO
        final Vector2f positionVec =  position.asVector2f();
        if (positionVec.roughlyEquals(houseEntry, speed, 0)) {
            position.set(houseEntry.x(), houseEntry.y());
            navigator.setMoveDir(ghost, DOWN);
            navigator.setWishDir(ghost, DOWN);

            //TODO check if this should be done here
            ghostStateSystem.changeState(gameContext, ghost, GhostState.ENTERING_HOUSE);
        }
        else {
            //TODO use system method
            ghost.worldNavigation().setTargetTile(ghost.house().leftDoorTile());
            navigator.setSpeed(ghost, speed);
            navigator.navigateTowardsTarget(ghost, gameContext);
            navigator.tryMovingOrTeleporting(ghost, gameContext);
        }
    }
}
