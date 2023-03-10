/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.Optional;

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

/**
 * 3D energizer pellet.
 * 
 * @author Armin Reichert
 */
public class Energizer3D implements Eatable3D {

	private static final double MIN_SCALE = 0.25;

	private final Shape3D shape;
	private final ScaleTransition pumping;
	private Animation eatenAnimation;

	public Energizer3D(double radius) {
		shape = new Sphere(radius);
		pumping = new ScaleTransition(Duration.seconds(1.0 / 4), shape);
		pumping.setAutoReverse(true);
		pumping.setCycleCount(Animation.INDEFINITE);
		pumping.setInterpolator(Interpolator.EASE_BOTH);
		pumping.setFromX(1.0);
		pumping.setFromY(1.0);
		pumping.setFromZ(1.0);
		pumping.setToX(MIN_SCALE);
		pumping.setToY(MIN_SCALE);
		pumping.setToZ(MIN_SCALE);
	}

	public void setTile(Vector2i tile) {
		shape.setUserData(tile);
		shape.setTranslateX(tile.x() * TS + HTS);
		shape.setTranslateY(tile.y() * TS + HTS);
		shape.setTranslateZ(-HTS + 1);
	}

	@Override
	public Shape3D getRoot() {
		return shape;
	}

	@Override
	public Optional<Animation> getEatenAnimation() {
		return Optional.ofNullable(eatenAnimation);
	}

	public void setEatenAnimation(Animation animation) {
		this.eatenAnimation = animation;
	}

	@Override
	public void eat() {
		pumping.stop();
		var hideAfterDelay = Ufx.afterSeconds(0.05, () -> shape.setVisible(false));
		if (eatenAnimation != null) {
			new SequentialTransition(hideAfterDelay, eatenAnimation).play();
		} else {
			hideAfterDelay.play();
		}
	}

	public void startPumping() {
		pumping.playFromStart();
	}

	public void stopPumping() {
		pumping.stop();
	}

	@Override
	public String toString() {
		return String.format("[Energizer, tile: %s, pumping: %s]", tile(), pumping.getStatus() == Status.RUNNING);
	}
}