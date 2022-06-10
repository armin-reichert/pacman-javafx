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
package de.amr.games.pacman.ui.fx._2d.rendering.common;

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.GenericAnimationMap;
import de.amr.games.pacman.lib.animation.SingleGenericAnimation;
import de.amr.games.pacman.lib.animation.StaticGenericAnimation;
import de.amr.games.pacman.model.common.LevelCounter;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.actors.Score;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.model.mspacman.MovingBonus;
import de.amr.games.pacman.model.pacman.StaticBonus;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Common interface for Pac-Man and Ms. Pac-Man (spritesheet based) rendering.
 * 
 * @author Armin Reichert
 */
public interface Rendering2D {

	public enum Mouth {
		CLOSED, OPEN, WIDE_OPEN
	}

	Font getArcadeFont();

	Color getGhostColor(int ghostID);

	Image getSpriteImage(Rectangle2D sprite);

	default Image[] getAnimationImages(StaticGenericAnimation<Rectangle2D> animation) {
		int n = animation.numFrames();
		Image[] images = new Image[n];
		for (int i = 0; i < n; ++i) {
			images[i] = getSpriteImage(animation.frame(i));
		}
		return images;
	}

	// Sprites

	Rectangle2D getGhostSprite(int ghostID, Direction dir);

	Rectangle2D getPacSprite(Direction dir, Mouth mouth);

	Rectangle2D getLifeSprite();

	Rectangle2D getSymbolSprite(int symbol);

	// Animations

	GenericAnimationMap<Direction, Rectangle2D> createPacMunchingAnimation();

	SingleGenericAnimation<Rectangle2D> createPacDyingAnimation();

	GenericAnimationMap<Direction, Rectangle2D> createGhostColorAnimation(int ghostID);

	SingleGenericAnimation<Rectangle2D> createGhostBlueAnimation();

	SingleGenericAnimation<Rectangle2D> createGhostFlashingAnimation();

	GenericAnimationMap<Direction, Rectangle2D> createGhostEyesAnimation();

	SingleGenericAnimation<Image> createMazeFlashingAnimation(int mazeNumber);

	StaticGenericAnimation<Rectangle2D> createBonusSymbolAnimation();

	StaticGenericAnimation<Rectangle2D> createBonusValueAnimation();

	StaticGenericAnimation<Rectangle2D> createGhostValueAnimation();

	// Maze

	int mazeNumber(int levelNumber);

	Color getFoodColor(int mazeNumber);

	Image getMazeFullImage(int mazeNumber);

	Image getMazeEmptyImage(int mazeNumber);

	// Drawing

	default void drawPac(GraphicsContext g, Pac pac) {
		pac.animations().ifPresent(anim -> {
			drawEntity(g, pac, (Rectangle2D) anim.currentSprite(pac));
		});
	}

	default void drawGhost(GraphicsContext g, Ghost ghost) {
		ghost.animations().ifPresent(anim -> {
			drawEntity(g, ghost, (Rectangle2D) anim.currentSprite(ghost));
		});
	}

	default void drawMovingBonus(GraphicsContext g, MovingBonus bonus) {
		bonus.animations().ifPresent(anim -> {
			int dy = bonus.dy();
			g.save();
			g.translate(0, dy);
			drawEntity(g, bonus, (Rectangle2D) anim.currentSprite(bonus));
			g.restore();
		});
	}

	default void drawStaticBonus(GraphicsContext g, StaticBonus bonus) {
		bonus.animations().ifPresent(anim -> {
			drawEntity(g, bonus, (Rectangle2D) anim.currentSprite(bonus));
		});
	}

	/**
	 * Draws the entity's sprite centered over the entity's collision box. The collision box is a square with left upper
	 * corner defined by the entity position and side length of one tile size. Respects the current visibility of the
	 * entity.
	 * 
	 * @param g      graphics context
	 * @param entity entity
	 * @param s      entity sprite (region in spritesheet)
	 */
	default void drawEntity(GraphicsContext g, Entity entity, Rectangle2D sprite) {
		if (entity.visible && sprite != null) {
			drawSpriteCenteredOverBox(g, sprite, entity.position.x, entity.position.y);
		}
	}

	/**
	 * Draws the sprite defined by the given spritesheet region centered of the the one square tile box with left upper
	 * corner defined by the given coordinates.
	 * 
	 * @param g graphics context
	 * @param s sprite region in spritesheet
	 * @param x left upper corner of the box (one square tile)
	 * @param y left upper corner of the box
	 */
	default void drawSpriteCenteredOverBox(GraphicsContext g, Rectangle2D s, double x, double y) {
		double dx = HTS - s.getWidth() / 2, dy = HTS - s.getHeight() / 2;
		drawSprite(g, s, x + dx, y + dy);
	}

	/**
	 * Draws sprite (region) using spritesheet.
	 * 
	 * @param g graphics context
	 * @param s sprite (region in spritesheet)
	 * @param x left upper corner x
	 * @param y left upper corner y
	 */
	void drawSprite(GraphicsContext g, Rectangle2D s, double x, double y);

	/**
	 * Draws the copyright text and image. Used in several scenes so put this here.
	 * 
	 * @param g graphics context
	 * @param x left upper corner x
	 * @param y left upper corner y
	 */
	void drawCopyright(GraphicsContext g, int tileY);

	default void drawCredit(GraphicsContext g, int credit, boolean visible) {
		if (visible) {
			g.setFont(getArcadeFont());
			g.setFill(Color.WHITE);
			g.fillText("CREDIT  %d".formatted(credit), t(2), t(36) - 1);
		}
	}

	default void drawScore(GraphicsContext g, Score score) {
		if (score.visible) {
			String pointsText = score.showContent ? "%02d".formatted(score.points) : "00";
			String levelText = score.showContent ? "L" + score.levelNumber : "";
			g.setFont(getArcadeFont());
			g.setFill(Color.WHITE);
			g.fillText(score.title, score.position.x, score.position.y);
			g.setFill(Color.WHITE);
			g.fillText("%7s".formatted(pointsText), score.position.x, score.position.y + t(1));
			g.setFill(Color.LIGHTGRAY);
			g.fillText(levelText, score.position.x + t(8), score.position.y + t(1));
		}
	}

	default void drawGameStateMessage(GraphicsContext g, GameState state) {
		if (state == GameState.GAME_OVER) {
			g.setFont(getArcadeFont());
			g.setFill(Color.RED);
			g.fillText("GAME", t(9), t(21));
			g.fillText("OVER", t(15), t(21));
		} else if (state == GameState.READY) {
			g.setFont(getArcadeFont());
			g.setFill(Color.YELLOW);
			g.fillText("READY!", t(11), t(21));
		}
	}

	default void drawLevelCounter(GraphicsContext g, LevelCounter levelCounter) {
		if (levelCounter.visible) {
			double x = levelCounter.position.x;
			for (int symbol : levelCounter.symbols) {
				var sprite = getSymbolSprite(symbol);
				drawSprite(g, sprite, x, levelCounter.position.y);
				x -= t(2);
			}
		}
	}

	default void drawLivesCounter(GraphicsContext g, int numLives) {
		int x = t(2), y = t(34);
		int maxLives = 5;
		for (int i = 0; i < Math.min(numLives, maxLives); ++i) {
			drawSprite(g, getLifeSprite(), x + t(2 * i), y);
		}
		// text indicating that more lives are available than displayed
		int excessLives = numLives - maxLives;
		if (excessLives > 0) {
			g.setFill(Color.YELLOW);
			g.setFont(Font.font("Serif", FontWeight.BOLD, 8));
			g.fillText("+" + excessLives, x + t(10), y + t(1));
		}
	}

	default void drawWorld(GraphicsContext g, World world, int mazeNumber, boolean foodHidden) {
		int x = 0, y = t(3);
		g.drawImage(getMazeFullImage(mazeNumber), x, y);
		world.tiles().filter(world::containsEatenFood).forEach(tile -> clearTile(g, tile));
		if (foodHidden) { // dark blinking phase
			world.energizerTiles().forEach(tile -> clearTile(g, tile));
		}
	}

	static void clearTile(GraphicsContext g, V2i tile) {
		g.setFill(Color.BLACK);
		g.fillRect(t(tile.x) + 0.2, t(tile.y) + 0.2, TS - 0.2, TS - 0.2);
	}

	// Debug draw functions

	default void drawTileBorders(GraphicsContext g, Stream<V2i> tiles, Color color) {
		tiles.forEach(tile -> drawTileBorder(g, tile, color));
	}

	default void drawTileBorder(GraphicsContext g, V2i tile, Color color) {
		g.setStroke(color);
		g.strokeRect(t(tile.x) + 0.2, t(tile.y) + 0.2, TS - 0.2, TS - 0.2);
	}

	default void drawGrid(GraphicsContext g) {
		g.setStroke(Color.rgb(160, 160, 160, 0.5));
		g.setLineWidth(1);
		for (int row = 0; row < ArcadeWorld.TILES_Y; ++row) {
			line(g, 0, t(row), ArcadeWorld.SIZE.x, t(row));
		}
		for (int col = 0; col < ArcadeWorld.TILES_X; ++col) {
			line(g, t(col), 0, t(col), ArcadeWorld.SIZE.y);
		}
	}

	// WTF: why are lines blurred without this?
	static void line(GraphicsContext g, double x1, double y1, double x2, double y2) {
		double offset = 0.5;
		g.strokeLine(x1 + offset, y1 + offset, x2 + offset, y2 + offset);
	}
}