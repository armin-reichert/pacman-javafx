/*
MIT License

Copyright (c) 2023 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package de.amr.games.pacman.ui.fx._3d.animation;

import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.ui.fx._3d.entity.Pac3D;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class HeadBanging implements Pac3D.WalkingAnimation {

	private static final short DEFAULT_ANGLE_FROM = -25;
	private static final short DEFAULT_ANGLE_TO = 15;
	private static final Duration DEFAULT_DURATION = Duration.seconds(0.25);

	private final RotateTransition animation;

	public HeadBanging(Node node) {
		animation = new RotateTransition(DEFAULT_DURATION, node);
		animation.setAxis(Rotate.X_AXIS);
		animation.setCycleCount(Animation.INDEFINITE);
		animation.setAutoReverse(true);
		animation.setInterpolator(Interpolator.EASE_BOTH);
		setPowerMode(false);
	}

	@Override
	public void setPowerMode(boolean power) {
		double amplification = power ? 1.5 : 1;
		double rate = power ? 2 : 1;
		animation.stop();
		animation.setFromAngle(DEFAULT_ANGLE_FROM * amplification);
		animation.setToAngle(DEFAULT_ANGLE_TO * amplification);
		animation.setRate(rate);
	}

	public RotateTransition animation() {
		return animation;
	}

	@Override
	public void update(Pac pac) {
		if (pac.isStandingStill()) {
			end(pac);
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
	public void end(Pac pac) {
		animation.stop();
		animation.getNode().setRotationAxis(animation.getAxis());
		animation.getNode().setRotate(0);
	}
}