/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.system;

import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.ecs.comp.PositionComp;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostHouseAccessComp;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;

import static de.amr.basics.math.Direction.*;
import static de.amr.pacmanfx.core.Validations.differsAtMost;

public class GhostHouseAccessSystem {

    private final WorldNavigationSystem navigator;
    private final GhostWorldMovementPolicy movementPolicy;
    private final MovementSystem motor;

    public GhostHouseAccessSystem(WorldNavigationSystem navigator, GhostWorldMovementPolicy movementPolicy, MovementSystem motor) {
        this.navigator = navigator;
        this.movementPolicy = movementPolicy;
        this.motor = motor;
    }

    public void update(Ghost ghost, GameLevel level, float speed) {
        switch (ghost.state().enumValue()) {
            case LOCKED         -> bounceInHouse(ghost, speed);
            case LEAVING_HOUSE  -> moveTowardsHouseExit(ghost, speed);
            case RETURNING_HOME -> moveTowardsHouse(ghost, level, speed);
            case ENTERING_HOUSE -> moveTowardsRevivalPosition(ghost, speed);
        }
    }

    /**
     * In locked state, ghosts inside the house are bouncing up and down. They become blue when Pac-Man gets power
     * and start blinking when Pac-Man's power starts fading. After that, they return to their normal color.
     */
    private void bounceInHouse(Ghost ghost, float speed) {
        if (ghost.reqComp(WorldNavigationComp.class).isDisabled()) {
            return;
        }
        final House house = ghost.worldInfo().house();
        final PositionComp position = ghost.pos();
        if (house.isVisitedBy(ghost)) {
            final float minY = (house.floorplan().minTile().y() + 1) * WorldMap.TS + WorldMap.HTS;
            final float maxY = (house.floorplan().maxTile().y() - 1) * WorldMap.TS - WorldMap.HTS;
            if (position.y() <= minY) {
                navigator.setMoveDir(ghost, DOWN);
                navigator.setWishDir(ghost, DOWN);
            }
            else if (position.y() >= maxY) {
                navigator.setMoveDir(ghost, UP);
                navigator.setWishDir(ghost, UP);
            }
            position.setY(Math.clamp(position.y(), minY, maxY));
            navigator.setMoveDirSpeed(ghost, speed);
            motor.move(ghost);
        }
    }

    /**
     * When a ghost leaves the house, he follows a specific route from his home/revival position to the house exit.
     * In the Arcade versions of Pac-Man and Ms.Pac-Man, the ghost first moves towards the vertical center of the house
     * and then raises up until he has passed the door on top of the house.
     * <p>
     * The ghost speed is slower than outside, but I do not know the exact value.
     */
    private void moveTowardsHouseExit(Ghost ghost, float speed) {
        if (ghost.reqComp(WorldNavigationComp.class).isDisabled()) {
            return;
        }

        final PositionComp position = ghost.pos();
        final House house = ghost.worldInfo().house();
        final Vector2f houseEntryPosition = house.floorplan().entryPosition();

        if (position.y() <= houseEntryPosition.y()) {
            position.setY(houseEntryPosition.y());
            navigator.setMoveDir(ghost, LEFT);
            navigator.setWishDir(ghost, LEFT);

            // don't change direction directly when outside house
            ghost.worldNavigation().setNewTileEntered(false);
            ghost.reqComp(GhostHouseAccessComp.class).setLeftHouse(true);
        }
        else {
            final float centerX = position.x() + WorldMap.HTS;
            final float houseCenterX = house.center().x();
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

            navigator.setMoveDirSpeed(ghost, speed);
            motor.move(ghost);

            ghost.reqComp(GhostHouseAccessComp.class).setLeftHouse(false);
        }
    }

    /**
     * When an eaten ghost has arrived at the ghost house door, he falls down to the center of the house,
     * then moves up again (if the house center is his revival position), or moves sidewards towards his revival position.
     */
    private void moveTowardsRevivalPosition(Ghost ghost, float speed) {
        if (ghost.reqComp(WorldNavigationComp.class).isDisabled()) {
            return;
        }

        final PositionComp position = ghost.pos();
        final House house = ghost.worldInfo().house();
        final Vector2f revivalPosition = WorldMap.halfTileRightOf(house.floorplan().ghostRevivalTile(ghost.personality()));
        final Vector2f positionVec = position.asVector2f();

        if (positionVec.roughlyEquals(revivalPosition, 0.5f * speed, 0.5f * speed)) {
            position.set(revivalPosition.x(), revivalPosition.y());
            navigator.setMoveDir(ghost, UP);
            navigator.setWishDir(ghost, UP);
            ghost.reqComp(GhostHouseAccessComp.class).setReachedRevivalPosition(true);
            return;
        }

        if (position.y() < revivalPosition.y()) {
            navigator.setMoveDir(ghost, DOWN);
            navigator.setWishDir(ghost, DOWN);
        }
        else if (position.x() > revivalPosition.x()) {
            navigator.setMoveDir(ghost, LEFT);
            navigator.setWishDir(ghost, LEFT);
        }
        else if (position.x() < revivalPosition.x()) {
            navigator.setMoveDir(ghost, RIGHT);
            navigator.setWishDir(ghost, RIGHT);
        }
        navigator.setMoveDirSpeed(ghost, speed);
        motor.move(ghost);

        ghost.reqComp(GhostHouseAccessComp.class).setReachedRevivalPosition(false);
    }

    private void moveTowardsHouse(Ghost ghost, GameLevel level, float speed) {
        final PositionComp ghostPos = ghost.pos();
        final House house = ghost.worldInfo().house();
        final Vector2f entryPos = house.floorplan().entryPosition();

        if (ghostPos.asVector2f().roughlyEquals(entryPos, speed, 0)) {
            ghostPos.set(entryPos);
            navigator.setMoveDir(ghost, DOWN);
            navigator.setWishDir(ghost, DOWN);
            ghost.reqComp(GhostHouseAccessComp.class).setReachedHouseEntry(true);
        }
        else {
            navigator.setTargetTile(ghost, house.floorplan().leftDoorTile());
            navigator.setWishDirTowardsTargetTile(ghost, level, movementPolicy);
            navigator.setMoveDirSpeed(ghost, speed);
            navigator.tryMovingOrTeleporting(level, ghost, motor, movementPolicy);
            ghost.reqComp(GhostHouseAccessComp.class).setReachedHouseEntry(false);
        }
    }
}
