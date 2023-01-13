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

import de.amr.games.pacman.lib.anim.EntityAnimation;
import de.amr.games.pacman.lib.anim.EntityAnimationMap;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.GameModel;
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

	public static final GhostColorScheme[] GHOST_COLOR_SCHEMES = new GhostColorScheme[4];

	static {
		var red = Color.RED;
		var pink = Color.web("FCB5FF");
		var cyan = Color.CYAN;
		var orange = Color.web("FBBE58");
		var blue = Color.web("#2121FF");
		var pale = Color.web("E0DDFF");
		var rose = Color.web("FCBBB3");

		//@formatter:off
		GHOST_COLOR_SCHEMES[Ghost.ID_RED_GHOST] = new GhostColorScheme(//
			red, pale, blue,  // normal
			blue, rose, rose, // frightened
			pale, rose, red   // flashing
		);

		GHOST_COLOR_SCHEMES[Ghost.ID_PINK_GHOST] = new GhostColorScheme(//
			pink, pale, blue, // normal
			blue, rose, rose, // frightened
			pale, rose, red   // flashing
		);

		GHOST_COLOR_SCHEMES[Ghost.ID_CYAN_GHOST] = new GhostColorScheme(//
			cyan, pale, blue, // normal
			blue, rose, rose, // frightened
			pale, rose, red   // flashing
		);
		
		GHOST_COLOR_SCHEMES[Ghost.ID_ORANGE_GHOST] = new GhostColorScheme(//
			orange, pale, blue, // normal
			blue, rose, rose,   // frightened
			pale, rose, red     // flashing
		);
		//@formatter:on
	}

	private static final Font ARCADE_FONT_TS = Ufx.font("fonts/emulogic.ttf", TS);

	public void hideTileContent(GraphicsContext g, Vector2i tile) {
		g.setFill(Color.BLACK);
		g.fillRect(t(tile.x()), t(tile.y()), TS, TS);
	}

	@Override
	public Font arcadeFont(double size) {
		return size == TS ? ARCADE_FONT_TS : Font.font(ARCADE_FONT_TS.getFamily(), size);
	}

	@Override
	public Color ghostColor(int ghostID) {
		return GHOST_COLOR_SCHEMES[ghostID].normalDress();
	}

	@Override
	public EntityAnimationMap<AnimKeys> createPacAnimations(Pac pac) {
		var map = new EntityAnimationMap<AnimKeys>(2);
		map.put(AnimKeys.PAC_DYING, createPacDyingAnimation());
		map.put(AnimKeys.PAC_MUNCHING, createPacMunchingAnimation(pac));
		map.select(AnimKeys.PAC_MUNCHING);
		return map;
	}

	@Override
	public EntityAnimationMap<AnimKeys> createGhostAnimations(Ghost ghost) {
		var map = new EntityAnimationMap<AnimKeys>(5);
		map.put(AnimKeys.GHOST_COLOR, createGhostColorAnimation(ghost));
		map.put(AnimKeys.GHOST_BLUE, createGhostBlueAnimation());
		map.put(AnimKeys.GHOST_EYES, createGhostEyesAnimation(ghost));
		map.put(AnimKeys.GHOST_FLASHING, createGhostFlashingAnimation());
		map.put(AnimKeys.GHOST_VALUE, createGhostValueList());
		map.select(AnimKeys.GHOST_COLOR);
		return map;
	}

	@Override
	public void drawSprite(GraphicsContext g, Rectangle2D r, double x, double y) {
		if (r != null) {
			g.drawImage(spritesheet().source(), r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight(), x, y, r.getWidth(),
					r.getHeight());
		}
	}

	@Override
	public void drawSpriteCenteredOverBox(GraphicsContext g, Rectangle2D r, double x, double y) {
		if (r != null) {
			double dx = HTS - r.getWidth() / 2;
			double dy = HTS - r.getHeight() / 2;
			drawSprite(g, r, x + dx, y + dy);
		}
	}

	@Override
	public void drawEntitySprite(GraphicsContext g, Entity entity, Rectangle2D r) {
		if (entity.isVisible()) {
			drawSpriteCenteredOverBox(g, r, entity.position().x(), entity.position().y());
		}
	}

	private static Rectangle2D currentSprite(EntityAnimation animation) {
		return (Rectangle2D) animation.frame();
	}

	@Override
	public void drawPac(GraphicsContext g, Pac pac) {
		pac.animation().ifPresent(animation -> drawEntitySprite(g, pac, currentSprite(animation)));
	}

	@Override
	public void drawGhost(GraphicsContext g, Ghost ghost) {
		ghost.animation().ifPresent(animation -> drawEntitySprite(g, ghost, currentSprite(animation)));
	}

	@Override
	public void drawBonus(GraphicsContext g, Bonus bonus) {
		var sprite = switch (bonus.state()) {
		case INACTIVE -> null;
		case EDIBLE -> bonusSymbolSprite(bonus.symbol());
		case EATEN -> bonusValueSprite(bonus.symbol());
		};
		if (bonus.entity() instanceof MovingBonus movingBonus) {
			g.save();
			g.translate(0, movingBonus.dy());
			drawEntitySprite(g, movingBonus, sprite);
			g.restore();
		} else {
			drawEntitySprite(g, bonus.entity(), sprite);
		}
	}

	@Override
	public void drawLevelCounter(GraphicsContext g, Iterable<Byte> levelCounter) {
		double x = t(24);
		for (var symbol : levelCounter) {
			drawSprite(g, bonusSymbolSprite(symbol), x, t(34));
			x -= t(2);
		}
	}

	@Override
	public void drawLivesCounter(GraphicsContext g, int numLivesDisplayed) {
		if (numLivesDisplayed <= 0) {
			return;
		}
		int x = t(2);
		int y = t(ArcadeWorld.SIZE_TILES.y() - 2);
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
		game.score().ifPresent(score -> drawScore(g, score, "SCORE", TS, TS));
		game.highScore().ifPresent(hiscore -> drawScore(g, hiscore, "HIGH SCORE", 16 * TS, TS));
		if (creditVisible) {
			drawText(g, "CREDIT  %d".formatted(game.credit()), Color.rgb(222, 222, 255), arcadeFont(TS), t(2), t(36) - 1);
		}
	}

	private void drawScore(GraphicsContext g, Score score, String title, double x, double y) {
		var font = arcadeFont(TS);
		var color = Color.rgb(222, 222, 255);
		drawText(g, title, color, font, x, y);
		var pointsText = "%02d".formatted(score.points());
		drawText(g, "%7s".formatted(pointsText), color, font, x, y + TS + 1);
		if (score.points() != 0) {
			drawText(g, "L" + score.levelNumber(), color, font, x + t(8), y + TS + 1);
		}
	}

	@Override
	public void drawGameReadyMessage(GraphicsContext g) {
		drawText(g, "READY", Color.YELLOW, ARCADE_FONT_TS, t(11), t(21));
		var italic = Font.font(ARCADE_FONT_TS.getFamily(), FontPosture.ITALIC, ARCADE_FONT_TS.getSize());
		drawText(g, "!", Color.YELLOW, italic, t(16), t(21));
	}

	@Override
	public void drawGameOverMessage(GraphicsContext g) {
		drawText(g, "GAME  OVER", Color.RED, ARCADE_FONT_TS, t(9), t(21));
	}

	@Override
	public void drawText(GraphicsContext g, String text, Color color, Font font, double x, double y) {
		g.setFont(font);
		g.setFill(color);
		g.fillText(text, x, y);
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