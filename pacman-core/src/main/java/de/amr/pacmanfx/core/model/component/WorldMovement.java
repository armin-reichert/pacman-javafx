package de.amr.pacmanfx.core.model.component;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.actors.WorldMovementInfo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.List;
import java.util.Optional;

import static de.amr.basics.math.Direction.RIGHT;

public class WorldMovement implements EntityComponent {

    //TODO create systems in game context...
    public static final WorldMovementSystem SYSTEM = new WorldMovementSystem();

    public static final Direction DEFAULT_MOVE_DIR = RIGHT;
    public static final Direction DEFAULT_WISH_DIR = RIGHT;
    public static final Vector2i DEFAULT_TARGET_TILE = null;
    public static final boolean DEFAULT_CAN_TELEPORT = true;

    /** Order in which directions are selected when navigation decision is met. */
    public static final List<Direction> NAVIGATION_ORDER = List.of(Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT);

    public ObjectProperty<Direction> moveDir;
    public ObjectProperty<Direction> wishDir;
    public ObjectProperty<Vector2i> targetTile;

    public boolean newTileEntered;
    public boolean turnBackRequested;
    public boolean canTeleport = DEFAULT_CAN_TELEPORT;

    //TODO this is just a primitive way to provide cornering speed differences
    public float corneringSpeedDelta;

    //TODO: store in frame context?
    public final WorldMovementInfo info = new WorldMovementInfo();

    public WorldMovement() {
    }

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
        return "MazeMovement{" +
            ", moveDir=" + moveDir() +
            ", wishDir=" + wishDir() +
            ", targetTile=" + targetTile() +
            ", newTileEntered=" + newTileEntered +
            ", turnBackRequested=" + turnBackRequested +
            ", canTeleport=" + canTeleport +
            ", corneringSpeedDelta" + corneringSpeedDelta +
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

    /**
     * @return The wish direction. Will be taken as soon as possible.
     */
    public Direction wishDir() {
        return wishDir != null ? wishDir.get() : DEFAULT_WISH_DIR;
    }

    public boolean isNewTileEntered() {
        return newTileEntered;
    }

    /**
     * @return {@code true} if the ghost should revert its direction at the next occasion
     */
    public boolean turnBackRequested() {
        return turnBackRequested;
    }

    /**
     * Signals that this actor should reverse its move direction as soon as possible.
     */
    public void requestTurnBack() {
        turnBackRequested = true;
        Logger.debug("Turn back ASAP! {}", this);
    }
}
