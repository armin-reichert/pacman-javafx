/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.system;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.ecs.comp.PositionComp;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.ecs.systems.WorldMovementPolicy;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;

import java.util.Optional;

import static de.amr.basics.math.Direction.*;
import static de.amr.pacmanfx.core.Validations.differsAtMost;

public class GhostHouseAccessSystem {

    /**
     * In locked state, ghosts inside the house are bouncing up and down. They become blue when Pac-Man gets power
     * and start blinking when Pac-Man's power starts fading. After that, they return to their normal color.
     */
    public void stayInHouse(Ghost ghost, WorldNavigationSystem worldNavigationSystem, MovementSystem motor, float speed) {
        final House house = ghost.worldInfo().house();
        final PositionComp position = ghost.pos();

        if (house.isVisitedBy(ghost)) {
            // locked inside house: jumping
            final float minY = (house.floorplan().minTile().y() + 1) * WorldMap.TS + WorldMap.HTS;
            final float maxY = (house.floorplan().maxTile().y() - 1) * WorldMap.TS - WorldMap.HTS;
            if (position.y() <= minY) {
                worldNavigationSystem.setMoveDir(ghost, DOWN);
                worldNavigationSystem.setWishDir(ghost, DOWN);
            }
            else if (position.y() >= maxY) {
                worldNavigationSystem.setMoveDir(ghost, UP);
                worldNavigationSystem.setWishDir(ghost, UP);
            }
            position.setY(Math.clamp(position.y(), minY, maxY));
            worldNavigationSystem.setMoveDirSpeed(ghost, speed);
            motor.move(ghost);
        }
        else {
            // locked outside of house: standing still
            worldNavigationSystem.setMoveDirSpeed(ghost, 0);
        }
    }

    /**
     * When a ghost leaves the house, he follows a specific route from his home/revival position to the house exit.
     * In the Arcade versions of Pac-Man and Ms.Pac-Man, the ghost first moves towards the vertical center of the house
     * and then raises up until he has passed the door on top of the house.
     * <p>
     * The ghost speed is slower than outside, but I do not know the exact value.
     */
    public boolean leaveHouse(Ghost ghost, WorldNavigationSystem worldNavigationSystem, MovementSystem motor, float speed) {
        final PositionComp position = ghost.pos();
        final House house = ghost.worldInfo().house();
        final Vector2f houseEntryPosition = house.floorplan().entryPosition();

        if (position.y() <= houseEntryPosition.y()) {
            position.setY(houseEntryPosition.y());
            worldNavigationSystem.setMoveDir(ghost, LEFT);
            worldNavigationSystem.setWishDir(ghost, LEFT);

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
                worldNavigationSystem.setMoveDir(ghost, UP);
                worldNavigationSystem.setWishDir(ghost, UP);
            }
            else {
                // move sidewards until center axis is reached
                worldNavigationSystem.setMoveDir(ghost, centerX < houseCenterX ? RIGHT : LEFT);
                worldNavigationSystem.setWishDir(ghost, centerX < houseCenterX ? RIGHT : LEFT);
            }

            worldNavigationSystem.setMoveDirSpeed(ghost, speed);
            motor.move(ghost);

            return false;
        }
    }

    /**
     * When an eaten ghost has arrived at the ghost house door, he falls down to the center of the house,
     * then moves up again (if the house center is his revival position), or moves sidewards towards his revival position.
     */
    public Optional<GhostState> enterHouse(
        Ghost ghost,
        WorldNavigationSystem worldNavigationSystem,
        MovementSystem motor,
        float speed)
    {
        final PositionComp position = ghost.pos();
        final House house = ghost.worldInfo().house();
        final Vector2f revivalPosition = WorldMap.halfTileRightOf(
            house.floorplan().ghostRevivalTile(ghost.personality()));
        final Vector2f positionVec = position.asVector2f();
        if (positionVec.roughlyEquals(revivalPosition, 0.5f * speed, 0.5f * speed)) {
            position.set(revivalPosition.x(), revivalPosition.y());
            worldNavigationSystem.setMoveDir(ghost, UP);
            worldNavigationSystem.setWishDir(ghost, UP);

            return Optional.of(GhostState.LOCKED);
        }
        if (position.y() < revivalPosition.y()) {
            worldNavigationSystem.setMoveDir(ghost, DOWN);
            worldNavigationSystem.setWishDir(ghost, DOWN);
        }
        else if (position.x() > revivalPosition.x()) {
            worldNavigationSystem.setMoveDir(ghost, LEFT);
            worldNavigationSystem.setWishDir(ghost, LEFT);
        }
        else if (position.x() < revivalPosition.x()) {
            worldNavigationSystem.setMoveDir(ghost, RIGHT);
            worldNavigationSystem.setWishDir(ghost, RIGHT);
        }
        worldNavigationSystem.setMoveDirSpeed(ghost, speed);

        motor.move(ghost);

        return Optional.empty();
    }

    //TODO extract state change
    public Optional<GhostState> reachHouse(
        GameLevel level,
        Ghost ghost,
        WorldNavigationSystem worldNavigationSystem,
        WorldMovementPolicy movementPolicy,
        MovementSystem motor,
        float speed)
    {
        final PositionComp position = ghost.pos();
        final House house = ghost.worldInfo().house();
        final Vector2f houseEntry = house.floorplan().entryPosition();
        final Vector2f positionVec =  position.asVector2f();
        if (positionVec.roughlyEquals(houseEntry, speed, 0)) {
            position.set(houseEntry.x(), houseEntry.y());
            worldNavigationSystem.setMoveDir(ghost, DOWN);
            worldNavigationSystem.setWishDir(ghost, DOWN);
            return Optional.of(GhostState.ENTERING_HOUSE);
        }

        //TODO check if this should be done here
        //ghostStateSystem.changeState(ghost, GhostState.ENTERING_HOUSE);
        //TODO use system method
        ghost.worldNavigation().setTargetTile(house.floorplan().leftDoorTile());
        worldNavigationSystem.setMoveDirSpeed(ghost, speed);
        worldNavigationSystem.navigateTowardsTarget(ghost, level, movementPolicy);
        worldNavigationSystem.tryMovingOrTeleporting(level, ghost, motor, movementPolicy);

        return Optional.empty();
    }
}
