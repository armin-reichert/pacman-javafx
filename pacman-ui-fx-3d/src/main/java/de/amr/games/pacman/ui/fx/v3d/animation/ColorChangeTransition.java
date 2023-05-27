/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.animation;

import javafx.animation.Transition;
import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class ColorChangeTransition extends Transition {

	private final Color fromColor;
	private final Color toColor;
	private final ObjectProperty<Color> targetColorPy;

	public ColorChangeTransition(Duration duration, Color fromColor, Color toColor, ObjectProperty<Color> targetColorPy) {
		this.fromColor = fromColor;
		this.toColor = toColor;
		this.targetColorPy = targetColorPy;
		setCycleCount(1);
		setCycleDuration(duration);
	}

	@Override
	protected void interpolate(double t) {
		targetColorPy.setValue(fromColor.interpolate(toColor, t));
	}
}