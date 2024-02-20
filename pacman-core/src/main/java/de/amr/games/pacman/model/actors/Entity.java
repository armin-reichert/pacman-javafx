/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * Base class for all "entities", e.g. creatures and bonus entities. Each entity has a position, velocity, acceleration
 * and visibility.
 * 
 * @author Armin Reichert
 */
public class Entity {

	protected boolean visible = false;
	protected float posX;
	protected float posY;
	protected float velX;
	protected float velY;
	protected float accX;
	protected float accY;

	@Override
	public String toString() {
		return "Entity{" +
			"visible=" + visible +
			", posX=" + posX +
			", posY=" + posY +
			", velX=" + velX +
			", velY=" + velY +
			", accX=" + accX +
			", accY=" + accY +
			'}';
	}

	public void reset() {
		visible = false;
		posX = 0;
		posY = 0;
		velX = 0;
		velY = 0;
		accX = 0;
		accY = 0;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void show() {
		visible = true;
	}

	public void hide() {
		visible = false;
	}

	public float posX() {
		return posX;
	}

	public float posY() {
		return posY;
	}

	/**
	 * @return upper left corner of the entity collision box which is a square of size one tile.
	 */
	public Vector2f position() {
		return v2f(posX, posY);
	}

	public void setPosX(float pos_x) {
		this.posX = pos_x;
	}

	public void setPosY(float pos_y) {
		this.posY = pos_y;
	}

	public void setPosition(float x, float y) {
		posX = x;
		posY = y;
	}

	public void setPosition(Vector2f position) {
		checkNotNull(position, "Position of entity must not be null");
		posX = position.x();
		posY = position.y();
	}

	/** @return Center position of entity collision box (position property stores *upper left corner* of box). */
	public Vector2f center() {
		return v2f(posX + HTS, posY + HTS);
	}

	public Vector2f velocity() {
		return v2f(velX, velY);
	}

	public void setVelocity(Vector2f velocity) {
		checkNotNull(velocity, "Velocity of entity must not be null");
		velX = velocity.x();
		velY = velocity.y();
	}

	public void setVelocity(float vx, float vy) {
		velX = vx;
		velY = vy;
	}

	public Vector2f acceleration() {
		return v2f(accX, accY);
	}

	public void setAcceleration(Vector2f acceleration) {
		checkNotNull(acceleration, "Acceleration of entity must not be null");
		accX = acceleration.x();
		accY = acceleration.y();
	}

	public void setAcceleration(float ax, float ay) {
		accX = ax;
		accY = ay;
	}

	/**
	 * Moves this entity by its current velocity and increases its velocity by its current acceleration.
	 */
	public void move() {
		posX += velX;
		posY += velY;
		velX += accX;
		velY += accY;
	}

	/** @return Tile containing the center of the entity collision box. */
	public Vector2i tile() {
		return tileAt(posX + HTS, posY + HTS);
	}

	/** @return Offset inside current tile: (0, 0) if centered, range: [-4, +4) */
	public Vector2f offset() {
		var tile = tile();
		return v2f(posX - TS * tile.x(), posY - TS * tile.y());
	}

	/**
	 * @param other some entity
	 * @return <code>true</code> if both entities occupy same tile
	 */
	public boolean sameTile(Entity other) {
		checkNotNull(other, "Entity to check for same tile must not be null");
		return tile().equals(other.tile());
	}
}