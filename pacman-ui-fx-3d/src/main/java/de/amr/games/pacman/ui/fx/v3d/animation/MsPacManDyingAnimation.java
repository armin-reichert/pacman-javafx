/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.animation;

import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.function.Supplier;

/**
 * @author Armin Reichert
 */
public class MsPacManDyingAnimation implements Supplier<Animation> {

	private final Animation animation;

	public MsPacManDyingAnimation(Node node) {
		var spin = new RotateTransition(Duration.seconds(0.5), node);
		spin.setAxis(Rotate.X_AXIS);
		spin.setFromAngle(0);
		spin.setToAngle(360);
		spin.setInterpolator(Interpolator.LINEAR);
		spin.setCycleCount(4);
		spin.setRate(2);
		animation = new SequentialTransition(
			Ufx.pauseSeconds(0.5),
			spin,
			Ufx.pauseSeconds(2)
		);
	}

	@Override
	public Animation get() {
		return animation;
	}
}