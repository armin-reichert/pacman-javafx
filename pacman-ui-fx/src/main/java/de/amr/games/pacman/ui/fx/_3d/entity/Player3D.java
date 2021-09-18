package de.amr.games.pacman.ui.fx._3d.entity;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Pac;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D-representation of the player.
 * 
 * <p>
 * TODO: need 3D-model for Ms. Pac-Man
 * 
 * @author Armin Reichert
 */
public class Player3D extends Group {

	//@formatter:off
	private static final int[][][] ROTATION_INTERVALS = {
		{ {  0, 0}, {  0, 180}, {  0, 90}, {  0, -90} },
		{ {180, 0}, {180, 180}, {180, 90}, {180, 270} }, 
		{ { 90, 0}, { 90, 180}, { 90, 90}, { 90, 270} },
		{ {-90, 0}, {270, 180}, {-90, 90}, {-90, -90} },
	};
	//@formatter:on

	private static int index(Direction dir) {
		return dir == Direction.LEFT ? 0 : dir == Direction.RIGHT ? 1 : dir == Direction.UP ? 2 : 3;
	}

	private static int[] rotationInterval(Direction from, Direction to) {
		return ROTATION_INTERVALS[index(from)][index(to)];
	}

	public final Pac player;
	private final RotateTransition rotateTransition;
	private final PointLight light;
	private Direction targetDir;

	public Player3D(Pac player, PacManModel3D model3D) {
		this.player = player;
		targetDir = player.dir();
		rotateTransition = new RotateTransition(Duration.seconds(0.25), this);
		rotateTransition.setAxis(Rotate.Z_AXIS);
		int[] rotationInterval = rotationInterval(player.dir(), player.dir());
		setRotationAxis(Rotate.Z_AXIS);
		setRotate(rotationInterval[0]);
		light = new PointLight(Color.WHITE);
		light.setTranslateZ(-4);
		getChildren().addAll(model3D.createPacMan(), light);
		setTranslateZ(-3);
	}

	public void update() {
		setVisible(player.isVisible());
		setTranslateX(player.position().x);
		setTranslateY(player.position().y);
		if (targetDir != player.dir()) {
			int[] rotationInterval = rotationInterval(targetDir, player.dir());
			rotateTransition.stop();
			rotateTransition.setFromAngle(rotationInterval[0]);
			rotateTransition.setToAngle(rotationInterval[1]);
			rotateTransition.play();
			targetDir = player.dir();
		}
	}
}