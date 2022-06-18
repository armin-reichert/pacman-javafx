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

import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
import de.amr.games.pacman.ui.fx.util.CoordinateAxes;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public abstract class GameScene3D implements GameScene {

	protected final Group sceneRoot = new Group();
	protected final Group sceneContent = new Group();
	protected final SubScene fxSubScene;
	protected final AmbientLight light = new AmbientLight(Color.GHOSTWHITE);
	protected final CoordinateAxes axes = new CoordinateAxes(1000);
	protected SceneContext $;

	public GameScene3D() {
		sceneRoot.getChildren().setAll(sceneContent, axes, light);
		fxSubScene = new SubScene(sceneRoot, 100, 100, true, SceneAntialiasing.BALANCED);
		axes.visibleProperty().bind(Env.$axesVisible);
	}

	@Override
	public boolean is3D() {
		return true;
	}

	@Override
	public void setSceneContext(SceneContext context) {
		$ = context;
	}

	@Override
	public SubScene getFXSubScene() {
		return fxSubScene;
	}

	@Override
	public void setParent(Scene parent) {
		fxSubScene.widthProperty().bind(parent.widthProperty());
		fxSubScene.heightProperty().bind(parent.heightProperty());
	}

	protected void blockGameController() {
		$.gameState().timer().resetIndefinitely();
	}

	protected void unblockGameController() {
		$.gameState().timer().expire();
	}
}