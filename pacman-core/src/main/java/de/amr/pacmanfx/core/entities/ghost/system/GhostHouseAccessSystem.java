/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.system;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.comp.PositionComp;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.WorldMovementPolicy;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.GameSession;

import static de.amr.basics.math.Direction.*;
import static de.amr.pacmanfx.core.Validations.differsAtMost;

public class GhostHouseAccessSystem {

    /**
     * In locked state, ghosts inside the house are bouncing up and down. They become blue when Pac-Man gets power
     * and start blinking when Pac-Man's power starts fading. After that, they return to their normal color.
     */
    public void stayInHouse(GameContext game, Ghost ghost, float speed) {
        final GameSystems systems = game.variant().systems();

        final House house = ghost.worldInfo().house();
        final PositionComp position = ghost.pos();

        if (house.isVisitedBy(ghost)) {
            // locked inside house: jumping
            final float minY = (house.floorplan().minTile().y() + 1) * WorldMap.TS + WorldMap.HTS;
            final float maxY = (house.floorplan().maxTile().y() - 1) * WorldMap.TS - WorldMap.HTS;
            if (position.y() <= minY) {
                systems.worldNavigator().setMoveDir(ghost, DOWN);
                systems.worldNavigator().setWishDir(ghost, DOWN);
            }
            else if (position.y() >= maxY) {
                systems.worldNavigator().setMoveDir(ghost, UP);
                systems.worldNavigator().setWishDir(ghost, UP);
            }
            position.setY(Math.clamp(position.y(), minY, maxY));
            systems.worldNavigator().setSpeed(ghost, speed);
            systems.motor().move(ghost);
        }
        else {
            // locked outside of house: standing still
            systems.worldNavigator().setSpeed(ghost, 0);
        }
    }

    /**
     * When a ghost leaves the house, he follows a specific route from his home/revival position to the house exit.
     * In the Arcade versions of Pac-Man and Ms.Pac-Man, the ghost first moves towards the vertical center of the house
     * and then raises up until he has passed the door on top of the house.
     * <p>
     * The ghost speed is slower than outside, but I do not know the exact value.
     */
    public boolean leaveHouse(GameContext game, Ghost ghost, float speed) {
        final GameSystems systems = game.variant().systems();

        final PositionComp position = ghost.pos();
        final House house = ghost.worldInfo().house();
        final Vector2f houseEntryPosition = house.floorplan().entryPosition();

        if (position.y() <= houseEntryPosition.y()) {
            position.setY(houseEntryPosition.y());
            systems.worldNavigator().setMoveDir(ghost, LEFT);
            systems.worldNavigator().setWishDir(ghost, LEFT);

            // don't change direction directly when outside house
            ghost.worldNavigation().setNewTileEntered(false);

            return true;
        }
        else {
            final float centerX = position.x() + WorldMap.HTS;
            final float houseCenterX = house.center().x();
            if (differsAtMost(0.5f * speed, centerX, houseCenterX)) {
                // align horizontally and raise
                position.setX(houseCenterX - WorldMap.HTS);
                systems.worldNavigator().setMoveDir(ghost, UP);
                systems.worldNavigator().setWishDir(ghost, UP);
            }
            else {
                // move sidewards until center axis is reached
                systems.worldNavigator().setMoveDir(ghost, centerX < houseCenterX ? RIGHT : LEFT);
                systems.worldNavigator().setWishDir(ghost, centerX < houseCenterX ? RIGHT : LEFT);
            }

            systems.worldNavigator().setSpeed(ghost, speed);
            systems.motor().move(ghost);

            return false;
        }
    }

    /**
     * When an eaten ghost has arrived at the ghost house door, he falls down to the center of the house,
     * then moves up again (if the house center is his revival position), or moves sidewards towards his revival position.
     */
    public void enterHouse(GameContext game, Ghost ghost, float speed) {
        final GameSystems systems = game.variant().systems();

        final PositionComp position = ghost.pos();
        final House house = ghost.worldInfo().house();
        final Vector2f revivalPosition = WorldMap.halfTileRightOf(
            house.floorplan().ghostRevivalTile(ghost.personality()));
        final Vector2f positionVec = position.asVector2f();
        if (positionVec.roughlyEquals(revivalPosition, 0.5f * speed, 0.5f * speed)) {
            position.set(revivalPosition.x(), revivalPosition.y());
            systems.worldNavigator().setMoveDir(ghost, UP);
            systems.worldNavigator().setWishDir(ghost, UP);

            systems.ghostState().changeState(ghost, GhostState.LOCKED);
            return;
        }
        if (position.y() < revivalPosition.y()) {
            systems.worldNavigator().setMoveDir(ghost, DOWN);
            systems.worldNavigator().setWishDir(ghost, DOWN);
        }
        else if (position.x() > revivalPosition.x()) {
            systems.worldNavigator().setMoveDir(ghost, LEFT);
            systems.worldNavigator().setWishDir(ghost, LEFT);
        }
        else if (position.x() < revivalPosition.x()) {
            systems.worldNavigator().setMoveDir(ghost, RIGHT);
            systems.worldNavigator().setWishDir(ghost, RIGHT);
        }
        systems.worldNavigator().setSpeed(ghost, speed);

        systems.motor().move(ghost);
    }

    //TODO extract state change
    public void reachHouse(GameContext game, Ghost ghost, float speed) {
        final GameSession session = game.session();
        final GameSystems systems = game.variant().systems();
        final WorldMovementPolicy policy = systems.ghostWorldMovementPolicy();
        final GameLevel level = session.assertLevel();

        final PositionComp position = ghost.pos();
        final House house = ghost.worldInfo().house();
        final Vector2f houseEntry = house.floorplan().entryPosition();
        final Vector2f positionVec =  position.asVector2f();
        if (positionVec.roughlyEquals(houseEntry, speed, 0)) {
            position.set(houseEntry.x(), houseEntry.y());
            systems.worldNavigator().setMoveDir(ghost, DOWN);
            systems.worldNavigator().setWishDir(ghost, DOWN);

            //TODO check if this should be done here
            systems.ghostState().changeState(ghost, GhostState.ENTERING_HOUSE);

        }
        else {
            //TODO use system method
            ghost.worldNavigation().setTargetTile(house.floorplan().leftDoorTile());
            systems.worldNavigator().setSpeed(ghost, speed);
            systems.worldNavigator().navigateTowardsTarget(ghost, level, policy);
            systems.worldNavigator().tryMovingOrTeleporting(ghost, level, policy);
        }
    }
}
