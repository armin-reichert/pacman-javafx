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
package de.amr.games.pacman.ui.fx._2d.entity.common;

import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.SingleGenericAnimation;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.common.DebugDraw;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx.app.Env;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * 2D representation of the world. Implements the flashing animation played on the end of each level.
 * 
 * @author Armin Reichert
 */
public class World2D {

	private final SingleGenericAnimation<Boolean> energizerPulse = SingleGenericAnimation.pulse(10);
	private final SingleGenericAnimation<Image> flashingAnimation;
	private final GameModel game;
	private double x, y;
	private int mazeNumber;

	public World2D(GameModel game, Rendering2D r2D) {
		this.game = game;
		this.x = t(0);
		this.y = t(3);
		this.mazeNumber = r2D.mazeNumber(game.level.number);
		this.flashingAnimation = r2D.createMazeFlashingAnimation(mazeNumber);
	}

	public void startFlashing(int numFlashes) {
		flashingAnimation.repeat(numFlashes);
		flashingAnimation.restart();
	}

	public void showEnergizersOn() {
		energizerPulse.reset();
	}

	public void letEnergizersBlink(boolean blink) {
		if (blink) {
			energizerPulse.restart();
		} else {
			energizerPulse.stop();
			energizerPulse.reset();
		}
	}

	public void render(GraphicsContext g, Rendering2D r2D) {
		if (flashingAnimation.isRunning()) {
			g.drawImage(flashingAnimation.animate(), x, y);
		} else {
			g.drawImage(r2D.getMazeFullImage(mazeNumber), x, y);
			hideEatenFood(g, game.level.world);
		}
		if (Env.$tilesVisible.get()) {
			DebugDraw.drawTileBorders(g, game.level.world.tiles().filter(game.level.world::isIntersection), Color.RED);
		}
	}

	private void hideEatenFood(GraphicsContext g, World world) {
		world.tiles().filter(world::containsEatenFood).forEach(tile -> clearTile(g, tile));
		if (!energizerPulse.animate()) { // dark blinking phase
			world.energizerTiles().forEach(tile -> clearTile(g, tile));
		}
	}

	private void clearTile(GraphicsContext g, V2i tile) {
		g.setFill(Color.BLACK);
		g.fillRect(t(tile.x) + 0.2, t(tile.y) + 0.2, TS - 0.2, TS - 0.2);
	}
}