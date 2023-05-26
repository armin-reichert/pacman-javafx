/*
MIT License

Copyright (c) 2023 Armin Reichert

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

package de.amr.games.pacman.ui.fx.v3d.scene;

import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;
import de.amr.games.pacman.ui.fx.scene2d.PlaySceneScaled;
import de.amr.games.pacman.ui.fx.v3d.app.PacManGames3d;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;

/**
 * @author Armin Reichert
 */
public class PictureInPicture {

	public final DoubleProperty heightPy = new SimpleDoubleProperty(PacManGames3d.PIP_MIN_HEIGHT);

	public final DoubleProperty opacityPy = new SimpleDoubleProperty(1.0);

	private final GameScene2D gameScene;

	public PictureInPicture() {
		gameScene = new PlaySceneScaled();
		gameScene.fxSubScene().heightProperty().bind(heightPy);
		gameScene.fxSubScene().widthProperty().bind(heightPy.multiply(GameScene2D.ASPECT_RATIO));
		gameScene.fxSubScene().opacityProperty().bind(opacityPy);
		gameScene.fxSubScene().setVisible(false);
	}

	public Node root() {
		return gameScene.fxSubScene();
	}

	public void render() {
		gameScene.render();
	}

	public void update(GameScene master, boolean on) {
		if (master != null) {
			gameScene.setContext(master.context());
			gameScene.fxSubScene().setVisible(on && master.is3D());
		} else {
			gameScene.fxSubScene().setVisible(false);
		}
	}
}