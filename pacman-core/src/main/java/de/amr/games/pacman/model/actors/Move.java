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


public abstract class Move extends Entity{
    protected static final Direction[] DIRECTION_PRIORITY = {UP, LEFT, DOWN, RIGHT};

    protected String name;
    protected Direction moveDir;
    protected Direction wishDir;
    protected Vector2i targetTile;
    protected float baseSpeed;
    protected World world;
    protected final MoveResult moveResult = new MoveResult();
    protected boolean newTileEntered;
    protected boolean gotReverseCommand;
    protected boolean canTeleport;
    protected float corneringSpeedUp;

    public World world() {
        return world;
    }

    public void navigateTowardsTarget() {
        if (targetTile == null) {
            return;
        }
        if (!newTileEntered && hasMoved()) {
            return; // we don't need no navigation, dim dit diddit diddit dim dit diddit diddit...
        }
        if (world().belongsToPortal(tile())) {
            return; // inside portal, no navigation happens
        }
        computeTargetDirection().ifPresent(this::setWishDir);
    }
    public void setWishDir(Direction dir) {
        checkDirectionNotNull(dir);
        if (wishDir != dir) {
            wishDir = dir;
            Logger.trace("{}: New wishDir: {}. {}", name, wishDir, this);
        }
    }
    private Optional<Direction> computeTargetDirection() {
        final var currentTile = tile();
        Direction targetDir = null;
        float minDistance = Float.MAX_VALUE;
        for (var dir : DIRECTION_PRIORITY) {
            if (dir == moveDir.opposite()) {
                continue; // reversing the move direction is not allowed
            }
            final var neighborTile = currentTile.plus(dir.vector());
            if (canAccessTile(neighborTile)) {
                final float distance = neighborTile.euclideanDistance(targetTile);
                if (distance < minDistance) {
                    minDistance = distance;
                    targetDir = dir;
                }
            }
        }
        return Optional.ofNullable(targetDir);
    }
    
    public boolean hasMoved() {
        return moveResult.moved;
    }




    public abstract boolean canAccessTile(Vector2i tile);
    public abstract void setPercentageSpeed(byte tile);
    public abstract boolean isNewTileEntered();
    public abstract void placeAtTile(Vector2i tile, float ox, float oy);

    public Direction moveDir() {
        return moveDir;
    }
    public void setMoveDir(Direction dir) {
        checkDirectionNotNull(dir);
        if (moveDir != dir) {
            moveDir = dir;
            setVelocity(moveDir.vector().toFloatVec().scaled(velocity().length()));
            Logger.trace("{}: New moveDir: {}. {}", name, moveDir, this);
        }
    }

    public void roam(World world, byte relSpeed, Direction dir) {
        if (!world.belongsToPortal(tile()) && (isNewTileEntered() || !hasMoved())) {
            while (dir == moveDir().opposite() || !canAccessTile(tile().plus(dir.vector()))) {
                dir = dir.nextClockwise();
            }
            setWishDir(dir);
        }
        setPercentageSpeed(relSpeed);
        tryMoving();
    }


    public void tryMoving() {
        moveResult.clear();
        tryTeleport(world.portals());
        if (!moveResult.teleported) {
            executeReverseCommand();
            tryMoving(wishDir);
            if (moveResult.moved) {
                setMoveDir(wishDir);
            } else {
                tryMoving(moveDir);
            }
        }
        if (moveResult.teleported || moveResult.moved) {
            Logger.trace("{}: {} {} {}", name, moveResult, moveResult.summary(), this);
        }
    }
    public abstract boolean canReverse();

    private void executeReverseCommand() {
        if (gotReverseCommand && canReverse()) {
            setWishDir(moveDir.opposite());
            gotReverseCommand = false;
            Logger.trace("{}: [turned around]", name);
        }
    }

    private void tryTeleport(List<Portal> portals) {
        if (canTeleport) {
            for (var portal : portals) {
                tryTeleport(portal);
                if (moveResult.teleported) {
                    return;
                }
            }
        }
    }

    private void tryTeleport(Portal portal) {
        var tile = tile();
        var old_pos_x = posX;
        var old_pos_y = posY;
        if (tile.y() == portal.leftTunnelEnd().y() && posX < portal.leftTunnelEnd().x() - portal.depth() * TS) {
            centerOverTile(portal.rightTunnelEnd());
            moveResult.teleported = true;
            moveResult.addMessage(String.format("%s: Teleported from (%.2f,%.2f) to (%.2f,%.2f)",
                name, old_pos_x, old_pos_y, posX, posY));
        } else if (tile.equals(portal.rightTunnelEnd().plus(portal.depth(), 0))) {
            centerOverTile(portal.leftTunnelEnd().minus(portal.depth(), 0));
            moveResult.teleported = true;
            moveResult.addMessage(String.format("%s: Teleported from (%.2f,%.2f) to (%.2f,%.2f)",
                name, old_pos_x, old_pos_y, posX, posY));
        }
    }
    public void centerOverTile(Vector2i tile) {
        placeAtTile(tile, 0, 0);
    }

    private void tryMoving(Direction dir) {
        final Vector2i tileBeforeMove = tile();
        final Vector2f dirVector = dir.vector().toFloatVec();
        final Vector2f newVelocity = dirVector.scaled(velocity().length());
        final Vector2f touchPosition = center().plus(dirVector.scaled(HTS)).plus(newVelocity);
        final Vector2i touchedTile = tileAt(touchPosition);
        final boolean isTurn = !dir.sameOrientation(moveDir);

        if (!canAccessTile(touchedTile)) {
            if (!isTurn) {
                centerOverTile(tile()); // adjust over tile (would move forward against wall)
            }
            moveResult.addMessage(String.format("Cannot move %s into tile %s", dir, touchedTile));
            return;
        }

        if (isTurn) {
            float offset = dir.isHorizontal() ? offset().y() : offset().x();
            boolean atTurnPosition = Math.abs(offset) <= 1; // TODO <= pixel-speed?
            if (atTurnPosition) {
                centerOverTile(tile()); // adjust over tile (starts moving around corner)
            } else {
                moveResult.addMessage(String.format("Wants to take corner towards %s but not at turn position", dir));
                return;
            }
        }

        if (isTurn && corneringSpeedUp > 0) {
            setVelocity(newVelocity.plus(dirVector.scaled(corneringSpeedUp)));
            Logger.trace("{} velocity around corner: {}", this.name, velocity().length());
            move();
            setVelocity(newVelocity);
        } else {
            setVelocity(newVelocity);
            move();
        }

        Vector2i currentTile = tile();

        newTileEntered = !tileBeforeMove.equals(currentTile);
        moveResult.moved = true;
        moveResult.tunnelEntered = world.isTunnel(currentTile)
            && !world.isTunnel(tileBeforeMove)
            && !world.belongsToPortal(tileBeforeMove);
        moveResult.tunnelLeft = !world.isTunnel(currentTile)
            && world.isTunnel(tileBeforeMove)
            && !world.belongsToPortal(currentTile);

        moveResult.addMessage(String.format("%5s (%.2f pixels)", dir, newVelocity.length()));

        if (moveResult.tunnelEntered) {
            Logger.trace("{} entered tunnel", name);
        }
        if (moveResult.tunnelLeft) {
            Logger.trace("{} left tunnel", name);
        }
    }

    public boolean hasTeleported() {
        return moveResult.teleported;
    }

    public boolean hasEnteredTunnel() {
        return moveResult.tunnelEntered;
    }

    public boolean hasLeftTunnel() {
        return moveResult.tunnelLeft;
    }
    /**
     * Sets the tile this creature tries to reach (can be an unreachable tile or <code>null</code>).
     *
     * @param tile some tile or <code>null</code>
     */
    public void setTargetTile(Vector2i tile) {
        targetTile = tile;
    }
    public void followTarget(Vector2i targetTile, byte relSpeed) {
        setPercentageSpeed(relSpeed);
        setTargetTile(targetTile);
        navigateTowardsTarget();
        tryMoving();
    }


}