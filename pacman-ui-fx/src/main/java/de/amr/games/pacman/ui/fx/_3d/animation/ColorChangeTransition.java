/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import javafx.animation.Transition;
import javafx.beans.property.ObjectProperty;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class ColorChangeTransition extends Transition {

	private Color fromColor;
	private Color toColor;
	private final ObjectProperty<Color> pyTargetColor;

	public ColorChangeTransition(Duration duration, Color fromColor, Color toColor, ObjectProperty<Color> pyTargetColor) {
		this.fromColor = fromColor;
		this.toColor = toColor;
		this.pyTargetColor = pyTargetColor;
		setCycleCount(1);
		setCycleDuration(duration);
	}

	@Override
	protected void interpolate(double t) {
		pyTargetColor.setValue(fromColor.interpolate(toColor, t));
	}
}