package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Creature;
import javafx.scene.Group;

/**
 * Common base class for 3D creatures (Pac-Man, ghosts).
 * 
 * @author Armin Reichert
 */
public abstract class Creature3D extends Group {

	//@formatter:off
	//TODO there sure is a more elegant way to do this
	private static final int[][][] ROTATION_ANGLES = {
		{ {  0, 0}, {  0, 180}, {  0, 90}, {  0, -90} }, // LEFT
		{ {180, 0}, {180, 180}, {180, 90}, {180, 270} }, // RIGHT
		{ { 90, 0}, { 90, 180}, { 90, 90}, { 90, 270} }, // UP
		{ {-90, 0}, {270, 180}, {-90, 90}, {-90, -90} }, // DOWN
	};
	//@formatter:on

	protected int index(Direction dir) {
		return dir == Direction.LEFT ? 0 : dir == Direction.RIGHT ? 1 : dir == Direction.UP ? 2 : 3;
	}

	protected int[] rotationAngles(Direction from, Direction to) {
		return ROTATION_ANGLES[index(from)][index(to)];
	}

	protected int rotationAngle(Direction dir) {
		return ROTATION_ANGLES[index(dir)][0][0];
	}

	protected boolean outsideMaze(Creature creature) {
		return creature.position().x < 0 || creature.position().x > (creature.world.numCols() - 1) * TS;
	}
}