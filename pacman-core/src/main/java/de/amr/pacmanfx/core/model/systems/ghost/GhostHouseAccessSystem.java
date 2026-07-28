/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.ghost;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameSystems;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.GhostState;
import de.amr.pacmanfx.core.model.component.common.Position;
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
        final GameSystems sys = gameContext.systems();

        final House house = ghost.worldPlacement().house();
        final Position position = ghost.position();

        if (house.isVisitedBy(ghost)) {
            // locked inside house: jumping
            final float minY = (house.minTile().y() + 1) * WorldMap.TS + WorldMap.HTS;
            final float maxY = (house.maxTile().y() - 1) * WorldMap.TS - WorldMap.HTS;
            if (position.y <= minY) {
                sys.navigator.setMoveDir(ghost, DOWN);
                sys.navigator.setWishDir(ghost, DOWN);
            }
            else if (position.y >= maxY) {
                sys.navigator.setMoveDir(ghost, UP);
                sys.navigator.setWishDir(ghost, UP);
            }
            position.setY(Math.clamp(position.y, minY, maxY));
            sys.navigator.setSpeed(ghost, speed);
            sys.motor.moveAccelerated(ghost);
        }
        else {
            // locked outside of house: standing still
            sys.navigator.setSpeed(ghost, 0);
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
        final GameSystems sys = gameContext.systems();

        final Position position = ghost.position();
        final House house = ghost.worldPlacement().house();
        final Vector2f houseEntryPosition = house.entryPosition();

        if (position.y <= houseEntryPosition.y()) {
            position.setY(houseEntryPosition.y());
            sys.navigator.setMoveDir(ghost, LEFT);
            sys.navigator.setWishDir(ghost, LEFT);

            // don't change direction directly when outside house
            ghost.worldNavigation().setNewTileEntered(false);

            return true;
        }
        else {
            final float centerX = position.x + WorldMap.HTS;
            final float houseCenterX = house.center().x();
            if (differsAtMost(0.5f * speed, centerX, houseCenterX)) {
                // align horizontally and raise
                position.setX(houseCenterX - WorldMap.HTS);
                sys.navigator.setMoveDir(ghost, UP);
                sys.navigator.setWishDir(ghost, UP);
            }
            else {
                // move sidewards until center axis is reached
                sys.navigator.setMoveDir(ghost, centerX < houseCenterX ? RIGHT : LEFT);
                sys.navigator.setWishDir(ghost, centerX < houseCenterX ? RIGHT : LEFT);
            }

            sys.navigator.setSpeed(ghost, speed);
            sys.motor.moveAccelerated(ghost);

            return false;
        }
    }

    /**
     * When an eaten ghost has arrived at the ghost house door, he falls down to the center of the house,
     * then moves up again (if the house center is his revival position), or moves sidewards towards his revival position.
     */
    public void enterHouse(GameContext gameContext, Ghost ghost, float speed) {
        final GameSystems sys = gameContext.systems();

        final Position position = ghost.position();
        final House house = ghost.worldPlacement().house();
        final Vector2f revivalPosition = WorldMap.halfTileRightOf(house.ghostRevivalTile(ghost.personality()));
        final Vector2f positionVec = position.asVector2f();
        if (positionVec.roughlyEquals(revivalPosition, 0.5f * speed, 0.5f * speed)) {
            position.set(revivalPosition.x(), revivalPosition.y());
            sys.navigator.setMoveDir(ghost, UP);
            sys.navigator.setWishDir(ghost, UP);

            sys.ghostState.changeState(gameContext, ghost, GhostState.LOCKED);
            return;
        }
        if (position.y < revivalPosition.y()) {
            sys.navigator.setMoveDir(ghost, DOWN);
            sys.navigator.setWishDir(ghost, DOWN);
        }
        else if (position.x > revivalPosition.x()) {
            sys.navigator.setMoveDir(ghost, LEFT);
            sys.navigator.setWishDir(ghost, LEFT);
        }
        else if (position.x < revivalPosition.x()) {
            sys.navigator.setMoveDir(ghost, RIGHT);
            sys.navigator.setWishDir(ghost, RIGHT);
        }
        sys.navigator.setSpeed(ghost, speed);

        sys.motor.moveAccelerated(ghost);
    }

    public void reachHouse(GameContext gameContext, Ghost ghost, float speed) {
        final GameSystems sys = gameContext.systems();

        final Position position = ghost.position();
        final House house = ghost.worldPlacement().house();
        final Vector2f houseEntry = house.entryPosition();
        final Vector2f positionVec =  position.asVector2f();
        if (positionVec.roughlyEquals(houseEntry, speed, 0)) {
            position.set(houseEntry.x(), houseEntry.y());
            sys.navigator.setMoveDir(ghost, DOWN);
            sys.navigator.setWishDir(ghost, DOWN);

            //TODO check if this should be done here
            sys.ghostState.changeState(gameContext, ghost, GhostState.ENTERING_HOUSE);
        }
        else {
            //TODO use system method
            ghost.worldNavigation().setTargetTile(house.leftDoorTile());
            sys.navigator.setSpeed(ghost, speed);
            sys.navigator.navigateTowardsTarget(ghost, gameContext);
            sys.navigator.tryMovingOrTeleporting(ghost, gameContext);
        }
    }
}
