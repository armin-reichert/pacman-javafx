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

import java.util.Optional;
import java.util.function.Consumer;

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.EntityAnimation;
import de.amr.games.pacman.lib.animation.EntityAnimationByDirection;
import de.amr.games.pacman.lib.animation.EntityAnimationSet;
import de.amr.games.pacman.lib.animation.FixedEntityAnimation;
import de.amr.games.pacman.lib.animation.SingleEntityAnimation;
import de.amr.games.pacman.model.common.LevelCounter;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.actors.Score;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.model.mspacman.MovingBonus;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 * Common interface for Pac-Man and Ms. Pac-Man (spritesheet based) rendering.
 * 
 * @author Armin Reichert
 */
public interface Rendering2D {

	/**
	 * @return spritesheet source image
	 */
	Image source();

	Image getSpriteImage(Rectangle2D sprite);

	Font getArcadeFont();

	Color getGhostColor(int ghostID);

	// Sprites

	Rectangle2D getGhostSprite(int ghostID, Direction dir);

	Rectangle2D getLifeSprite();

	Rectangle2D getBonusSymbolSprite(int symbol);

	Rectangle2D getBonusValueSprite(int symbol);

	// Animations

	default EntityAnimationSet createPacAnimationSet(Pac pac) {
		EntityAnimationSet set = new EntityAnimationSet(2);
		set.put(AnimKeys.PAC_DYING, createPacDyingAnimation());
		set.put(AnimKeys.PAC_MUNCHING, createPacMunchingAnimationMap(pac));
		set.select(AnimKeys.PAC_MUNCHING);
		return set;
	}

	EntityAnimationByDirection createPacMunchingAnimationMap(Pac pac);

	SingleEntityAnimation<Rectangle2D> createPacDyingAnimation();

	default EntityAnimationSet createGhostAnimationSet(Ghost ghost) {
		EntityAnimationSet set = new EntityAnimationSet(5);
		set.put(AnimKeys.GHOST_BLUE, createGhostBlueAnimation());
		set.put(AnimKeys.GHOST_EYES, createGhostEyesAnimationMap(ghost));
		set.put(AnimKeys.GHOST_FLASHING, createGhostFlashingAnimation());
		set.put(AnimKeys.GHOST_COLOR, createGhostColorAnimationMap(ghost));
		set.put(AnimKeys.GHOST_VALUE, createGhostValueList());
		set.select(AnimKeys.GHOST_COLOR);
		return set;
	}

	EntityAnimationByDirection createGhostColorAnimationMap(Ghost ghost);

	SingleEntityAnimation<Rectangle2D> createGhostBlueAnimation();

	SingleEntityAnimation<Rectangle2D> createGhostFlashingAnimation();

	EntityAnimationByDirection createGhostEyesAnimationMap(Ghost ghost);

	SingleEntityAnimation<Image> createMazeFlashingAnimation(int mazeNumber);

	FixedEntityAnimation<Rectangle2D> createGhostValueList();

	// Maze

	Color getFoodColor(int mazeNumber);

	Image getMazeFullImage(int mazeNumber);

	// Drawing

	default void drawText(GraphicsContext g, String text, Color color, Font font, double x, double y) {
		g.setFont(font);
		g.setFill(color);
		g.fillText(text, x, y);
	}

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
			double dx = HTS - sprite.getWidth() / 2;
			double dy = HTS - sprite.getHeight() / 2;
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
		if (entity.isVisible()) {
			drawSpriteCenteredOverBox(g, sprite, entity.getPosition().x(), entity.getPosition().y());
		}
	}

	static Optional<Rectangle2D> currentFrame(Optional<EntityAnimationSet> anims) {
		return anims.map(EntityAnimationSet::selectedAnimation).map(EntityAnimation::frame).map(Rectangle2D.class::cast);
	}

	default void drawPac(GraphicsContext g, Pac pac) {
		currentFrame(pac.animationSet()).ifPresent(frame -> drawEntity(g, pac, frame));
	}

	default void drawGhost(GraphicsContext g, Ghost ghost) {
		currentFrame(ghost.animationSet()).ifPresent(frame -> drawEntity(g, ghost, frame));
	}

	default void drawGhosts(GraphicsContext g, Ghost[] ghosts) {
		for (var ghost : ghosts) {
			drawGhost(g, ghost);
		}
	}

	default void drawBonus(GraphicsContext g, Bonus bonus) {
		var sprite = switch (bonus.state()) {
		case INACTIVE -> null;
		case EDIBLE -> getBonusSymbolSprite(bonus.symbol());
		case EATEN -> getBonusValueSprite(bonus.symbol());
		};
		if (bonus.entity() instanceof MovingBonus movingBonus) {
			g.save();
			g.translate(0, movingBonus.dy());
			drawEntity(g, movingBonus, sprite);
			g.restore();
		} else {
			drawEntity(g, bonus.entity(), sprite);
		}
	}

	/**
	 * Draws the copyright text and image. Used in several scenes so put this here.
	 * 
	 * @param g     graphics context
	 * @param tileY vertical tile position
	 */
	void drawCopyright(GraphicsContext g, int tileY);

	default void drawCredit(GraphicsContext g, Font font, int credit) {
		drawText(g, "CREDIT  %d".formatted(credit), Color.WHITE, font, t(2), t(36) - 1);
	}

	default void drawScore(GraphicsContext g, Font font, Score score) {
		if (score.isVisible()) {
			var pointsText = score.showContent ? "%02d".formatted(score.points) : "00";
			var levelText = score.showContent ? "L" + score.levelNumber : "";
			drawText(g, score.title, Color.WHITE, font, score.getPosition().x(), score.getPosition().y());
			drawText(g, "%7s".formatted(pointsText), Color.WHITE, font, score.getPosition().x(),
					score.getPosition().y() + t(1));
			drawText(g, levelText, Color.LIGHTGRAY, font, score.getPosition().x() + t(8), score.getPosition().y() + t(1));
		}
	}

	default void drawGameStateMessage(GraphicsContext g, Font font, GameState state) {
		if (state == GameState.GAME_OVER) {
			drawText(g, "GAME  OVER", Color.RED, font, t(9), t(21));
		} else if (state == GameState.READY) {
			drawText(g, "READY", Color.YELLOW, font, t(11), t(21));
			g.setFont(Font.font(font.getFamily(), FontPosture.ITALIC, TS));
			g.fillText("!", t(16), t(21));
		}
	}

	default void drawLevelCounter(GraphicsContext g, LevelCounter levelCounter) {
		if (levelCounter.isVisible()) {
			double x = levelCounter.getPosition().x();
			for (int symbol : levelCounter.symbols) {
				drawSprite(g, getBonusSymbolSprite(symbol), x, levelCounter.getPosition().y());
				x -= t(2);
			}
		}
	}

	default void drawLivesCounter(GraphicsContext g, int numLivesDisplayed) {
		if (numLivesDisplayed <= 0) {
			return;
		}
		int x = t(2);
		int y = t(ArcadeWorld.TILES_Y - 2);
		int maxLives = 5;
		for (int i = 0; i < Math.min(numLivesDisplayed, maxLives); ++i) {
			drawSprite(g, getLifeSprite(), x + t(2 * i), y);
		}
		// text indicating that more lives are available than displayed
		int excessLives = numLivesDisplayed - maxLives;
		if (excessLives > 0) {
			g.setFill(Color.YELLOW);
			g.setFont(Font.font("Serif", FontWeight.BOLD, 8));
			g.fillText("+" + excessLives, x + t(10), y + t(1));
		}
	}

	default void drawMaze(GraphicsContext g, int x, int y, World world, int mazeNumber, boolean energizersDark) {
		Consumer<V2i> clearTile = tile -> {
			g.setFill(Color.BLACK);
			g.fillRect(t(tile.x()), t(tile.y()), TS, TS);
		};
		g.drawImage(getMazeFullImage(mazeNumber), x, y);
		world.tiles().filter(world::containsEatenFood).forEach(clearTile::accept);
		if (energizersDark) {
			world.energizerTiles().forEach(clearTile::accept);
		}
	}

	default void drawTileBorders(GraphicsContext g) {
		g.setStroke(Color.GRAY);
		g.setLineWidth(0.5);
		for (int row = 0; row <= ArcadeWorld.TILES_Y; ++row) {
			g.strokeLine(0, t(row), ArcadeWorld.WORLD_SIZE.x(), t(row));
		}
		for (int col = 0; col <= ArcadeWorld.TILES_X; ++col) {
			g.strokeLine(t(col), 0, t(col), ArcadeWorld.WORLD_SIZE.y());
		}
	}
}