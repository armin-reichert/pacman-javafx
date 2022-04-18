package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.world.World.HTS;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Creature;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * Encapsulates motion of a creature through the 3D world.
 * 
 * @author Armin Reichert
 */
public class Motion {

	//@formatter:off
	//TODO there must be a more elegant way to do this
	private static final int[][][] TURN_ANGLES = {
		{ {  0, 0}, {  0, 180}, {  0, 90}, {  0, -90} }, // LEFT
		{ {180, 0}, {180, 180}, {180, 90}, {180, 270} }, // RIGHT
		{ { 90, 0}, { 90, 180}, { 90, 90}, { 90, 270} }, // UP
		{ {-90, 0}, {270, 180}, {-90, 90}, {-90, -90} }, // DOWN
	};
	//@formatter:on

	private final Creature guy;
	private final Node root;
	private final RotateTransition turningAnimation;
	private Direction targetDir;

	/**
	 * @param guy  the creature (model)
	 * @param root root node of the creature's 3D representation
	 */
	public Motion(Creature guy, Node root) {
		this.guy = guy;
		this.root = root;
		turningAnimation = new RotateTransition(Duration.seconds(0.3), root);
		turningAnimation.setAxis(Rotate.Z_AXIS);
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

	private int index(Direction dir) {
		if (dir == null) {
			return 0;
		}
		return switch (dir) {
		case LEFT -> 0;
		case RIGHT -> 1;
		case UP -> 2;
		case DOWN -> 3;
		};
	}

	private int[] angles(Direction from, Direction to) {
		return TURN_ANGLES[index(from)][index(to)];
	}
}