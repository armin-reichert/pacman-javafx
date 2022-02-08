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

import javafx.animation.FillTransition;
import javafx.animation.Transition;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.util.Duration;

/**
 * Kind of a {@link FillTransition} for 3D shapes.
 * 
 * @author Armin Reichert
 */
public class FillTransition3D extends Transition {

	private final Shape3D shape;
	private final PhongMaterial material;
	private Color fromColor;
	private Color toColor;

	public FillTransition3D(Duration duration, Shape3D shape, Color fromColor, Color toColor) {
		this.shape = shape;
		this.fromColor = fromColor;
		this.toColor = toColor;
		this.material = new PhongMaterial(fromColor);
		setCycleCount(1);
		setCycleDuration(duration);
	}

	@Override
	protected void interpolate(double t) {
		material.setDiffuseColor(fromColor.interpolate(toColor, t));
		shape.setMaterial(material);
	}
}