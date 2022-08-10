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
package de.amr.games.pacman.ui.fx.shell;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx._2d.scene.common.PlayScene2D;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Picture-In-Picture view. Displays an embedded 2D game scene.
 * 
 * @author Armin Reichert
 */
public class PiPView {

	public static final V2d MIN_SIZE = new V2d(ArcadeWorld.WORLD_SIZE);
	public static final V2d MAX_SIZE = new V2d(ArcadeWorld.WORLD_SIZE).scaled(2.0);

	public final DoubleProperty heightPy = new SimpleDoubleProperty() {
		@Override
		protected void invalidated() {
			playScene.resize(get());
		}
	};

	private final StackPane root = new StackPane();
	private final PlayScene2D playScene = new PlayScene2D();
	private final Canvas canvas = playScene.getGameSceneCanvas();

	public PiPView() {
		root.setBackground(Ufx.colorBackground(Color.BLACK));
		root.setFocusTraversable(false);
		root.getChildren().add(playScene.getFXSubScene());
		playScene.resize(MIN_SIZE.y());
		playScene.getOverlayCanvas().visibleProperty().unbind();
	}

	public StackPane getRoot() {
		return root;
	}

	public void init(SceneContext context) {
		playScene.setSceneContext(context);
		playScene.init();
	}

	public void draw() {
		var g = canvas.getGraphicsContext2D();
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		playScene.drawSceneContent(g);
		playScene.drawHUD(g);
	}
}