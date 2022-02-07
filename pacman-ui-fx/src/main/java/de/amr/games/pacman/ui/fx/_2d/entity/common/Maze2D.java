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
package de.amr.games.pacman.ui.fx._2d.entity.common;

import static de.amr.games.pacman.model.world.World.TS;
import static de.amr.games.pacman.model.world.World.t;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx.app.Env;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * 2D representation of the maze. Implements the flashing animation played on the end of each level.
 * 
 * @author Armin Reichert
 */
public class Maze2D {

	private final GameModel game;
	private final int x;
	private final int y;
	private final Rendering2D r2D;
	private final TimedSequence<Boolean> energizerAnimation;
	private final Timeline flashingAnimation;

	private boolean flashing;

	/**
	 * @param x    x position (in pixels)
	 * @param y    y position (in pixels)
	 * @param game the game model
	 * @param r2D  the 2D rendering
	 */
	public Maze2D(int x, int y, GameModel game, Rendering2D r2D) {
		this.x = x;
		this.y = y;
		this.r2D = r2D;
		this.game = game;
		energizerAnimation = TimedSequence.pulse().frameDuration(10);
		flashingAnimation = new Timeline(new KeyFrame(Duration.millis(150), e -> flashing = !flashing));
		flashingAnimation.setCycleCount(2 * game.numFlashes);
	}

	public boolean isFlashing() {
		return flashingAnimation.getStatus() == Status.RUNNING;
	}

	public Timeline getFlashingAnimation() {
		return flashingAnimation;
	}

	public TimedSequence<Boolean> getEnergizerAnimation() {
		return energizerAnimation;
	}

	public void render(GraphicsContext gc) {
		if (flashingAnimation.getStatus() == Status.RUNNING) {
			if (flashing) {
				r2D.renderMazeFlashing(gc, game.mazeNumber, x, y);
			} else {
				r2D.renderMazeEmpty(gc, game.mazeNumber, x, y);
			}
		} else {
			r2D.renderMazeFull(gc, game.mazeNumber, x, y);
			Color dark = Color.BLACK;
			if (!energizerAnimation.animate()) { // dark phase
				gc.setFill(dark);
				game.world.energizerTiles().forEach(tile -> fillTile(gc, tile, dark));
			}
			game.world.tiles().filter(game::isFoodEaten).forEach(tile -> fillTile(gc, tile, dark));
			if (Env.$tilesVisible.get()) {
				drawTileBorders(gc);
			}
		}
	}

	private void fillTile(GraphicsContext gc, V2i tile, Color color) {
		gc.setFill(color);
		gc.fillRect(t(tile.x) + 0.2, t(tile.y) + 0.2, TS - 0.2, TS - 0.2);
	}

	private void drawTileBorders(GraphicsContext gc) {
		gc.setStroke(Color.rgb(160, 160, 160, 0.5));
		gc.setLineWidth(1);
		for (int row = 0; row < 36; ++row) {
			line(gc, 0, t(row), t(28), t(row));
		}
		for (int col = 0; col < 28; ++col) {
			line(gc, t(col), 0, t(col), t(36));
		}
	}

	// WTF
	private void line(GraphicsContext gc, double x1, double y1, double x2, double y2) {
		double offset = 0.5;
		gc.strokeLine(x1 + offset, y1 + offset, x2 + offset, y2 + offset);
	}
}