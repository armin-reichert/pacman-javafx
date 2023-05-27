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
public class HipSwaying implements WalkingAnimation {

	private static final short DEFAULT_ANGLE_FROM = -20;
	private static final short DEFAULT_ANGLE_TO = 20;
	private static final Duration DEFAULT_DURATION = Duration.seconds(0.4);

	private final Pac pac;
	private final RotateTransition animation;

	public HipSwaying(Pac pac, Node node) {
		this.pac = pac;
		animation = new RotateTransition(DEFAULT_DURATION, node);
		animation.setAxis(Rotate.Z_AXIS);
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
	public void walk() {
		if (pac.isStandingStill()) {
			hold();
			animation.getNode().setRotate(0);
			return;
		}
		animation.play();
	}

	@Override
	public void hold() {
		animation.stop();
		animation.getNode().setRotationAxis(animation.getAxis());
		animation.getNode().setRotate(0);
	}
}