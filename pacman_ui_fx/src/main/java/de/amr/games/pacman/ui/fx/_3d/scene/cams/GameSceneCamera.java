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
package de.amr.games.pacman.ui.fx._3d.scene.cams;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.KeyEvent;

/**
 * Cameras for the 3D play scene.
 * 
 * @author Armin Reichert
 */
public abstract class GameSceneCamera extends PerspectiveCamera {

	public static void changeBy(DoubleProperty property, double delta) {
		property.set(property.get() + delta);
	}

	protected GameSceneCamera() {
		super(true);
	}

	public String transformInfo() {
		return String.format("x=%.0f y=%.0f z=%.0f rot=%.0f", getTranslateX(), getTranslateY(), getTranslateZ(),
				getRotate());
	}

	public abstract boolean isManuallyConfigurable();

	public void reset() {
	}

	public void update(Node target) {
	}

	public void onKeyPressed(KeyEvent e) {
	}
}