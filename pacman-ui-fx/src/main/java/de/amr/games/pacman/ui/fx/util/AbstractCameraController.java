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
package de.amr.games.pacman.ui.fx.util;

import javafx.event.EventHandler;
import javafx.scene.Camera;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;

/**
 * Base class for all camera controllers.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractCameraController implements EventHandler<KeyEvent> {

	protected final Camera cam;

	public AbstractCameraController(Camera cam) {
		this.cam = cam;
	}

	@Override
	public void handle(KeyEvent event) {
	}

	public void reset() {
	}

	public void follow(Node target) {
	}

	public double approach(double current, double target) {
		return current + (target - current) * 0.02;
	}

	public String info() {
		return String.format("x=%.0f y=%.0f z=%.0f rot=%.0f", cam.getTranslateX(), cam.getTranslateY(),
				cam.getTranslateZ(), cam.getRotate());
	}
}