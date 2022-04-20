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

import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.event.DefaultGameEventHandler;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.entity.common.GameScore2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Rendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Rendering2D_PacMan;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

/**
 * Base class of all 2D scenes that get rendered inside a canvas.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene2D extends DefaultGameEventHandler implements GameScene {

	protected final GameController gc;
	protected final SubScene fxSubScene;
	protected final Canvas canvas;
	protected final V2i unscaledSize;
	protected final double aspectRatio;

	protected GameModel game;
	protected Rendering2D r2D;
	protected GameScore2D score2D;
	protected GameScore2D highScore2D;

	/**
	 * @param gc           game controller
	 * @param unscaledSize logical scene size (number of tiles x tile size)
	 */
	public GameScene2D(GameController gc, V2i unscaledSize) {
		this.gc = gc;
		this.unscaledSize = unscaledSize;
		this.aspectRatio = (double) unscaledSize.x / unscaledSize.y;
		this.canvas = new Canvas();
		StackPane root = new StackPane(canvas);
		root.setBackground(U.colorBackground(Color.BLACK));
		fxSubScene = new SubScene(root, unscaledSize.x, unscaledSize.y);
		canvas.widthProperty().bind(fxSubScene.widthProperty());
		canvas.heightProperty().bind(fxSubScene.heightProperty());
	}

	@Override
	public void setContext() {
		game = gc.game;
		r2D = switch (gc.gameVariant) {
		case MS_PACMAN -> Rendering2D_MsPacMan.get();
		case PACMAN -> Rendering2D_PacMan.get();
		};
		SoundManager.get().stopAll(); // TODO: check this
		SoundManager.get().selectGameVariant(gc.gameVariant);
	}

	@Override
	public SubScene getFXSubScene() {
		return fxSubScene;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	@Override
	public void resize(double height) {
		double width = aspectRatio * height;
		double scaling = height / unscaledSize.y;
		fxSubScene.setWidth(width);
		fxSubScene.setHeight(height);
		canvas.getTransforms().setAll(new Scale(scaling, scaling));
	}

	protected void createScores() {
		score2D = new GameScore2D(game);
		score2D.x = t(1);
		score2D.y = t(1);
		score2D.title = "SCORE";
		score2D.showHighscore = false;
		highScore2D = new GameScore2D(game);
		highScore2D.x = t(16);
		highScore2D.y = t(1);
		highScore2D.title = "HIGH SCORE";
		highScore2D.showHighscore = true;
	}

	/**
	 * Updates the scene. Subclasses override this method.
	 */
	protected abstract void doUpdate();

	/**
	 * Renders the scene content. Subclasses override this method.
	 */
	protected abstract void doRender(GraphicsContext g);

	@Override
	public final void update() {
		doUpdate();
		var g = canvas.getGraphicsContext2D();
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		doRender(g);
		if (Env.$tilesVisible.get()) {
			drawTileBorders(g);
		}
	}

	@Override
	public boolean is3D() {
		return false;
	}

	private void drawTileBorders(GraphicsContext g) {
		g.setStroke(Color.rgb(160, 160, 160, 0.5));
		g.setLineWidth(0.5);
		for (int y = 0; y < unscaledSize.y; y += TS) {
			line(g, 0, y, unscaledSize.x, y);
		}
		for (int x = 0; x < unscaledSize.x; x += TS) {
			line(g, x, 0, x, unscaledSize.y);
		}
	}

	// WTF
	private static void line(GraphicsContext g, double x1, double y1, double x2, double y2) {
		double offset = 0.5;
		g.strokeLine(x1 + offset, y1 + offset, x2 + offset, y2 + offset);
	}
}