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
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class PacManDyingAnimation implements Pac3D.DyingAnimation {

	private final Pac pac;
	private final Animation animation;

	public PacManDyingAnimation(Pac pac, Node root) {
		this.pac = pac;

		var totalDuration = Duration.seconds(2);
		var numSpins = 15;

		var spinning = new RotateTransition(totalDuration.divide(numSpins), root);
		spinning.setAxis(Rotate.Z_AXIS);
		spinning.setByAngle(360);
		spinning.setCycleCount(numSpins);
		spinning.setInterpolator(Interpolator.LINEAR);

		var shrinking = new ScaleTransition(totalDuration, root);
		shrinking.setToX(0.5);
		shrinking.setToY(0.5);
		shrinking.setToZ(0.0);

		var falling = new TranslateTransition(totalDuration, root);
		falling.setToZ(4);

		animation = new SequentialTransition(//
				Ufx.pause(0.4), //
				new ParallelTransition(spinning, shrinking, falling), //
				Ufx.pause(0.25));

		animation.setOnFinished(e -> root.setTranslateZ(0));
	}

	@Override
	public Animation animation() {
		// TODO set orientation correctly
		return animation;
	}
}