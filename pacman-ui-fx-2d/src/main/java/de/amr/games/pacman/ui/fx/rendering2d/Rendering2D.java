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
package de.amr.games.pacman.ui.fx.rendering2d;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

import java.util.List;

import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.Score;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.Game2d;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Common interface for the Pac-Man and Ms. Pac-Man game renderers.
 * 
 * @author Armin Reichert
 */
public interface Rendering2D {

	static void drawTileGrid(GraphicsContext g, int tilesX, int tilesY) {
		g.save();
		g.translate(0.5, 0.5);
		g.setStroke(ArcadeTheme.PALE);
		g.setLineWidth(0.2);
		for (int row = 0; row <= tilesY; ++row) {
			g.strokeLine(0, TS * (row), tilesX * TS, TS * (row));
		}
		for (int col = 0; col <= tilesY; ++col) {
			g.strokeLine(TS * (col), 0, TS * (col), tilesY * TS);
		}
		g.restore();
	}

	static void drawText(GraphicsContext g, String text, Color color, Font font, double x, double y) {
		g.setFont(font);
		g.setFill(color);
		g.fillText(text, x, y);
	}

	static void hideTileContent(GraphicsContext g, Vector2i tile) {
		g.setFill(ArcadeTheme.BLACK);
		g.fillRect(TS * tile.x(), TS * tile.y(), TS, TS);
	}

	static void drawScore(GraphicsContext g, Score score, String title, double x, double y) {
		drawText(g, title, ArcadeTheme.PALE, Game2d.assets.arcadeFont, x, y);
		var pointsText = "%02d".formatted(score.points());
		drawText(g, "%7s".formatted(pointsText), ArcadeTheme.PALE, Game2d.assets.arcadeFont, x, y + TS + 1);
		if (score.points() != 0) {
			drawText(g, "L%d".formatted(score.levelNumber()), ArcadeTheme.PALE, Game2d.assets.arcadeFont, x + TS * 8,
					y + TS + 1);
		}
	}

	static void drawCredit(GraphicsContext g, int credit, double x, double y) {
		drawText(g, "CREDIT %2d".formatted(credit), ArcadeTheme.PALE, Game2d.assets.arcadeFont, x, y);
	}

	Spritesheet spritesheet();

	/**
	 * @return sprite used in lives counter
	 */
	Rectangle2D livesCounterSprite();

	/**
	 * @return sprite showing ghost value (200, 400, 800, 1600)
	 */
	Rectangle2D ghostValueSprite(int index);

	/**
	 * @param symbol bonus symbol (index)
	 * @return sprite showing bonus symbol (cherries, strawberry, ...)
	 */
	Rectangle2D bonusSymbolSprite(int symbol);

	/**
	 * @param symbol bonus symbol (index)
	 * @return sprite showing bonus symbol value (100, 300, ...)
	 */
	Rectangle2D bonusValueSprite(int symbol);

	/**
	 * Draws a sprite at the given position. The position specifies the left-upper corner.
	 * 
	 * @param g graphics context
	 * @param r sprite (may be null)
	 * @param x x position
	 * @param y y position
	 */
	default void drawSprite(GraphicsContext g, Rectangle2D r, double x, double y) {
		if (r != null) {
			g.drawImage(spritesheet().source, r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight(), x, y, r.getWidth(),
					r.getHeight());
		}
	}

	/**
	 * Draws a sprite centered over a one "square tile" large box (bounding box of creature). The position specifies the
	 * left-upper corner of the bounding box. Note that the sprites for Pac-Man and the ghosts are 16 pixels wide but the
	 * bounding box is only 8 pixels (one square tile) wide.
	 * 
	 * @param g graphics context
	 * @param r spritesheet region (may be null)
	 * @param x x coordinate of left-upper corner of bounding box
	 * @param y y coordinate of left-upper corner of bounding box
	 */
	default void drawSpriteOverBoundingBox(GraphicsContext g, Rectangle2D r, double x, double y) {
		if (r != null) {
			drawSprite(g, r, x + HTS - r.getWidth() / 2, y + HTS - r.getHeight() / 2);
		}
	}

	/**
	 * Draws the sprite over the bounding box of the given entity (if visible).
	 * 
	 * @param g      graphics context
	 * @param entity an entity like Pac-Man or a ghost
	 * @param r      the sprite
	 */
	default void drawEntitySprite(GraphicsContext g, Entity entity, Rectangle2D r) {
		checkNotNull(entity);
		if (entity.isVisible()) {
			drawSpriteOverBoundingBox(g, r, entity.position().x(), entity.position().y());
		}
	}

	default void drawPac(GraphicsContext g, Pac pac) {
		pac.animation().ifPresent(animation -> drawEntitySprite(g, pac, (Rectangle2D) animation.frame()));
	}

	default void drawGhost(GraphicsContext g, Ghost ghost) {
		ghost.animation().ifPresent(animation -> drawEntitySprite(g, ghost, (Rectangle2D) animation.frame()));
	}

	void drawBonus(GraphicsContext g, Bonus bonus);

	void drawMaze(GraphicsContext g, double x, double y, int mazeNumber, World world);

	/**
	 * @param g            graphics context
	 * @param xr           x coordinate (right-upper corner, default: 24 * TS)
	 * @param yr           y coordinate (right-upper corner, default: 34 * TS)
	 * @param levelSymbols symbols to draw
	 */
	default void drawLevelCounter(GraphicsContext g, double xr, double yr, List<Byte> levelSymbols) {
		double x = xr;
		for (var symbol : levelSymbols) {
			drawSprite(g, bonusSymbolSprite(symbol), x, yr);
			x -= TS * 2;
		}
	}

	void drawLivesCounter(GraphicsContext g, int numLivesDisplayed);

	// TODO this is not the last word on this
	default void drawLivesCounter(GraphicsContext g, Spritesheet ss, int numLivesDisplayed) {
		if (numLivesDisplayed <= 0) {
			return;
		}
		int x = TS * (2);
		int y = TS * (World.TILES_Y - 2);
		int maxLives = 5;
		for (int i = 0; i < Math.min(numLivesDisplayed, maxLives); ++i) {
			drawSprite(g, livesCounterSprite(), x + TS * (2 * i), y);
		}
		// text indicating that more lives are available than displayed
		int excessLives = numLivesDisplayed - maxLives;
		if (excessLives > 0) {
			Rendering2D.drawText(g, "+" + excessLives, ArcadeTheme.YELLOW, Font.font("Serif", FontWeight.BOLD, 8),
					x + TS * (10), y + TS * (1));
		}
	}

	AnimationMap createPacAnimations(Pac pac);

	AnimationMap createGhostAnimations(Ghost ghost);

	AnimationMap createWorldAnimations(World world);
}