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
package de.amr.games.pacman.ui.fx._3d.scene;

import javafx.event.EventHandler;
import javafx.scene.Camera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

/**
 * Base class of all camera controllers.
 * 
 * @author Armin Reichert
 */
public abstract class CameraController implements EventHandler<KeyEvent> {

	public final Camera perspectiveCam = new PerspectiveCamera(true);
	public boolean keysEnabled = false;

	abstract void reset();

	abstract void update(PlayScene3D scene);

	@Override
	public void handle(KeyEvent e) {
		if (!keysEnabled) {
			return;
		}
		if (e.isControlDown()) {
			switch (e.getCode()) {
			case DIGIT0:
				perspectiveCam.setTranslateX(0);
				perspectiveCam.setTranslateY(0);
				perspectiveCam.setTranslateZ(-630);
				perspectiveCam.setRotationAxis(Rotate.X_AXIS);
				perspectiveCam.setRotate(0);
				perspectiveCam.setRotationAxis(Rotate.Y_AXIS);
				perspectiveCam.setRotate(0);
				perspectiveCam.setRotationAxis(Rotate.Z_AXIS);
				perspectiveCam.setRotate(0);
				break;
			case LEFT:
				perspectiveCam.setTranslateX(perspectiveCam.getTranslateX() + 10);
				break;
			case RIGHT:
				perspectiveCam.setTranslateX(perspectiveCam.getTranslateX() - 10);
				break;
			case UP:
				perspectiveCam.setTranslateY(perspectiveCam.getTranslateY() + 10);
				break;
			case DOWN:
				perspectiveCam.setTranslateY(perspectiveCam.getTranslateY() - 10);
				break;
			case PLUS:
				perspectiveCam.setTranslateZ(perspectiveCam.getTranslateZ() + 10);
				break;
			case MINUS:
				perspectiveCam.setTranslateZ(perspectiveCam.getTranslateZ() - 10);
				break;
			default:
				break;
			}
		}
		if (e.isShiftDown()) {
			switch (e.getCode()) {
			case DOWN:
				perspectiveCam.setRotationAxis(Rotate.X_AXIS);
				perspectiveCam.setRotate((360 + perspectiveCam.getRotate() - 1) % 360);
				break;
			case UP:
				perspectiveCam.setRotationAxis(Rotate.X_AXIS);
				perspectiveCam.setRotate((perspectiveCam.getRotate() + 1) % 360);
				break;
			case LEFT:
				perspectiveCam.setRotationAxis(Rotate.Z_AXIS);
				perspectiveCam.setRotate((360 + perspectiveCam.getRotate() - 1) % 360);
				break;
			case RIGHT:
				perspectiveCam.setRotationAxis(Rotate.Z_AXIS);
				perspectiveCam.setRotate((360 + perspectiveCam.getRotate() + 1) % 360);
				break;
			default:
				break;
			}
		}
	}

	public String info() {
		return String.format("x=%.0f y=%.0f z=%.0f rot=%.0f", perspectiveCam.getTranslateX(), perspectiveCam.getTranslateY(), perspectiveCam.getTranslateZ(),
				perspectiveCam.getRotate());
	}
}