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

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.ui.fx._3d.entity.Player3D;
import de.amr.games.pacman.ui.fx.util.AbstractCameraController;
import javafx.scene.Camera;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;

/**
 * Point Of View perspective.
 * 
 * @author Armin Reichert
 */
public class Cam_POV extends AbstractCameraController {

	public Cam_POV(Camera cam) {
		super(cam);
	}

	@Override
	public void reset() {
		cam.setNearClip(0.001);
		cam.setFarClip(100);
		cam.setTranslateZ(-40);
	}

	@Override
	public void follow(Node node) {
		Player3D player3D = (Player3D) node;
		V2d offset = new V2d(player3D.player.dir().vec).scaled(8);
		cam.setRotationAxis(Rotate.Z_AXIS);
		cam.setRotate(player3D.getRotate());
		cam.setTranslateX(-14 * 8 + player3D.player.position.x + offset.x);
		cam.setTranslateY(-18 * 8 + player3D.player.position.y + offset.y);
	}

	@Override
	public String toString() {
		return "POV";
	}
}