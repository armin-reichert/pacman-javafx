package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Creature;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * Common base class for 3D creatures.
 * 
 * @param <CREATURE> type of creature (Pac, Ghost, Bonus)
 * 
 * @author Armin Reichert
 */
public abstract class Creature3D<CREATURE extends Creature> extends Group {

	//@formatter:off
	//TODO there sure is a more elegant way to do this
	private static final int[][][] TURN_ANGLES = {
		{ {  0, 0}, {  0, 180}, {  0, 90}, {  0, -90} }, // LEFT
		{ {180, 0}, {180, 180}, {180, 90}, {180, 270} }, // RIGHT
		{ { 90, 0}, { 90, 180}, { 90, 90}, { 90, 270} }, // UP
		{ {-90, 0}, {270, 180}, {-90, 90}, {-90, -90} }, // DOWN
	};
	//@formatter:on

	public final CREATURE guy;
	protected final RotateTransition turningAnimation;
	protected Direction targetDir;

	public Creature3D(CREATURE guy) {
		this.guy = guy;
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
		boolean insideMaze = guy.position.x >= 0 && guy.position.x <= t(guy.world.numCols() - 1);
		setVisible(guy.visible && insideMaze);
		setTranslateX(guy.position.x + HTS);
		setTranslateY(guy.position.y + HTS);
		setTranslateZ(-HTS);
		if (targetDir != guy.moveDir()) {
			int[] angles = turnAngles(targetDir, guy.moveDir());
			turningAnimation.setFromAngle(angles[0]);
			turningAnimation.setToAngle(angles[1]);
			turningAnimation.playFromStart();
			targetDir = guy.moveDir();
		}
	}
}