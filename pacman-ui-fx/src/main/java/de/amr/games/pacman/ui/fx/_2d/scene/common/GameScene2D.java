/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
import static de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D.drawText;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.model.common.Score;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx._2d.rendering.common.ArcadeTheme;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;

/**
 * Base class of all 2D scenes that get rendered inside a canvas.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene2D implements GameScene {

	private static final Logger LOG = LogManager.getFormatterLogger();

	public final BooleanProperty infoVisiblePy = new SimpleBooleanProperty(this, "infoVisible", false);

	protected final SubScene fxSubScene;
	protected final StackPane root;
	protected final Canvas canvas = new Canvas();
	protected final GameSceneContext context;

	protected GameScene2D(GameController gameController) {
		root = new StackPane(canvas);
		fxSubScene = new SubScene(root, ArcadeWorld.SIZE_PX.x(), ArcadeWorld.SIZE_PX.y());
		canvas.widthProperty().bind(fxSubScene.widthProperty());
		canvas.heightProperty().bind(fxSubScene.heightProperty());
		context = new GameSceneContext(gameController);
	}

	@Override
	public void onEmbedIntoParentScene(Scene parentScene) {
		resize(parentScene.getHeight());
	}

	@Override
	public void onParentSceneResize(Scene parentScene) {
		resize(parentScene.getHeight());
	}

	/**
	 * Resizes the game scene to the given height, keeping the aspect ratio. The scaling of the canvas is adapted such
	 * that the canvas content is stretched to the new scene size.
	 * 
	 * @param height new game scene height
	 */
	public void resize(double height) {
		var aspectRatio = fxSubScene.getWidth() / fxSubScene.getHeight();
		var width = aspectRatio * height;
		fxSubScene.setWidth(width);
		fxSubScene.setHeight(height);
		var scaling = height / ArcadeWorld.SIZE_PX.y();
		canvas.getTransforms().setAll(new Scale(scaling, scaling));
		LOG.trace("2D game scene resized: %.0f x %.0f canvas scaling: %.2f (%s)", fxSubScene.getWidth(),
				fxSubScene.getHeight(), scaling, getClass().getSimpleName());
	}

	@Override
	public void render() {
		var g = canvas.getGraphicsContext2D();
		var r = context.r2D();
		g.setFill(ArcadeTheme.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		if (context.isScoreVisible()) {
			context.game().score()
					.ifPresent(score -> drawScore(g, score, "SCORE", r.screenFont(8), ArcadeTheme.PALE, t(1), t(1)));
			context.game().highScore()
					.ifPresent(score -> drawScore(g, score, "HIGH SCORE", r.screenFont(8), ArcadeTheme.PALE, t(16), t(1)));
		}
		drawSceneContent(canvas.getGraphicsContext2D());
		if (context.isCreditVisible()) {
			drawText(g, "CREDIT %2d".formatted(context.game().credit()), ArcadeTheme.PALE, r.screenFont(TS), t(2), t(36) - 1);
		}
		if (infoVisiblePy.get()) {
			drawSceneInfo(g);
		}
	}

	private void drawScore(GraphicsContext g, Score score, String title, Font font, Color color, double x, double y) {
		drawText(g, title, color, font, x, y);
		var pointsText = "%02d".formatted(score.points());
		drawText(g, "%7s".formatted(pointsText), color, font, x, y + TS + 1);
		if (score.points() != 0) {
			drawText(g, "L%d".formatted(score.levelNumber()), color, font, x + t(8), y + TS + 1);
		}
	}

	/**
	 * Draws the scene content, e.g. the maze and the guys.
	 * 
	 * @param g graphics context
	 */
	protected abstract void drawSceneContent(GraphicsContext g);

	/**
	 * Draws scene info, e.g. maze structure and special tiles
	 * 
	 * @param g graphics context
	 */
	protected void drawSceneInfo(GraphicsContext g) {
		// empty by default
	}

	@Override
	public boolean is3D() {
		return false;
	}

	@Override
	public GameSceneContext context() {
		return context;
	}

	@Override
	public SubScene fxSubScene() {
		return fxSubScene;
	}
}