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

import de.amr.games.pacman.lib.math.Vector2i;
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

	private final ScaleTransition pumping;

	public Energizer3D(Vector2i tile, PhongMaterial material) {
		super(tile, material, 3.0);
		pumping = new ScaleTransition(Duration.seconds(1.0 / 6), this);
		pumping.setAutoReverse(true);
		pumping.setCycleCount(Animation.INDEFINITE);
		pumping.setFromX(1.0);
		pumping.setFromY(1.0);
		pumping.setFromZ(1.0);
		pumping.setToX(0.1);
		pumping.setToY(0.1);
		pumping.setToZ(0.1);
	}

	public void init() {
		pumping.stop();
		setScaleX(1.0);
		setScaleY(1.0);
		setScaleZ(1.0);
	}

	@Override
	public void eat() {
		pumping.stop();
		super.eat();
	}

	public void startPumping() {
		pumping.playFromStart();
	}

	public void stopPumping() {
		pumping.stop();
	}

	@Override
	public String toString() {
		return String.format("[Energizer, tile; %s, pumping: %s]", tile(), pumping.getStatus() == Status.RUNNING);
	}
}