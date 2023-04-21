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

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.ui.fx._3d.entity.Pac3D;
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
public class MsPacManDyingAnimation implements Pac3D.DyingAnimation {

	private final Pac pac;
	private final RotateTransition spin;
	private final Animation animation;

	public MsPacManDyingAnimation(Pac pac, Node root) {
		this.pac = pac;

		spin = new RotateTransition(Duration.seconds(0.25), root);
		spin.setAxis(Rotate.X_AXIS);
		spin.setInterpolator(Interpolator.LINEAR);
		spin.setCycleCount(4);
		spin.setDelay(Duration.seconds(0.5));
		spin.setOnFinished(e -> root.setRotate(90));

		animation = new SequentialTransition(spin, Ufx.pause(2));
	}

	@Override
	public Animation animation() {
		var axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
		spin.setAxis(axis);
		spin.setByAngle(pac.moveDir() == Direction.LEFT || pac.moveDir() == Direction.DOWN ? -90 : 90);
		return animation;
	}
}