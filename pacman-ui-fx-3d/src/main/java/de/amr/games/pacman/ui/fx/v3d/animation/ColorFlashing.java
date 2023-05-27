/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.animation;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class ColorFlashing extends Transition {

	public final ObjectProperty<Color> colorPy = new SimpleObjectProperty<>();

	private final Color startColor;
	private final Color endColor;

	public ColorFlashing(Color startColor, Color endColor, double seconds, int numFlashes) {
		this.startColor = startColor;
		this.endColor = endColor;
		colorPy.set(startColor);
		setCycleCount(INDEFINITE);
		setCycleDuration(Duration.seconds(seconds / numFlashes));
		setAutoReverse(true);
		setInterpolator(Interpolator.EASE_OUT);
	}

	@Override
	protected void interpolate(double t) {
		colorPy.set(startColor.interpolate(endColor, t));
	}
}