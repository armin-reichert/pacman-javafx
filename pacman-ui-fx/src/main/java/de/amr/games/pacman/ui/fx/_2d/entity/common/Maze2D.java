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

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
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
public class Maze2D implements Renderable2D {

	private final Rendering2D rendering;
	private final int x;
	private final int y;
	private final TimedSequence<Boolean> energizerAnimation;
	private final Timeline flashingAnimation;

	private GameModel game;
	private boolean flashing;

	/**
	 * 
	 * @param x         x position (in pixels)
	 * @param y         y position (in pixels)
	 * @param game      the game model
	 * @param rendering the 2D rendering
	 */
	public Maze2D(int x, int y, GameModel game, Rendering2D rendering) {
		this.x = x;
		this.y = y;
		this.rendering = rendering;
		energizerAnimation = TimedSequence.pulse().frameDuration(10);
		flashingAnimation = new Timeline(new KeyFrame(Duration.millis(150), e -> flashing = !flashing));
		setGame(game);
	}

	public void setGame(GameModel game) {
		this.game = game;
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

	@Override
	public void render(GraphicsContext g) {
		if (flashingAnimation.getStatus() == Status.RUNNING) {
			if (flashing) {
				rendering.renderMazeFlashing(g, game.mazeNumber, x, y);
			} else {
				rendering.renderMazeEmpty(g, game.mazeNumber, x, y);
			}
		} else {
			rendering.renderMazeFull(g, game.mazeNumber, x, y);
			g.setFill(Color.BLACK);
			if (!energizerAnimation.animate()) {
				game.world.energizerTiles().forEach(tile -> g.fillRect(t(tile.x), t(tile.y), TS, TS));
			}
			game.world.tiles().filter(game::isFoodEaten).forEach(tile -> g.fillRect(t(tile.x), t(tile.y), TS, TS));
		}
	}
}