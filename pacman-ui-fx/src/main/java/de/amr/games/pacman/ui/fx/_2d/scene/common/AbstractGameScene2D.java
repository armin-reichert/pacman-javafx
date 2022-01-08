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

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.OptionalDouble;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx.scene.AbstractGameScene;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
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

	protected final double unscaledWidth;
	protected final double unscaledHeight;
	protected final double aspectRatio;

	protected final Rendering2D rendering;
	protected final SoundManager sounds;

	protected SubScene subSceneFX;
	protected GraphicsContext gc;

	public AbstractGameScene2D(Rendering2D rendering, SoundManager sounds, int tilesX, int tilesY) {
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
	public final void update() {
		if (gameController != null) {
			doUpdate();
		}
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		doRender();
	}

	/**
	 * Updates the scene. Subclasses override this method.
	 */
	protected abstract void doUpdate();

	/**
	 * Renders the scene content. Subclasses override this method.
	 */
	protected abstract void doRender();

	// this is used in play scene and intermission scenes, so define it here
	protected void renderLevelCounter(V2i tileRight) {
		int levelNumber = game.levelNumber;
		int x = tileRight.x * TS, y = tileRight.y * TS;
		int firstLevel = Math.max(1, levelNumber - 6);
		for (int level = firstLevel; level <= levelNumber; ++level) {
			Rectangle2D sprite = rendering.getSymbolSprites().get(game.levelSymbol(level));
			rendering.renderSprite(gc, sprite, x, y);
			x -= t(2);
		}
	}
}