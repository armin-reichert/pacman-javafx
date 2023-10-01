/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.animation;

import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.entity.Pac3D;
import javafx.animation.*;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.function.Supplier;

/**
 * @author Armin Reichert
 */
public class PacManDyingAnimation implements Supplier<Animation> {

	private final Animation animation;
	private final RotateTransition spinning;

	public PacManDyingAnimation(Pac3D pac3D) {
		var totalDuration = Duration.seconds(1.5);
		var numSpins = 12;

		spinning = new RotateTransition(totalDuration.divide(numSpins), pac3D.getRoot());
		spinning.setAxis(Rotate.Z_AXIS);
		spinning.setByAngle(360);
		spinning.setCycleCount(numSpins);
		spinning.setInterpolator(Interpolator.LINEAR);
		spinning.setRate(1.0); // TODO

		var shrinking = new ScaleTransition(totalDuration, pac3D.getRoot());
		shrinking.setToX(0.75);
		shrinking.setToY(0.75);
		shrinking.setToZ(0.0);

		var falling = new TranslateTransition(totalDuration, pac3D.getRoot());
		falling.setToZ(4);

		animation = new SequentialTransition( //
				Ufx.actionAfterSeconds(0, () -> clearOrientation(pac3D)), //
				Ufx.pauseSeconds(0.5), //
				new ParallelTransition(spinning, shrinking, falling), //
				Ufx.actionAfterSeconds(0.5, () -> restoreOrientation(pac3D)) //
		);

		animation.setOnFinished(e -> {
			pac3D.getRoot().setVisible(false);
			pac3D.getRoot().setTranslateZ(0);
		});
	}

	private void clearOrientation(Pac3D pac3D) {
		var pacNode = pac3D.getRoot().getChildren().get(0);
		pacNode.getTransforms().remove(pac3D.orientation());
	}

	private void restoreOrientation(Pac3D pac3D) {
		var pacNode = pac3D.getRoot().getChildren().get(0);
		pacNode.getTransforms().add(pac3D.orientation());
	}

	@Override
	public Animation get() {
		return animation;
	}
}