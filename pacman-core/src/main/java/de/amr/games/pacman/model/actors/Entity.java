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
	protected float pos_x;
	protected float pos_y;
	protected float vel_x;
	protected float vel_y;
	protected float acc_x;
	protected float acc_y;

	@Override
	public String toString() {
		return "Entity{" +
			"visible=" + visible +
			", pos_x=" + pos_x +
			", pos_y=" + pos_y +
			", vel_x=" + vel_x +
			", vel_y=" + vel_y +
			", acc_x=" + acc_x +
			", acc_y=" + acc_y +
			'}';
	}

	public void reset() {
		visible = false;
		pos_x = 0;
		pos_y = 0;
		vel_x = 0;
		vel_y = 0;
		acc_x = 0;
		acc_y = 0;
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

	public float pos_x() {
		return pos_x;
	}

	public float pos_y() {
		return pos_y;
	}

	/**
	 * @return upper left corner of the entity collision box which is a square of size one tile.
	 */
	public Vector2f position() {
		return v2f(pos_x, pos_y);
	}

	public void setPos_x(float pos_x) {
		this.pos_x = pos_x;
	}

	public void setPos_y(float pos_y) {
		this.pos_y = pos_y;
	}

	public void setPosition(float x, float y) {
		pos_x = x;
		pos_y = y;
	}

	public void setPosition(Vector2f position) {
		checkNotNull(position, "Position of entity must not be null");
		pos_x = position.x();
		pos_y = position.y();
	}

	/** @return Center position of entity collision box (position property stores *upper left corner* of box). */
	public Vector2f center() {
		return v2f(pos_x + HTS, pos_y + HTS);
	}

	public Vector2f velocity() {
		return v2f(vel_x, vel_y);
	}

	public void setVelocity(Vector2f velocity) {
		checkNotNull(velocity, "Velocity of entity must not be null");
		vel_x = velocity.x();
		vel_y = velocity.y();
	}

	public void setVelocity(float vx, float vy) {
		vel_x = vx;
		vel_y = vy;
	}

	public Vector2f acceleration() {
		return v2f(acc_x, acc_y);
	}

	public void setAcceleration(Vector2f acceleration) {
		checkNotNull(acceleration, "Acceleration of entity must not be null");
		acc_x = acceleration.x();
		acc_y = acceleration.y();
	}

	public void setAcceleration(float ax, float ay) {
		acc_x = ax;
		acc_y = ay;
	}

	/**
	 * Moves this entity by its current velocity and increases its velocity by its current acceleration.
	 */
	public void move() {
		pos_x += vel_x;
		pos_y += vel_y;
		vel_x += acc_x;
		vel_y += acc_y;
	}

	/** @return Tile containing the center of the entity collision box. */
	public Vector2i tile() {
		return tileAt(pos_x + HTS, pos_y + HTS);
	}

	/** @return Offset inside current tile: (0, 0) if centered, range: [-4, +4) */
	public Vector2f offset() {
		var tile = tile();
		return v2f(pos_x - TS * tile.x(), pos_y - TS * tile.y());
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