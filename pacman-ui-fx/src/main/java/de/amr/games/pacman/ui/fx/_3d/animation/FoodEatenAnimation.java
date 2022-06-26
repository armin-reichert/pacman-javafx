/*
MIT License

Copyright (c) 2022 Armin Reichert

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

import java.util.Random;

import de.amr.games.pacman.lib.U;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class FoodEatenAnimation extends Transition {

	private final Shape3D foodShape;
	private final Random rnd = new Random();

	private double scaleFactor;
	private double maxHeight;

	public FoodEatenAnimation(Shape3D foodShape, Color foodColor) {
		this.foodShape = foodShape;
		scaleFactor = 0.25 + 0.25 * rnd.nextDouble();
		maxHeight = -3 - 60 * rnd.nextDouble();
		foodShape.setMaterial(new PhongMaterial(foodColor.grayscale()));
		setCycleDuration(Duration.seconds(0.75));
		setInterpolator(Interpolator.EASE_BOTH);
		setOnFinished(e -> {
			foodShape.setVisible(false);
			foodShape.setTranslateZ(-3);
			foodShape.setScaleX(1.0);
			foodShape.setScaleY(1.0);
			foodShape.setScaleZ(1.0);
		});
	}

	@Override
	protected void interpolate(double t) {
		var scale = (1 - t) * scaleFactor;
		foodShape.setScaleX(scale);
		foodShape.setScaleY(scale);
		foodShape.setScaleZ(scale);
		foodShape.setTranslateZ(U.lerp(-3, maxHeight, t));
	}
}