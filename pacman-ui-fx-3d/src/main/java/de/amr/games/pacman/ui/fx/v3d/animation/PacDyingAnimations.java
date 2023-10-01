/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.animation;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.entity.Pac3D;
import javafx.animation.*;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public interface PacDyingAnimations {

	public static Animation createMsPacManDyingAnimation(Pac3D msPac3D) {
		var spin = new RotateTransition(Duration.seconds(0.5), msPac3D.getRoot());
		spin.setAxis(Rotate.X_AXIS);
		spin.setFromAngle(0);
		spin.setToAngle(360);
		spin.setInterpolator(Interpolator.LINEAR);
		spin.setCycleCount(4);
		spin.setRate(2);
		return new SequentialTransition(
			Ufx.pauseSeconds(0.5),
			spin,
			Ufx.pauseSeconds(2)
		);
	}

	public static Animation createPacManDyingAnimation(Pac3D pac3D) {
		Duration spinningDuration = Duration.seconds(1.5);
		short numSpins = 10;

		var spinning = new RotateTransition(spinningDuration.divide(numSpins), pac3D.getRoot());
		spinning.setAxis(Rotate.Z_AXIS);
		spinning.setByAngle(360);
		spinning.setCycleCount(numSpins);
		spinning.setInterpolator(Interpolator.LINEAR);

		var shrinking = new ScaleTransition(spinningDuration, pac3D.getRoot());
		shrinking.setToX(0.75);
		shrinking.setToY(0.75);
		shrinking.setToZ(0.0);
		shrinking.setInterpolator(Interpolator.LINEAR);

		var falling = new TranslateTransition(spinningDuration, pac3D.getRoot());
		falling.setToZ(4);
		falling.setInterpolator(Interpolator.EASE_IN);

		var animation = new SequentialTransition(
				Ufx.actionAfterSeconds(0, () -> {
					//TODO does not yet work as I want to
					pac3D.init();
					pac3D.turnTo(Direction.RIGHT);
				}),
				Ufx.pauseSeconds(0.5),
				new ParallelTransition(spinning, shrinking, falling),
				Ufx.pauseSeconds(1.0)
		);

		animation.setOnFinished(e -> {
			pac3D.getRoot().setVisible(false);
			pac3D.getRoot().setTranslateZ(0);
		});

		return animation;
	}
}