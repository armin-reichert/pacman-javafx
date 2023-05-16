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

package de.amr.games.pacman.ui.fx.v3d.animation;

import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class MsPacManDyingAnimation implements DyingAnimation {

	private final Animation animation;

	public MsPacManDyingAnimation(Node node) {
		var spin = new RotateTransition(Duration.seconds(0.5), node);
		spin.setAxis(Rotate.X_AXIS);
		spin.setFromAngle(0);
		spin.setToAngle(360);
		spin.setInterpolator(Interpolator.LINEAR);
		spin.setCycleCount(4);
		spin.setRate(2);
		//@formatter:off
		animation = new SequentialTransition(
			Ufx.pauseSeconds(0.5),
			spin,
			Ufx.pauseSeconds(2)
		);
		//@formatter:on
	}

	@Override
	public Animation animation() {
		return animation;
	}
}