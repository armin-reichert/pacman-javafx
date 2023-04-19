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
package de.amr.games.pacman.ui.fx._2d.scene;

import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.toPx;
import static de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D.drawText;
import static java.util.Objects.requireNonNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.ArcadeTheme;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;

/**
 * Base class of all 2D scenes. Each 2D scene has its own canvas.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene2D implements GameScene {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private static final float WIDTH = World.TILES_X * TS;
	private static final float HEIGHT = World.TILES_Y * TS;
	private static final float ASPECT_RATIO = WIDTH / HEIGHT;

	public final BooleanProperty infoVisiblePy = new SimpleBooleanProperty(this, "infoVisible", false);
	protected final GameSceneContext context;
	protected final SubScene fxSubScene;
	protected final Canvas canvas = new Canvas();

	protected GameScene2D(GameController gameController) {
		requireNonNull(gameController);

		context = new GameSceneContext(gameController);
		fxSubScene = new SubScene(new StackPane(canvas), WIDTH, HEIGHT);
		canvas.widthProperty().bind(fxSubScene.widthProperty());
		canvas.heightProperty().bind(fxSubScene.heightProperty());
	}

	@Override
	public GameSceneContext context() {
		return context;
	}

	@Override
	public SubScene fxSubScene() {
		return fxSubScene;
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
		if (height <= 0) {
			throw new IllegalArgumentException("Scene height must be positive");
		}
		var width = ASPECT_RATIO * height;
		var scaling = height / HEIGHT;
		fxSubScene.setWidth(width);
		fxSubScene.setHeight(height);
		canvas.getTransforms().setAll(new Scale(scaling, scaling));
		LOG.trace("2D game scene resized: %.0f x %.0f, canvas scaling: %.2f (%s)", width, height, scaling,
				getClass().getSimpleName());
	}

	@Override
	public void render() {
		var g = canvas.getGraphicsContext2D();
		var r = context.rendering2D();
		r.fillCanvas(g, ArcadeTheme.BLACK);
		if (context.isScoreVisible()) {
			context.game().score()
					.ifPresent(score -> r.drawScore(g, score, "SCORE", r.screenFont(8), ArcadeTheme.PALE, toPx(1), toPx(1)));
			context.game().highScore()
					.ifPresent(score -> r.drawScore(g, score, "HIGH SCORE", r.screenFont(8), ArcadeTheme.PALE, toPx(16), toPx(1)));
		}
		drawScene(g);
		if (context.isCreditVisible()) {
			drawText(g, "CREDIT %2d".formatted(context.game().credit()), ArcadeTheme.PALE, r.screenFont(TS), toPx(2), toPx(36) - 1);
		}
		if (infoVisiblePy.get()) {
			drawInfo(g);
		}
	}

	/**
	 * Draws the scene content, e.g. the maze and the guys.
	 * 
	 * @param g graphics context
	 */
	protected abstract void drawScene(GraphicsContext g);

	/**
	 * Draws scene info, e.g. maze structure and special tiles
	 * 
	 * @param g graphics context
	 */
	protected void drawInfo(GraphicsContext g) {
		// empty by default
	}

	protected void drawLevelCounter(GraphicsContext g) {
		context.rendering2D().drawLevelCounter(g, context.level().map(GameLevel::number), context.game().levelCounter());
	}

	@Override
	public boolean is3D() {
		return false;
	}
}