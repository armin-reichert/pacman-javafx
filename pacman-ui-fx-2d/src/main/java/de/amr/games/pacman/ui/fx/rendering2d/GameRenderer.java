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
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Common interface for the Pac-Man and Ms. Pac-Man game renderers.
 * 
 * @author Armin Reichert
 */
public abstract class GameRenderer {

	public static void drawSprite(GraphicsContext g, Image source, Rectangle2D r, double x, double y) {
		if (r != null) {
			g.drawImage(source, r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight(), x, y, r.getWidth(), r.getHeight());
		}
	}

	public static void drawTileGrid(GraphicsContext g, int tilesX, int tilesY) {
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

	public static void drawText(GraphicsContext g, String text, Color color, Font font, double x, double y) {
		g.setFont(font);
		g.setFill(color);
		g.fillText(text, x, y);
	}

	public static void hideTileContent(GraphicsContext g, Vector2i tile) {
		g.setFill(ArcadeTheme.BLACK);
		g.fillRect(TS * tile.x(), TS * tile.y(), TS, TS);
	}

	protected final Theme theme;
	protected final Spritesheet spritesheet;

	protected GameRenderer(Theme theme, Spritesheet spritesheet) {
		checkNotNull(theme);
		checkNotNull(spritesheet);
		this.theme = theme;
		this.spritesheet = spritesheet;
	}

	public Theme theme() {
		return theme;
	}

	public Spritesheet spritesheet() {
		return spritesheet;
	}

	/**
	 * @return sprite used in lives counter
	 */
	public abstract Rectangle2D livesCounterSprite();

	/**
	 * @return sprite showing ghost value (200, 400, 800, 1600)
	 */
	public abstract Rectangle2D ghostValueSprite(int index);

	/**
	 * @param symbol bonus symbol (index)
	 * @return sprite showing bonus symbol (cherries, strawberry, ...)
	 */
	public abstract Rectangle2D bonusSymbolSprite(int symbol);

	/**
	 * @param symbol bonus symbol (index)
	 * @return sprite showing bonus symbol value (100, 300, ...)
	 */
	public abstract Rectangle2D bonusValueSprite(int symbol);

	/**
	 * Draws a sprite at the given position. The position specifies the left-upper corner.
	 * 
	 * @param g graphics context
	 * @param r sprite (may be null)
	 * @param x x position
	 * @param y y position
	 */
	public void drawSprite(GraphicsContext g, Rectangle2D r, double x, double y) {
		drawSprite(g, spritesheet().source(), r, x, y);
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
	public void drawSpriteOverBoundingBox(GraphicsContext g, Rectangle2D r, double x, double y) {
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
	public void drawEntitySprite(GraphicsContext g, Entity entity, Rectangle2D r) {
		checkNotNull(entity);
		if (entity.isVisible()) {
			drawSpriteOverBoundingBox(g, r, entity.position().x(), entity.position().y());
		}
	}

	public void drawPac(GraphicsContext g, Pac pac) {
		pac.animation().ifPresent(animation -> drawEntitySprite(g, pac, (Rectangle2D) animation.frame()));
		if (pac.moveDir() != pac.wishDir() && pac.wishDir() != null) {
			// drawWishDirIndicator(g, pac);
		}
	}

	public void drawWishDirIndicator(GraphicsContext g, Pac pac) {
		g.setFill(Color.RED);
		float r = 4;
		var center = pac.center().plus(pac.wishDir().vector().toFloatVec().scaled(8f)).minus(r, r);
		g.fillOval(center.x(), center.y(), 2 * r, 2 * r);
	}

	public void drawGhost(GraphicsContext g, Ghost ghost) {
		ghost.animation().ifPresent(animation -> drawEntitySprite(g, ghost, (Rectangle2D) animation.frame()));
	}

	public abstract void drawBonus(GraphicsContext g, Bonus bonus);

	public abstract void drawMaze(GraphicsContext g, double x, double y, int mazeNumber, World world);

	/**
	 * @param g            graphics context
	 * @param xr           x coordinate (right-upper corner, default: 24 * TS)
	 * @param yr           y coordinate (right-upper corner, default: 34 * TS)
	 * @param levelSymbols symbols to draw
	 */
	public void drawLevelCounter(GraphicsContext g, double xr, double yr, List<Byte> levelSymbols) {
		double x = xr;
		for (var symbol : levelSymbols) {
			drawSprite(g, bonusSymbolSprite(symbol), x, yr);
			x -= TS * 2;
		}
	}

	public void drawScore(GraphicsContext g, Score score, String title, double x, double y) {
		var font = theme.font("font.arcade", 8);
		drawText(g, title, ArcadeTheme.PALE, font, x, y);
		var pointsText = "%02d".formatted(score.points());
		drawText(g, "%7s".formatted(pointsText), ArcadeTheme.PALE, font, x, y + TS + 1);
		if (score.points() != 0) {
			drawText(g, "L%d".formatted(score.levelNumber()), ArcadeTheme.PALE, font, x + TS * 8, y + TS + 1);
		}
	}

	public void drawCredit(GraphicsContext g, int credit, double x, double y) {
		var font = theme.font("font.arcade", 8);
		drawText(g, "CREDIT %2d".formatted(credit), ArcadeTheme.PALE, font, x, y);
	}

	public abstract void drawLivesCounter(GraphicsContext g, int numLivesDisplayed);

	// TODO this is not the last word on this
	public void drawLivesCounter(GraphicsContext g, Spritesheet ss, int numLivesDisplayed) {
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
			GameRenderer.drawText(g, "+" + excessLives, ArcadeTheme.YELLOW, Font.font("Serif", FontWeight.BOLD, 8),
					x + TS * (10), y + TS * (1));
		}
	}

	public abstract AnimationMap createPacAnimations(Pac pac);

	public abstract AnimationMap createGhostAnimations(Ghost ghost);

	public abstract AnimationMap createWorldAnimations(World world);
}