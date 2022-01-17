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

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.OptionalDouble;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.fx._2d.entity.common.GameScore2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.AbstractGameScene;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import de.amr.games.pacman.ui.fx.util.AbstractCameraController;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

/**
 * Base class of all 2D scenes that get rendered inside a canvas.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractGameScene2D extends AbstractGameScene {

	public static final int DEFAULT_TILES_X = 28;
	public static final int DEFAULT_TILES_Y = 36;

	protected final double unscaledWidth;
	protected final double unscaledHeight;
	protected final double aspectRatio;

	protected final int levelCounterRightX = t(25);
	protected final int levelCounterRightY = t(34);

	protected GameScore2D score2D;
	protected GameScore2D highScore2D;

	protected final Rendering2D rendering;
	protected final SoundManager sounds;

	protected SubScene subSceneFX;
	protected GraphicsContext gc;

	public AbstractGameScene2D(PacManGameUI ui, Rendering2D rendering, SoundManager sounds) {
		this(ui, DEFAULT_TILES_X, DEFAULT_TILES_Y, rendering, sounds);
	}

	public AbstractGameScene2D(PacManGameUI ui, int tilesX, int tilesY, Rendering2D rendering, SoundManager sounds) {
		super(ui);
		this.unscaledWidth = t(tilesX);
		this.unscaledHeight = t(tilesY);
		this.aspectRatio = unscaledWidth / unscaledHeight;
		this.rendering = rendering;
		this.sounds = sounds;
	}

	public void setCanvas(Canvas canvas) {
		gc = canvas.getGraphicsContext2D();
		canvas.setWidth(unscaledWidth);
		canvas.setHeight(unscaledHeight);
		subSceneFX = new SubScene(new Group(canvas), unscaledWidth, unscaledHeight);
		subSceneFX.widthProperty().bind(canvas.widthProperty());
		subSceneFX.heightProperty().bind(canvas.heightProperty());
	}

	@Override
	public boolean is3D() {
		return false;
	}

	@Override
	public AbstractCameraController currentCameraController() {
		return null;
	}

	@Override
	public final OptionalDouble aspectRatio() {
		return OptionalDouble.of(aspectRatio);
	}

	@Override
	public void resize(double width, double height) {
		// resize canvas to take given height and respect aspect ratio
		Canvas canvas = gc.getCanvas();
		canvas.setWidth(aspectRatio().getAsDouble() * height);
		canvas.setHeight(height);
		double scaling = height / unscaledHeight;
		canvas.getTransforms().setAll(new Scale(scaling, scaling));
	}

	@Override
	public SubScene getSubSceneFX() {
		return subSceneFX;
	}

	@Override
	public void init(PacManGameController gameController) {
		super.init(gameController);
		score2D = new GameScore2D("SCORE", t(1), t(1), game, false, rendering);
		highScore2D = new GameScore2D("HIGH SCORE", t(16), t(1), game, true, rendering);
	}

	@Override
	public final void update() {
		if (gameController != null) {
			doUpdate();
		}
		drawBackground();
		doRender();
		drawTileBorders();
	}

	private void drawBackground() {
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
	}

	private void drawTileBorders() {
		if (Env.$tilesVisible.get()) {
			gc.setStroke(Color.rgb(160, 160, 160, 0.5));
			gc.setLineWidth(1);
			for (int row = 0; row < 36; ++row) {
				line(0, t(row), t(28), t(row));
			}
			for (int col = 0; col < 28; ++col) {
				line(t(col), 0, t(col), t(36));
			}
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

	/**
	 * This is used in play scene and intermission scenes, so define it here
	 */
	protected void renderLevelCounter() {
		int x = levelCounterRightX, y = levelCounterRightY;
		int firstLevel = Math.max(1, game.levelNumber - 6);
		for (int level = firstLevel; level <= game.levelNumber; ++level) {
			Rectangle2D r = rendering.getSymbolSprites().get(game.levelSymbol(level));
			rendering.renderSprite(gc, r, x, y);
			x -= t(2);
		}
	}
}