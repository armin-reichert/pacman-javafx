/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.component.world;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.component.EntityComponent;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.List;
import java.util.Optional;

import static de.amr.basics.math.Direction.RIGHT;

public class WorldNavigation implements EntityComponent {

    public static final Direction DEFAULT_MOVE_DIR = RIGHT;
    public static final Direction DEFAULT_WISH_DIR = RIGHT;
    public static final Vector2i DEFAULT_TARGET_TILE = null;
    public static final boolean DEFAULT_CAN_TELEPORT = true;

    /** Order in which directions are selected when navigation decision is met. */
    public static final List<Direction> NAVIGATION_ORDER = List.of(Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT);

    private ObjectProperty<Direction> moveDir;
    private ObjectProperty<Direction> wishDir;
    private ObjectProperty<Vector2i> targetTile;

    private boolean newTileEntered;
    private boolean turnBackRequested;
    private boolean canTeleport = DEFAULT_CAN_TELEPORT;

    //TODO this is just a primitive way to provide cornering speed differences
    public float corneringSpeedDelta;

    //TODO: store in frame context?
    public final WorldMovementInfo info = new WorldMovementInfo();

    public WorldNavigation() {}

    @Override
    public void reset() {
        moveDirProperty().setValue(DEFAULT_MOVE_DIR);
        wishDirProperty().setValue(DEFAULT_WISH_DIR);
        targetTileProperty().setValue(DEFAULT_TARGET_TILE);
        newTileEntered = false;
        turnBackRequested = false;
        canTeleport = DEFAULT_CAN_TELEPORT;
        info.clear();
    }

    @Override
    public String toString() {
        return "WorldMovement{" +
            "moveDir=" + moveDir() +
            ", wishDir=" + wishDir() +
            ", targetTile=" + targetTile() +
            ", newTileEntered=" + newTileEntered +
            ", turnBackRequested=" + turnBackRequested +
            ", canTeleport=" + canTeleport +
            ", corneringSpeedDelta=" + corneringSpeedDelta +
            ", info=" + info +
            '}';
    }

    public final ObjectProperty<Vector2i> targetTileProperty() {
        if (targetTile == null) {
            targetTile = new SimpleObjectProperty<>(DEFAULT_TARGET_TILE);
        }
        return targetTile;
    }

    /**
     * Sets the tile this actor tries to reach (can be an unreachable tile or <code>null</code>).
     *
     * @param tile some tile or <code>null</code>
     */
    public void setTargetTile(Vector2i tile) {
        targetTileProperty().set(tile);
    }

    /**
     * @return current target tile. Can be null, an inaccessible tile or a tile outside the world.
     */
    public Vector2i targetTile() {
        return targetTile != null ? targetTile.get() : DEFAULT_TARGET_TILE;
    }

    /**
     * @return (Optional) target tile. Can be inaccessible or outside the world.
     */
    public Optional<Vector2i> optTargetTile() {
        return Optional.ofNullable(targetTile());
    }


    public final ObjectProperty<Direction> moveDirProperty() {
        if (moveDir == null) {
            moveDir = new SimpleObjectProperty<>(DEFAULT_MOVE_DIR);
        }
        return moveDir;
    }

    /**
     * @return The current move direction.
     */
    public Direction moveDir() {
        return moveDir != null ? moveDir.get() : DEFAULT_MOVE_DIR;
    }

    public final ObjectProperty<Direction> wishDirProperty() {
        if (wishDir == null) {
            wishDir = new SimpleObjectProperty<>(DEFAULT_WISH_DIR);
        }
        return wishDir;
    }

    public Direction wishDir() {
        return wishDir != null ? wishDir.get() : DEFAULT_WISH_DIR;
    }

    public boolean isNewTileEntered() {
        return newTileEntered;
    }

    public void setNewTileEntered(boolean newTileEntered) {
        this.newTileEntered = newTileEntered;
    }

    public void setCanTeleport(boolean canTeleport) {
        this.canTeleport = canTeleport;
    }

    public boolean canTeleport() {
        return canTeleport;
    }

    public boolean isTurnBackRequested() {
        return turnBackRequested;
    }

    public void setTurnBackRequested(boolean value) {
        this.turnBackRequested = value;
    }
}
