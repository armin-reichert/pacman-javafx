package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.ui.fx.model3D.Model3DHelper.lerp;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.fx.model3D.GianmarcosModel3D;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;

public class Player3D extends Group {

	private final Pac pac;

	public Player3D(Pac pac) {
		this.pac = pac;
		getChildren().add(GianmarcosModel3D.IT.createPacMan());
	}

	public void update() {
		setVisible(pac.visible);
		setTranslateX(pac.position.x);
		setTranslateY(pac.position.y);
		double target = rotationForDir(pac.dir(), 0, 180, 90, -90);
		rotateTowardsTargetZ(target);
	}

	private RotateTransition playerTurning;

	private void rotateTowardsTargetZ(double target) {
		setRotationAxis(Rotate.Z_AXIS);
		if (getRotate() != target) {
			double next = lerp(getRotate(), target, 0.1);
			if (getRotate() - 180 > target) {
				next = lerp(getRotate(), target + 360, 0.1);
			} else if (getRotate() + 180 < target) {
				next = lerp(getRotate(), target - 360, 0.1);
			}
			setRotate(next);
		}
	}

	private double rotationForDir(Direction dir, int left, int right, int up, int down) {
		return dir == Direction.LEFT ? 0 : dir == Direction.UP ? 90 : dir == Direction.RIGHT ? 180 : 270;
	}
}