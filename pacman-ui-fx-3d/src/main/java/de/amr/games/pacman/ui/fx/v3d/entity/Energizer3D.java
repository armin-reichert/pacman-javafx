/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Point3D;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.*;
import static java.util.Objects.requireNonNull;

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
	public Point3D position() {
		return new Point3D(shape.getTranslateX(), shape.getTranslateY(), shape.getTranslateZ());
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
	public void onEaten() {
		pumping.stop();
		// TODO check this
		var hideAfterDelay = Ufx.actionAfterSeconds(0.05, () -> shape.setVisible(false));
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