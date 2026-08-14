/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs.comp;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.GameEntityComponent;

import java.util.List;
import java.util.Optional;

import static de.amr.basics.math.Direction.RIGHT;

public class WorldNavigationComp implements GameEntityComponent {

    public static final Direction DEFAULT_MOVE_DIR = RIGHT;
    public static final Direction DEFAULT_WISH_DIR = RIGHT;
    public static final boolean DEFAULT_CAN_TELEPORT = true;

    /** Order in which directions are selected when navigation decision is met. */
    public static final List<Direction> NAVIGATION_ORDER = List.of(Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT);

    private Direction moveDir;
    private Direction wishDir;

    private Vector2i targetTile;

    private boolean newTileEntered;
    private boolean turnBackRequested;
    private boolean canTeleport = DEFAULT_CAN_TELEPORT;
    private boolean inTeleportingSpace;

    //TODO this is just a primitive way to provide cornering speed differences
    public float corneringSpeedDelta;

    //TODO: store in frame state?
    private final WorldNavigationInfo info = new WorldNavigationInfo();

    public WorldNavigationComp() {}

    @Override
    public void reset() {
        setMoveDir(DEFAULT_MOVE_DIR);
        setWishDir(DEFAULT_WISH_DIR);
        targetTile = null;
        newTileEntered = false;
        turnBackRequested = false;
        canTeleport = DEFAULT_CAN_TELEPORT;
        inTeleportingSpace = false;
        info.clear();
    }

    @Override
    public String toString() {
        return "WorldNavigation{" +
            "moveDir=" + moveDir() +
            ", wishDir=" + wishDir() +
            ", targetTile=" + targetTile() +
            ", newTileEntered=" + newTileEntered +
            ", turnBackRequested=" + turnBackRequested +
            ", canTeleport=" + canTeleport +
            ", corneringSpeedDelta=" + corneringSpeedDelta +
            '}';
    }

    /**
     * Sets the tile this actor tries to reach (can be an unreachable tile or <code>null</code>).
     *
     * @param tile some tile or <code>null</code>
     */
    public void setTargetTile(Vector2i tile) {
        targetTile = tile;
    }

    /**
     * @return current target tile. Can be null, an inaccessible tile or a tile outside the world.
     */
    public Vector2i targetTile() {
        return targetTile;
    }

    /**
     * @return (Optional) target tile. Can be inaccessible or outside the world.
     */
    public Optional<Vector2i> optTargetTile() {
        return Optional.ofNullable(targetTile);
    }

    public void setMoveDir(Direction moveDir) {
        this.moveDir = moveDir;
    }

    public Direction moveDir() {
        return moveDir;
    }

    public void setWishDir(Direction wishDir) {
        this.wishDir = wishDir;
    }

    public Direction wishDir() {
        return wishDir;
    }

    public void setNewTileEntered(boolean newTileEntered) {
        this.newTileEntered = newTileEntered;
    }

    public boolean isNewTileEntered() {
        return newTileEntered;
    }

    public void setCanTeleport(boolean canTeleport) {
        this.canTeleport = canTeleport;
    }

    public boolean canTeleport() {
        return canTeleport;
    }

    public void setInTeleportingSpace(boolean inTeleportingSpace) {
        this.inTeleportingSpace = inTeleportingSpace;
    }

    public boolean inTeleportingSpace() {
        return inTeleportingSpace;
    }

    public boolean isTurnBackRequested() {
        return turnBackRequested;
    }

    public void setTurnBackRequested(boolean value) {
        this.turnBackRequested = value;
    }
}
