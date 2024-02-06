/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.animation;

import de.amr.games.pacman.model.actors.Pac;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class HeadBanging implements WalkingAnimation {

	private static final short DEFAULT_ANGLE_FROM = -25;
	private static final short DEFAULT_ANGLE_TO = 15;
	private static final Duration DEFAULT_DURATION = Duration.seconds(0.25);

	private final Pac pac;
	private final RotateTransition animation;

	public HeadBanging(Pac pac, Node node) {
		this.pac = pac;
		animation = new RotateTransition(DEFAULT_DURATION, node);
		animation.setAxis(Rotate.X_AXIS);
		animation.setCycleCount(Animation.INDEFINITE);
		animation.setAutoReverse(true);
		animation.setInterpolator(Interpolator.EASE_BOTH);
		setPowerWalking(false);
	}

	@Override
	public RotateTransition animation() {
		return animation;
	}

	@Override
	public void setPowerWalking(boolean power) {
		double amplification = power ? 1.5 : 1;
		double rate = power ? 2 : 1;
		animation.stop();
		animation.setFromAngle(DEFAULT_ANGLE_FROM * amplification);
		animation.setToAngle(DEFAULT_ANGLE_TO * amplification);
		animation.setRate(rate);
	}

	@Override
	public void play() {
		if (pac.isStandingStill()) {
			stop();
			animation.getNode().setRotate(0);
			return;
		}
		var axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
		if (!axis.equals(animation.getAxis())) {
			animation.stop();
			animation.setAxis(axis);
		}
		animation.play();
	}

	@Override
	public void stop() {
		animation.stop();
		animation.getNode().setRotationAxis(animation.getAxis());
		animation.getNode().setRotate(0);
	}
}