/*
MIT License

Copyright (c) 2022 Armin Reichert

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
package de.amr.games.pacman.ui.fx._2d.rendering;

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.lib.animation.EntityAnimation;
import de.amr.games.pacman.lib.animation.EntityAnimationSet;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.LevelCounter;
import de.amr.games.pacman.model.common.Score;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.mspacman.MovingBonus;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 * Common rendering functionality for all game variants.
 * 
 * @author Armin Reichert
 */
public abstract class RendererCommon implements Rendering2D {

	//@formatter:off
	private static final Color[] GHOST_COLORS = {
		Color.RED,
		Color.rgb(252, 181, 255), // PINK
		Color.CYAN,
		Color.rgb(253, 192, 90) // ORANGE
	};
	//@formatter:on

	private static final Font ARCADE_FONT = Ufx.font("fonts/emulogic.ttf", 8);

	protected void drawText(GraphicsContext g, String text, Color color, Font font, double x, double y) {
		g.setFont(font);
		g.setFill(color);
		g.fillText(text, x, y);
	}

	protected void clearTile(GraphicsContext g, V2i tile) {
		g.setFill(Color.BLACK);
		g.fillRect(t(tile.x()), t(tile.y()), TS, TS);
	}

	@Override
	public Font arcadeFont() {
		return ARCADE_FONT;
	}

	@Override
	public Color ghostColor(int ghostID) {
		return GHOST_COLORS[ghostID];
	}

	@Override
	public EntityAnimationSet<AnimKeys> createPacAnimationSet(Pac pac) {
		var set = new EntityAnimationSet<AnimKeys>(2);
		set.put(AnimKeys.PAC_DYING, createPacDyingAnimation());
		set.put(AnimKeys.PAC_MUNCHING, createPacMunchingAnimationMap(pac));
		set.select(AnimKeys.PAC_MUNCHING);
		return set;
	}

	@Override
	public EntityAnimationSet<AnimKeys> createGhostAnimationSet(Ghost ghost) {
		var set = new EntityAnimationSet<AnimKeys>(5);
		set.put(AnimKeys.GHOST_COLOR, createGhostColorAnimationMap(ghost));
		set.put(AnimKeys.GHOST_BLUE, createGhostBlueAnimation());
		set.put(AnimKeys.GHOST_EYES, createGhostEyesAnimationMap(ghost));
		set.put(AnimKeys.GHOST_FLASHING, createGhostFlashingAnimation());
		set.put(AnimKeys.GHOST_VALUE, createGhostValueList());
		set.select(AnimKeys.GHOST_COLOR);
		return set;
	}

	@Override
	public void drawSprite(GraphicsContext g, Rectangle2D region, double x, double y) {
		if (region != null) {
			g.drawImage(spritesheet().sourceImage(), region.getMinX(), region.getMinY(), region.getWidth(),
					region.getHeight(), x, y, region.getWidth(), region.getHeight());
		}
	}

	@Override
	public void drawSpriteCenteredOverBox(GraphicsContext g, Rectangle2D region, double x, double y) {
		if (region != null) {
			double dx = HTS - region.getWidth() / 2;
			double dy = HTS - region.getHeight() / 2;
			drawSprite(g, region, x + dx, y + dy);
		}
	}

	@Override
	public void drawEntity(GraphicsContext g, Entity entity, Rectangle2D region) {
		if (entity.isVisible()) {
			drawSpriteCenteredOverBox(g, region, entity.getPosition().x(), entity.getPosition().y());
		}
	}

	private static Rectangle2D regionOfCurrentFrame(EntityAnimation animation) {
		return (Rectangle2D) animation.frame();
	}

	@Override
	public void drawPac(GraphicsContext g, Pac pac) {
		pac.animation().ifPresent(animation -> drawEntity(g, pac, regionOfCurrentFrame(animation)));
	}

	@Override
	public void drawGhost(GraphicsContext g, Ghost ghost) {
		ghost.animation().ifPresent(animation -> drawEntity(g, ghost, regionOfCurrentFrame(animation)));
	}

	@Override
	public void drawBonus(GraphicsContext g, Bonus bonus) {
		var sprite = switch (bonus.state()) {
		case INACTIVE -> null;
		case EDIBLE -> bonusSymbolSprite(bonus.index());
		case EATEN -> bonusValueSprite(bonus.index());
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

	@Override
	public void drawLevelCounter(GraphicsContext g, LevelCounter levelCounter) {
		if (levelCounter.visible) {
			double x = levelCounter.rightBorderPosition.x();
			for (int symbol : levelCounter.symbols) {
				drawSprite(g, bonusSymbolSprite(symbol), x, levelCounter.rightBorderPosition.y());
				x -= t(2);
			}
		}
	}

	@Override
	public void drawLivesCounter(GraphicsContext g, int numLivesDisplayed) {
		if (numLivesDisplayed <= 0) {
			return;
		}
		int x = t(2);
		int y = t(ArcadeWorld.TILES_Y - 2);
		int maxLives = 5;
		for (int i = 0; i < Math.min(numLivesDisplayed, maxLives); ++i) {
			drawSprite(g, lifeSprite(), x + t(2 * i), y);
		}
		// text indicating that more lives are available than displayed
		int excessLives = numLivesDisplayed - maxLives;
		if (excessLives > 0) {
			drawText(g, "+" + excessLives, Color.YELLOW, Font.font("Serif", FontWeight.BOLD, 8), x + t(10), y + t(1));
		}
	}

	@Override
	public void drawHUD(GraphicsContext g, GameModel game, boolean creditVisible) {
		var font = arcadeFont();
		drawScore(g, game.gameScore, font, TS, TS);
		drawScore(g, game.highScore, font, 16 * TS, TS);
		if (creditVisible) {
			drawText(g, "CREDIT  %d".formatted(game.getCredit()), Color.WHITE, font, t(2), t(36) - 1);
		}
	}

	private void drawScore(GraphicsContext g, Score score, Font font, double x, double y) {
		if (score.visible) {
			drawText(g, score.title, Color.WHITE, font, x, y);
			var pointsText = score.showContent ? "%02d".formatted(score.points) : "00";
			drawText(g, "%7s".formatted(pointsText), Color.WHITE, font, x, y + TS + 1);
			if (score.showContent) {
				drawText(g, "L" + score.levelNumber, Color.LIGHTGRAY, font, x + t(8), y + TS + 1);
			}
		}
	}

	@Override
	public void drawGameStateMessage(GraphicsContext g, GameState state) {
		if (state == GameState.GAME_OVER) {
			drawText(g, "GAME  OVER", Color.RED, ARCADE_FONT, t(9), t(21));
		} else if (state == GameState.READY) {
			drawText(g, "READY", Color.YELLOW, ARCADE_FONT, t(11), t(21));
			var italic = Font.font(ARCADE_FONT.getFamily(), FontPosture.ITALIC, ARCADE_FONT.getSize());
			drawText(g, "!", Color.YELLOW, italic, t(16), t(21));
		}
	}

	public static void drawTileStructure(GraphicsContext g, int tilesX, int tilesY) {
		g.save();
		g.translate(0.5, 0.5);
		g.setStroke(Color.WHITE);
		g.setLineWidth(0.2);
		for (int row = 0; row <= tilesY; ++row) {
			g.strokeLine(0, t(row), tilesX * TS, t(row));
		}
		for (int col = 0; col <= tilesY; ++col) {
			g.strokeLine(t(col), 0, t(col), tilesY * TS);
		}
		g.restore();
	}
}