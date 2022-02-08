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
package de.amr.games.pacman.ui.fx._2d.scene.common;

import static de.amr.games.pacman.model.world.World.t;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.event.DefaultGameEventHandler;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.entity.common.GameScore2D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Base class of all 2D scenes that get rendered inside the canvas provided by the UI.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractGameScene2D extends DefaultGameEventHandler implements GameScene {

	protected final GameController gameController;
	protected final V2i unscaledSize;

	protected GameModel game;
	protected SubScene fxSubScene;
	protected GraphicsContext gc;
	protected GameScore2D score2D;
	protected GameScore2D highScore2D;

	public AbstractGameScene2D(GameController gameController, V2i unscaledSize) {
		this.gameController = gameController;
		this.unscaledSize = unscaledSize;
	}

	public void setCanvas(Canvas canvas) {
		gc = canvas.getGraphicsContext2D();
	}

	@Override
	public SubScene createSubScene(Scene parent) {
		Canvas canvas = gc.getCanvas();
		fxSubScene = new SubScene(new StackPane(canvas), unscaledSize.x, unscaledSize.y);
		fxSubScene.widthProperty().bind(canvas.widthProperty());
		fxSubScene.heightProperty().bind(canvas.heightProperty());
		return fxSubScene;
	}

	@Override
	public SubScene getSubScene() {
		return fxSubScene;
	}

	@Override
	public void init() {
		game = gameController.game;
		score2D = new GameScore2D("SCORE", t(1), t(1), game, false, Env.r2D);
		highScore2D = new GameScore2D("HIGH SCORE", t(16), t(1), game, true, Env.r2D);
	}

	@Override
	public final void update() {
		doUpdate();
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		doRender();
		if (Env.$tilesVisible.get()) {
			drawTileBorders();
		}
	}

	@Override
	public boolean is3D() {
		return false;
	}

	private void drawTileBorders() {
		gc.setStroke(Color.rgb(160, 160, 160, 0.5));
		gc.setLineWidth(1);
		for (int row = 0; row < 36; ++row) {
			line(0, t(row), t(28), t(row));
		}
		for (int col = 0; col < 28; ++col) {
			line(t(col), 0, t(col), t(36));
		}
	}

	// WTF
	private void line(double x1, double y1, double x2, double y2) {
		double offset = 0.5;
		gc.strokeLine(x1 + offset, y1 + offset, x2 + offset, y2 + offset);
	}

	/**
	 * Updates the scene. Subclasses override this method.
	 */
	protected abstract void doUpdate();

	/**
	 * Renders the scene content. Subclasses override this method.
	 */
	protected abstract void doRender();
}