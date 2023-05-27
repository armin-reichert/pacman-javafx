/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.animation;

import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class SinusCurveAnimation extends Transition {

	public final DoubleProperty elongationPy = new SimpleDoubleProperty(this, "elongation");

	private double amplitude;

	public SinusCurveAnimation(int times) {
		setCycleDuration(Duration.seconds(0.2));
		setCycleCount(2 * times);
		setAutoReverse(true);
	}

	public void setAmplitude(double value) {
		this.amplitude = value;
	}

	@Override
	protected void interpolate(double t) {
		elongationPy.set(amplitude * Math.cos(t * Math.PI / 2));
	}
}