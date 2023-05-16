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
import de.amr.games.pacman.ui.fx.v3d.entity.Pac3D;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class PacManDyingAnimation implements DyingAnimation {

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
	public Animation animation() {
		return animation;
	}
}