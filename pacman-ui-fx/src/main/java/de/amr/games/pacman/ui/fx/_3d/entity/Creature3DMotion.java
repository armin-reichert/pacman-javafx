package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.world.World.HTS;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Creature;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * Encapsulates motion and turning functionality of a 3D creature.
 * 
 * @param <CREATURE> type of creature (Pac, Ghost, Bonus)
 * 
 * @author Armin Reichert
 */
public class Creature3DMotion<CREATURE extends Creature> {

	//@formatter:off
	//TODO there must be a more elegant way to do this
	private static final int[][][] TURN_ANGLES = {
		{ {  0, 0}, {  0, 180}, {  0, 90}, {  0, -90} }, // LEFT
		{ {180, 0}, {180, 180}, {180, 90}, {180, 270} }, // RIGHT
		{ { 90, 0}, { 90, 180}, { 90, 90}, { 90, 270} }, // UP
		{ {-90, 0}, {270, 180}, {-90, 90}, {-90, -90} }, // DOWN
	};
	//@formatter:on

	public final CREATURE guy;
	private final Node root;
	private final RotateTransition turningAnimation;
	private Direction targetDir;

	public Creature3DMotion(CREATURE guy, Node root) {
		this.guy = guy;
		this.root = root;
		turningAnimation = new RotateTransition(Duration.seconds(0.3), root);
		turningAnimation.setAxis(Rotate.Z_AXIS);
	}

	private int index(Direction dir) {
		return dir == Direction.LEFT ? 0 : dir == Direction.RIGHT ? 1 : dir == Direction.UP ? 2 : 3;
	}

	private int[] angles(Direction from, Direction to) {
		return TURN_ANGLES[index(from)][index(to)];
	}

	public void update() {
		root.setTranslateX(guy.position.x + HTS);
		root.setTranslateY(guy.position.y + HTS);
		root.setTranslateZ(-HTS);
		if (targetDir != guy.moveDir()) {
			int[] angles = angles(targetDir, guy.moveDir());
			turningAnimation.setFromAngle(angles[0]);
			turningAnimation.setToAngle(angles[1]);
			turningAnimation.playFromStart();
			targetDir = guy.moveDir();
		}
	}
}