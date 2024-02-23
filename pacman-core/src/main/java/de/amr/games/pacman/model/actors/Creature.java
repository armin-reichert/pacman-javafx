/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.world.Portal;
import de.amr.games.pacman.model.world.World;
import org.tinylog.Logger;

import java.util.List;
import java.util.Optional;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.*;

/**
 * Base class for all creatures which can move through a level's world.
 * 
 * @author Armin Reichert
 */
public abstract class Creature extends Entity {

	protected static final Direction[] DIRECTION_PRIORITY = { UP, LEFT, DOWN, RIGHT };

	private final String name;
	private Direction moveDir;
	private Direction wishDir;
	private Vector2i targetTile;
	protected World world;
	protected final MoveResult moveResult = new MoveResult();
	protected boolean newTileEntered;
	protected boolean gotReverseCommand;
	protected boolean canTeleport;
	protected float corneringSpeedUp;

	protected Creature(String name) {
		checkNotNull(name, "Name of creature must not be null");
		this.name = name;
	}

	/** Readable name, for display and logging purposes. */
	public String name() {
		return name;
	}

	public void reset() {
		super.reset();

		moveDir = RIGHT;
		wishDir = RIGHT;
		targetTile = null;

		moveResult.clear();
		newTileEntered = true;
		gotReverseCommand = false;
		canTeleport = true;
	}

	public World world() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}

	public abstract boolean canReverse();

	/** Tells if the creature entered a new tile with its last move or placement. */
	public boolean isNewTileEntered() {
		return newTileEntered;
	}

	public void setCanTeleport(boolean canTeleport) {
		this.canTeleport = canTeleport;
	}

	public boolean canTeleport() {
		return canTeleport;
	}

	/**
	 * Sets the tile this creature tries to reach (can be an unreachable tile or <code>null</code>).
	 * 
	 * @param tile some tile or <code>null</code>
	 */
	public void setTargetTile(Vector2i tile) {
		targetTile = tile;
	}

	/**
	 * @return (Optional) target tile. Can be inaccessible or outside the world.
	 */
	public Optional<Vector2i> targetTile() {
		return Optional.ofNullable(targetTile);
	}

	/**
	 * Places this creature at the given tile coordinate with the given tile offsets. Updates the
	 * <code>newTileEntered</code> state.
	 * 
	 * @param tx tile x-coordinate (grid column)
	 * @param ty tile y-coordinate (grid row)
	 * @param ox x-offset inside tile
	 * @param oy y-offset inside tile
	 */
	public void placeAtTile(int tx, int ty, float ox, float oy) {
		var prevTile = tile();
		setPosition(tx * TS + ox, ty * TS + oy);
		newTileEntered = !tile().equals(prevTile);
	}

	/**
	 * Places this creature at the given tile coordinate with the given tile offsets. Updates the
	 * <code>newTileEntered</code> state.
	 * 
	 * @param tile tile
	 * @param ox   x-offset inside tile
	 * @param oy   y-offset inside tile
	 */
	public void placeAtTile(Vector2i tile, float ox, float oy) {
		checkTileNotNull(tile);
		placeAtTile(tile.x(), tile.y(), ox, oy);
	}

	/**
	 * Places this creature centered over the given tile.
	 *
	 * @param tile tile where creature is placed
	 */
	public void centerOverTile(Vector2i tile) {
		placeAtTile(tile, 0, 0);
	}

	/**
	 * Simulates the overflow bug from the original Arcade version.
	 * 
	 * @param numTiles number of tiles
	 * @return the tile located the given number of tiles in front of the creature (towards move direction). In case
	 *         creature looks up, additional n tiles are added towards left. This simulates an overflow error in the
	 *         original Arcade game.
	 */
	public Vector2i tilesAheadBuggy(int numTiles) {
		Vector2i ahead = tile().plus(moveDir.vector().scaled(numTiles));
		return moveDir == Direction.UP ? ahead.minus(numTiles, 0) : ahead;
	}

	/**
	 * @param tile some tile inside or outside the world
	 * @return if this creature can access the given tile
	 */
	public boolean canAccessTile(Vector2i tile) {
		checkTileNotNull(tile);
		if (world().insideBounds(tile)) {
			return !world().isWall(tile) && !world().house().door().occupies(tile);
		}
		return world().belongsToPortal(tile);
	}

	/**
	 * Sets the move direction and updates the velocity vector.
	 * 
	 * @param dir the new move direction
	 */
	public void setMoveDir(Direction dir) {
		checkDirectionNotNull(dir);
		if (moveDir != dir) {
			moveDir = dir;
			setVelocity(moveDir.vector().toFloatVec().scaled(velocity().length()));
			Logger.trace("{}: New moveDir: {}. {}", name, moveDir, this);
		}
	}

	/** @return The current move direction. */
	public Direction moveDir() {
		return moveDir;
	}

	/**
	 * Sets the wish direction and updates the velocity vector.
	 * 
	 * @param dir the new wish direction
	 */
	public void setWishDir(Direction dir) {
		checkDirectionNotNull(dir);
		if (wishDir != dir) {
			wishDir = dir;
			Logger.trace("{}: New wishDir: {}. {}", name, wishDir, this);
		}
	}

	/** @return The wish direction. Will be taken as soon as possible. */
	public Direction wishDir() {
		return wishDir;
	}

	/**
	 * Sets both directions at once.
	 * 
	 * @param dir the new wish and move direction
	 */
	public void setMoveAndWishDir(Direction dir) {
		setWishDir(dir);
		setMoveDir(dir);
	}

	/**
	 * Signals that this creature should reverse its move direction as soon as possible.
	 */
	public void reverseAsSoonAsPossible() {
		gotReverseCommand = true;
		newTileEntered = false;
		Logger.trace("{} (moveDir={}, wishDir={}) got command to reverse direction", name, moveDir, wishDir);
	}

	/**
	 * Sets the speed as a fraction of the base speed (1.25 pixels/sec).
	 * 
	 * @param percentage percentage of base speed
	 */
	public void setRelSpeed(byte percentage) {
		if (percentage < 0) {
			throw new IllegalArgumentException("Negative speed percentage: " + percentage);
		}
		setPixelSpeed((float) 0.01 *  percentage * GameModel.SPEED_PX_100_PERCENT);
	}

	/**
	 * Sets the absolute speed and updates the velocity vector.
	 * 
	 * @param pixelSpeed speed in pixels
	 */
	public void setPixelSpeed(float pixelSpeed) {
		if (pixelSpeed < 0) {
			throw new IllegalArgumentException("Negative pixel speed: " + pixelSpeed);
		}
		setVelocity(pixelSpeed == 0 ? Vector2f.ZERO : moveDir.vector().toFloatVec().scaled(pixelSpeed));
	}

	/**
	 * Sets the new wish direction for reaching the target tile.
	 */
	public void navigateTowardsTarget() {
		if (targetTile == null) {
			return;
		}
		if (!newTileEntered && moved()) {
			return; // we don't need no navigation, dim dit diddit diddit dim dit diddit diddit...
		}
		if (world().belongsToPortal(tile())) {
			return; // inside portal, no navigation happens
		}
		computeTargetDirection().ifPresent(this::setWishDir);
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

	public boolean moved() {
		return moveResult.moved;
	}

	public boolean teleported() {
		return moveResult.teleported;
	}

	public boolean enteredTunnel() {
		return moveResult.tunnelEntered;
	}

	/**
	 * Tries moving through the game world.
	 * <p>
	 * First checks if the creature can teleport, then if the creature can move to its wish direction. If this is not
	 * possible, it keeps moving to its current move direction.
	 */
	public void tryMoving() {
		moveResult.clear();
		tryTeleport(world().portals());
		if (!moveResult.teleported) {
			checkReverseCommand();
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

	private void checkReverseCommand() {
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
			Logger.trace("{} velocity around corner: {}", name(), velocity().length());
			move();
			setVelocity(newVelocity);
		} else {
			setVelocity(newVelocity);
			move();
		}

		newTileEntered = !tileBeforeMove.equals(tile());
		moveResult.moved = true;
		moveResult.tunnelEntered = !world().isTunnel(tileBeforeMove) && world().isTunnel(tile());
		moveResult.addMessage(String.format("%5s (%.2f pixels)", dir, newVelocity.length()));
	}
}