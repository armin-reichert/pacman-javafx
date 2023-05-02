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
package de.amr.games.pacman.ui.fx3d._3d.entity;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.requirePositive;
import static de.amr.games.pacman.model.world.World.tileAt;
import static java.util.Objects.requireNonNull;

import java.util.Optional;

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.util.Vector3f;
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
		requirePositive(radius, "Energizer radius must be positive but is %f");

		shape = new Sphere(radius);
		shape.setUserData(this);

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

	@Override
	public String toString() {
		var pumpingText = pumping.getStatus() == Status.RUNNING ? ", pumping" : "";
		return String.format("[Energizer%s, tile: %s, %s]", pumpingText, tile(), shape);
	}

	public void placeAtTile(Vector2i tile) {
		requireNonNull(tile);

		shape.setTranslateX(tile.x() * TS + HTS);
		shape.setTranslateY(tile.y() * TS + HTS);
		shape.setTranslateZ(-HTS);
	}

	@Override
	public Vector3f position() {
		return new Vector3f((float) shape.getTranslateX(), (float) shape.getTranslateY(), (float) shape.getTranslateZ());
	}

	@Override
	public Vector2i tile() {
		return tileAt((float) shape.getTranslateX(), (float) shape.getTranslateY());
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
		// TODO check this
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
}