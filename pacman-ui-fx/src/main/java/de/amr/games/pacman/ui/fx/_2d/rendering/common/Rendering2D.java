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

import java.util.List;
import java.util.function.Consumer;

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.SimpleAnimation;
import de.amr.games.pacman.lib.animation.AnimationMap;
import de.amr.games.pacman.lib.animation.SpriteArray;
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

	Image source();

	Font getArcadeFont();

	Color getGhostColor(int ghostID);

	Image getSpriteImage(Rectangle2D sprite);

	default Image[] getAnimationImages(List<Rectangle2D> animation) {
		int n = animation.size();
		Image[] images = new Image[n];
		for (int i = 0; i < n; ++i) {
			images[i] = getSpriteImage(animation.get(i));
		}
		return images;
	}

	default Image[] getAnimationImages(SpriteArray<Rectangle2D> animation) {
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

	Rectangle2D getBonusSymbolSprite(int symbol);

	Rectangle2D getBonusValueSprite(int symbol);

	// Animations

	AnimationMap<Direction, Rectangle2D> createPacMunchingAnimationMap();

	SimpleAnimation<Rectangle2D> createPacDyingAnimation();

	AnimationMap<Direction, Rectangle2D> createGhostColorAnimationMap(int ghostID);

	SimpleAnimation<Rectangle2D> createGhostBlueAnimation();

	SimpleAnimation<Rectangle2D> createGhostFlashingAnimation();

	AnimationMap<Direction, Rectangle2D> createGhostEyesAnimationMap();

	SimpleAnimation<Image> createMazeFlashingAnimation(int mazeNumber);

	SpriteArray<Rectangle2D> createGhostValueList();

	// Maze

	int mazeNumber(int levelNumber);

	Color getFoodColor(int mazeNumber);

	Image getMazeFullImage(int mazeNumber);

	Image getMazeEmptyImage(int mazeNumber);

	// Drawing

	/**
	 * Draws sprite (region) using spritesheet.
	 * 
	 * @param g      graphics context
	 * @param sprite sprite (region in spritesheet), may be null
	 * @param x      left upper corner x
	 * @param y      left upper corner y
	 */
	default void drawSprite(GraphicsContext g, Rectangle2D sprite, double x, double y) {
		if (sprite != null) {
			g.drawImage(source(), sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(), x, y,
					sprite.getWidth(), sprite.getHeight());
		}
	}

	/**
	 * Draws the sprite defined by the given spritesheet region centered of the the one square tile box with left upper
	 * corner defined by the given coordinates.
	 * 
	 * @param g      graphics context
	 * @param sprite sprite region in spritesheet, my be null
	 * @param x      left upper corner of the box (one square tile)
	 * @param y      left upper corner of the box
	 */
	default void drawSpriteCenteredOverBox(GraphicsContext g, Rectangle2D sprite, double x, double y) {
		if (sprite != null) {
			double dx = HTS - sprite.getWidth() / 2, dy = HTS - sprite.getHeight() / 2;
			drawSprite(g, sprite, x + dx, y + dy);
		}
	}

	/**
	 * Draws the entity's sprite centered over the entity's collision box. The collision box is a square with left upper
	 * corner defined by the entity position and side length of one tile size. Respects the current visibility of the
	 * entity.
	 * 
	 * @param g      graphics context
	 * @param entity entity
	 * @param s      entity sprite (region in spritesheet), may be null
	 */
	default void drawEntity(GraphicsContext g, Entity entity, Rectangle2D sprite) {
		if (entity.visible) {
			drawSpriteCenteredOverBox(g, sprite, entity.position.x, entity.position.y);
		}
	}

	default void drawPac(GraphicsContext g, Pac pac) {
		pac.animations().ifPresent(anim -> drawEntity(g, pac, (Rectangle2D) anim.current(pac)));
	}

	default void drawGhost(GraphicsContext g, Ghost ghost) {
		ghost.animations().ifPresent(anim -> {
			var sprite = (Rectangle2D) anim.current(ghost);
			drawEntity(g, ghost, sprite);
		});
	}

	default void drawGhosts(GraphicsContext g, Ghost[] ghosts) {
		for (var ghost : ghosts) {
			drawGhost(g, ghost);
		}
	}

	default void drawBonus(GraphicsContext g, Entity bonusEntity) {
		if (bonusEntity instanceof StaticBonus) {
			StaticBonus bonus = (StaticBonus) bonusEntity;
			var sprite = switch (bonus.state()) {
			case INACTIVE -> null;
			case EDIBLE -> getBonusSymbolSprite(bonus.symbol());
			case EATEN -> getBonusValueSprite(bonus.symbol());
			};
			drawEntity(g, bonus, sprite);
		} else if (bonusEntity instanceof MovingBonus) {
			MovingBonus bonus = (MovingBonus) bonusEntity;
			var sprite = switch (bonus.state()) {
			case INACTIVE -> null;
			case EDIBLE -> getBonusSymbolSprite(bonus.symbol());
			case EATEN -> getBonusValueSprite(bonus.symbol());
			};
			g.save();
			g.translate(0, bonus.dy());
			drawEntity(g, bonus, sprite);
			g.restore();
		}
	}

	/**
	 * Draws the copyright text and image. Used in several scenes so put this here.
	 * 
	 * @param g graphics context
	 * @param x left upper corner x
	 * @param y left upper corner y
	 */
	void drawCopyright(GraphicsContext g, int tileY);

	default void drawCredit(GraphicsContext g, int credit) {
		g.setFont(getArcadeFont());
		g.setFill(Color.WHITE);
		g.fillText("CREDIT  %d".formatted(credit), t(2), t(36) - 1);
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
				drawSprite(g, getBonusSymbolSprite(symbol), x, levelCounter.position.y);
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

	default void drawWorld(GraphicsContext g, World world, int mazeNumber, boolean energizersDark) {
		Consumer<V2i> hideTile = tile -> {
			g.setFill(Color.BLACK);
			g.fillRect(t(tile.x) + 0.1, t(tile.y) + 0.1, TS - 0.2, TS - 0.2);
		};
		int x = 0, y = t(3);
		g.drawImage(getMazeFullImage(mazeNumber), x, y);
		world.tiles().filter(world::containsEatenFood).forEach(hideTile::accept);
		if (energizersDark) {
			world.energizerTiles().forEach(hideTile::accept);
		}
	}

	// Debug draw functions

	default void drawGrid(GraphicsContext g) {
		g.setStroke(Color.rgb(160, 160, 160, 1));
		g.setLineWidth(0.5);
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