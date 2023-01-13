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

import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;

/**
 * @author Armin Reichert
 */
public class CamFirstPerson extends GameSceneCamera {

	private Pac pac;

	public void setPac(Pac pac) {
		this.pac = pac;
	}

	@Override
	public String toString() {
		return "Near Player";
	}

	@Override
	public void reset() {
		setNearClip(0.1);
		setFarClip(1000.0);
		getTransforms().clear();
		getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		getTransforms().add(new Rotate(90, Rotate.Y_AXIS));
	}

	@Override
	public void update(Node target) {
		var ref = pac.position().plus(pac.moveDir().vector().toFloatVec().scaled(6));
		setTranslateX(ref.x() - GameScene.DEFAULT_SIZE.x() / 2);
		setTranslateY(ref.y() - GameScene.DEFAULT_SIZE.y() / 2);
		setTranslateZ(-6);
		setRotationAxis(Rotate.Z_AXIS);
		setRotate(rotate(pac.moveDir()));
	}

	private double rotate(Direction dir) {
		return switch (dir) {
		case LEFT -> 180;
		case RIGHT -> 0;
		case UP -> 270;
		case DOWN -> 90;
		};
	}
}