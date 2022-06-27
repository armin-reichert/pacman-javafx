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
package de.amr.games.pacman.ui.fx._3d.entity;

import de.amr.games.pacman.lib.V2i;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.ScaleTransition;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;

/**
 * 3D energizer pellet.
 * 
 * @author Armin Reichert
 */
public class Energizer3D extends Pellet3D {

	private final ScaleTransition animation;

	public Energizer3D(V2i tile, PhongMaterial material, double radius) {
		super(tile, material, radius);
		animation = new ScaleTransition(Duration.seconds(1.0 / 6), this);
		animation.setAutoReverse(true);
		animation.setCycleCount(Animation.INDEFINITE);
		animation.setFromX(1.0);
		animation.setFromY(1.0);
		animation.setFromZ(1.0);
		animation.setToX(0.1);
		animation.setToY(0.1);
		animation.setToZ(0.1);
	}

	public void startBlinking() {
		animation.playFromStart();
	}

	public void stopBlinking() {
		animation.stop();
	}

	@Override
	public String toString() {
		return String.format("[Energizer, tile; %s, animation running: %s]", tile(),
				animation.getStatus() == Status.RUNNING);
	}
}