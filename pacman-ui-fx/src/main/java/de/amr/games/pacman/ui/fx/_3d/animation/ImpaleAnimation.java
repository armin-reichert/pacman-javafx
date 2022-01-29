/*
MIT License

Copyright (c) 2021 Armin Reichert

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

import static de.amr.games.pacman.ui.fx.util.Animations.lerp;

import javafx.animation.Transition;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.util.Duration;

/**
 * Let's Pac-Man impale.
 * 
 * @author Armin Reichert
 */
public class ImpaleAnimation extends Transition {

	private final Shape3D shape;
	private final PhongMaterial material;
	private Color fromColor;
	private Color toColor;

	public ImpaleAnimation(Duration duration, Shape3D shape, Color fromColor) {
		this.shape = shape;
		this.fromColor = fromColor;
		this.toColor = Color.GHOSTWHITE;
		this.material = new PhongMaterial(fromColor);
		setCycleCount(1);
		setCycleDuration(duration);
	}

	@Override
	protected void interpolate(double t) {
		if (t == 0) {
			shape.setMaterial(material);
		}
		Color color = Color.color( //
				lerp(fromColor.getRed(), toColor.getRed(), t), //
				lerp(fromColor.getGreen(), toColor.getGreen(), t), //
				lerp(fromColor.getBlue(), toColor.getBlue(), t));
		material.setDiffuseColor(color);
	}
}