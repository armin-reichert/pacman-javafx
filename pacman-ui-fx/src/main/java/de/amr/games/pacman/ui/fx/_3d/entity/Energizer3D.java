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
package de.amr.games.pacman.ui.fx._3d.entity;

import de.amr.games.pacman.lib.V2i;
import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;

/**
 * 3D energizer pellet.
 * 
 * @author Armin Reichert
 */
public class Energizer3D extends Pellet3D {

	public final Animation animation;

	public Energizer3D(V2i tile, PhongMaterial material) {
		super(tile, material);
		setRadius(2.5);
		var scaleTransition = new ScaleTransition(Duration.seconds(1.0 / 6), this);
		scaleTransition.setAutoReverse(true);
		scaleTransition.setCycleCount(Animation.INDEFINITE);
		scaleTransition.setFromX(1.0);
		scaleTransition.setFromY(1.0);
		scaleTransition.setFromZ(1.0);
		scaleTransition.setToX(0.1);
		scaleTransition.setToY(0.1);
		scaleTransition.setToZ(0.1);
		animation = scaleTransition;
	}
}