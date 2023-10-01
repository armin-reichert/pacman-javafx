/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.animation;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.entity.Pac3D;
import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.function.Supplier;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class PacManDyingAnimation implements Supplier<Animation> {

	private final Pac3D pac3D;
	private Duration spinningDuration = Duration.seconds(1.5);
	private short numSpins = 10;

	public PacManDyingAnimation(Pac3D pac3D) {
		checkNotNull(pac3D);
		this.pac3D = pac3D;
	}

	@Override
	public Animation get() {
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