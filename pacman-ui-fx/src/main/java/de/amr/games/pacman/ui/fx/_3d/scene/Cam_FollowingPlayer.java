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
package de.amr.games.pacman.ui.fx._3d.scene;

import static de.amr.games.pacman.ui.fx.util.U.lerp;

import de.amr.games.pacman.ui.fx._3d.entity.Pac3D;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

public class Cam_FollowingPlayer extends GameSceneCamera {

	private double speed = 0.03;

	@Override
	public String toString() {
		return "Following Player";
	}

	@Override
	public void reset() {
		setNearClip(0.1);
		setFarClip(10000.0);
		setRotationAxis(Rotate.X_AXIS);
		setRotate(60);
		setTranslateZ(-160);
	}

	@Override
	public void update(Pac3D player3D) {
		setTranslateX(lerp(getTranslateX(), player3D.getTranslateX() - 100, speed));
		setTranslateY(lerp(getTranslateY(), player3D.getTranslateY() + 60, speed));
	}

	@Override
	public void onKeyPressed(KeyEvent e) {
	}
}