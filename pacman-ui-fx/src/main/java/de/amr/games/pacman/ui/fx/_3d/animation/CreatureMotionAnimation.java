package de.amr.games.pacman.ui.fx._3d.animation;

import static de.amr.games.pacman.model.common.world.World.HTS;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.actors.Creature;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * Encapsulates motion of a creature through the 3D world.
 * 
 * @author Armin Reichert
 */
public class CreatureMotionAnimation {

	//@formatter:off
	private static final int[][][] TURN_ANGLES = {
		{ {  0, 0}, {  0, 180}, {  0, 90}, {  0, -90} }, // LEFT
		{ {180, 0}, {180, 180}, {180, 90}, {180, 270} }, // RIGHT
		{ { 90, 0}, { 90, 180}, { 90, 90}, { 90, 270} }, // UP
		{ {-90, 0}, {270, 180}, {-90, 90}, {-90, -90} }, // DOWN
	};
	//@formatter:on

	private static int index(Direction dir) {
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

	private final Creature guy;
	private final Node node;
	private Direction targetDir;

	public CreatureMotionAnimation(Creature guy, Node node) {
		this.guy = guy;
		this.node = node;
	}

	public void reset() {
		targetDir = guy.moveDir();
		update();
	}

	public void update() {
		node.setVisible(guy.isVisible());
		node.setTranslateX(guy.getPosition().x + HTS);
		node.setTranslateY(guy.getPosition().y + HTS);
		node.setTranslateZ(-HTS);
		if (targetDir != guy.moveDir()) {
			int[] angles = TURN_ANGLES[index(targetDir)][index(guy.moveDir())];
			var turning = new RotateTransition(Duration.seconds(0.3), node);
			turning.setAxis(Rotate.Z_AXIS);
			turning.setInterpolator(Interpolator.EASE_BOTH);
			turning.setFromAngle(angles[0]);
			turning.setToAngle(angles[1]);
			turning.playFromStart();
			targetDir = guy.moveDir();
		}
	}
}