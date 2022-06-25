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
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class ColorFlashingTransition extends Transition {

	public final ObjectProperty<Color> colorProperty = new SimpleObjectProperty<>();

	private final Color colorStart;
	private final Color colorEnd;

	public ColorFlashingTransition(Color colorStart, Color colorEnd) {
		this.colorStart = colorStart;
		this.colorEnd = colorEnd;
		colorProperty.set(colorStart);
		setCycleCount(INDEFINITE);
		setCycleDuration(Duration.seconds(0.2));
		setAutoReverse(true);
	}

	public Color getColor() {
		return colorProperty.get();
	}

	@Override
	protected void interpolate(double t) {
		if (t <= 0.25 || t >= 0.75) { // TODO check this
			colorProperty.set(colorStart.interpolate(colorEnd, t));
		}
	}
}