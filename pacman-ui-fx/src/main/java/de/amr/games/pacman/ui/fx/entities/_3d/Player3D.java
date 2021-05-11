package de.amr.games.pacman.ui.fx.entities._3d;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.fx.model3D.GianmarcosModel3D;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class Player3D extends Group {

	//@formatter:off
	private static final int[][][] ROTATION_INTERVALS = {
			{ {0,0},   {0, 180},   {0,90},   {0,-90} },
			{ {180,0}, {180, 180}, {180,90}, {180,270} },
			{ {90,0},  {90, 180},  {90,90},  {90,270} },
			{ {-90,0}, {270, 180}, {-90,90},  {-90,-90} },
	};
	//@formatter:on

	private static int indexOfDir(Direction dir) {
		return dir == Direction.LEFT ? 0 : dir == Direction.RIGHT ? 1 : dir == Direction.UP ? 2 : 3;
	}

	private static int[] rotationInterval(Direction from, Direction to) {
		int row = indexOfDir(from), col = indexOfDir(to);
		return ROTATION_INTERVALS[row][col];
	}

	private final Pac pac;
	private final RotateTransition rotateTransition;
	private Direction targetDir;

	public Player3D(Pac pac) {
		getChildren().add(GianmarcosModel3D.createPacMan());
		this.pac = pac;
		targetDir = pac.dir();
		rotateTransition = new RotateTransition(Duration.seconds(0.25), this);
		rotateTransition.setAxis(Rotate.Z_AXIS);
		int[] rotationInterval = rotationInterval(pac.dir(), pac.dir());
		setRotationAxis(Rotate.Z_AXIS);
		setRotate(rotationInterval[0]);
	}

	public void update() {
		setVisible(pac.isVisible());
		setTranslateX(pac.position().x);
		setTranslateY(pac.position().y);
		if (targetDir != pac.dir()) {
			int[] rotationInterval = rotationInterval(targetDir, pac.dir());
			rotateTransition.stop();
			rotateTransition.setFromAngle(rotationInterval[0]);
			rotateTransition.setToAngle(rotationInterval[1]);
			rotateTransition.play();
			targetDir = pac.dir();
		}
	}
}