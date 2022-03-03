package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Creature;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * Common base class for 3D creatures (Pac-Man, ghosts).
 * 
 * @author Armin Reichert
 */
public abstract class Creature3D<C extends Creature> extends Group {

	//@formatter:off
	//TODO there sure is a more elegant way to do this
	private static final int[][][] TURN_ANGLES = {
		{ {  0, 0}, {  0, 180}, {  0, 90}, {  0, -90} }, // LEFT
		{ {180, 0}, {180, 180}, {180, 90}, {180, 270} }, // RIGHT
		{ { 90, 0}, { 90, 180}, { 90, 90}, { 90, 270} }, // UP
		{ {-90, 0}, {270, 180}, {-90, 90}, {-90, -90} }, // DOWN
	};
	//@formatter:on

	public final C creature;
	protected final RotateTransition turningAnimation;
	protected Direction targetDir;

	public Creature3D(C creature) {
		this.creature = creature;
		turningAnimation = new RotateTransition(Duration.seconds(0.3));
		turningAnimation.setAxis(Rotate.Z_AXIS);
	}

	protected int index(Direction dir) {
		return dir == Direction.LEFT ? 0 : dir == Direction.RIGHT ? 1 : dir == Direction.UP ? 2 : 3;
	}

	protected int[] turnAngles(Direction from, Direction to) {
		return TURN_ANGLES[index(from)][index(to)];
	}

	protected int turnAngle(Direction dir) {
		return TURN_ANGLES[index(dir)][0][0];
	}

	public void update() {
		boolean outsideMaze = creature.position.x < 0 || creature.position.x > (creature.world.numCols() - 1) * TS;
		setVisible(creature.visible && !outsideMaze);
		setTranslateX(creature.position.x + HTS);
		setTranslateY(creature.position.y + HTS);
		setTranslateZ(-HTS);
		if (targetDir != creature.moveDir()) {
			int[] angles = turnAngles(targetDir, creature.moveDir());
			turningAnimation.setFromAngle(angles[0]);
			turningAnimation.setToAngle(angles[1]);
			turningAnimation.playFromStart();
			targetDir = creature.moveDir();
		}
	}
}