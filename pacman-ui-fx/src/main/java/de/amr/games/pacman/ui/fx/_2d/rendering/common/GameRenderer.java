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
package de.amr.games.pacman.ui.fx._2d.rendering.common;

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.List;

import de.amr.games.pacman.lib.anim.EntityAnimation;
import de.amr.games.pacman.lib.anim.EntityAnimationMap;
import de.amr.games.pacman.lib.anim.FixedEntityAnimation;
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
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
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
public abstract class GameRenderer implements Rendering2D {

	public static class Palette {
		public static final Color RED = Color.rgb(255, 0, 0);
		public static final Color YELLOW = Color.YELLOW;
		public static final Color PINK = Color.rgb(252, 181, 255);
		public static final Color CYAN = Color.rgb(0, 255, 255);
		public static final Color ORANGE = Color.rgb(251, 190, 88);
		public static final Color BLUE = Color.rgb(33, 33, 255);
		public static final Color PALE = Color.rgb(222, 222, 255);
		public static final Color ROSE = Color.rgb(252, 187, 179);
	}

	public static final GhostColoring[] GHOST_COLORS = new GhostColoring[4];

	static {
	//@formatter:off
		GHOST_COLORS[Ghost.ID_RED_GHOST] = new GhostColoring(//
			Palette.RED, Palette.PALE, Palette.BLUE,  // normal
			Palette.BLUE, Palette.ROSE, Palette.ROSE, // frightened
			Palette.PALE, Palette.ROSE, Palette.RED   // flashing
		);

		GHOST_COLORS[Ghost.ID_PINK_GHOST] = new GhostColoring(//
			Palette.PINK, Palette.PALE, Palette.BLUE, // normal
			Palette.BLUE, Palette.ROSE, Palette.ROSE, // frightened
			Palette.PALE, Palette.ROSE, Palette.RED   // flashing
		);

		GHOST_COLORS[Ghost.ID_CYAN_GHOST] = new GhostColoring(//
			Palette.CYAN, Palette.PALE, Palette.BLUE, // normal
			Palette.BLUE, Palette.ROSE, Palette.ROSE, // frightened
			Palette.PALE, Palette.ROSE, Palette.RED   // flashing
		);
		
		GHOST_COLORS[Ghost.ID_ORANGE_GHOST] = new GhostColoring(//
			Palette.ORANGE, Palette.PALE, Palette.BLUE, // normal
			Palette.BLUE, Palette.ROSE, Palette.ROSE, // frightened
			Palette.PALE, Palette.ROSE, Palette.RED   // flashing
		);
		//@formatter:on
	}

	private static final Font ARCADE_FONT_TS = ResourceMgr.font("fonts/emulogic.ttf", TS);

	public abstract Spritesheet spritesheet();

	public void hideTileContent(GraphicsContext g, int mazeNumber, Vector2i tile) {
		g.setFill(mazeBackgroundColor(mazeNumber));
		g.fillRect(t(tile.x()), t(tile.y()), TS, TS);
	}

	@Override
	public Font arcadeFont(double size) {
		return size == TS ? ARCADE_FONT_TS : Font.font(ARCADE_FONT_TS.getFamily(), size);
	}

	@Override
	public Color ghostColor(int ghostID) {
		return GHOST_COLORS[ghostID].normalDress();
	}

	@Override
	public Color mazeBackgroundColor(int mazeNumber) {
		return Color.BLACK;
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
		map.put(AnimKeys.GHOST_VALUE, createGhostValueSpriteList());
		map.select(AnimKeys.GHOST_COLOR);
		return map;
	}

	private EntityAnimation createGhostValueSpriteList() {
		return new FixedEntityAnimation<>(ghostValueRegion(0), ghostValueRegion(1), ghostValueRegion(2),
				ghostValueRegion(3));
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

	@Override
	public void drawPac(GraphicsContext g, Pac pac) {
		pac.animation().ifPresent(animation -> drawEntitySprite(g, pac, (Rectangle2D) animation.frame()));
	}

	@Override
	public void drawGhost(GraphicsContext g, Ghost ghost) {
		ghost.animation().ifPresent(animation -> drawEntitySprite(g, ghost, (Rectangle2D) animation.frame()));
	}

	@Override
	public void drawBonus(GraphicsContext g, Bonus bonus) {
		var sprite = switch (bonus.state()) {
		case INACTIVE -> null;
		case EDIBLE -> bonusSymbolRegion(bonus.symbol());
		case EATEN -> bonusValueRegion(bonus.symbol());
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
	public void drawLevelCounter(GraphicsContext g, List<Byte> levelCounter) {
		double x = t(24);
		for (var symbol : levelCounter) {
			drawSprite(g, bonusSymbolRegion(symbol), x, t(34));
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
			drawSprite(g, lifeSymbolRegion(), x + t(2 * i), y);
		}
		// text indicating that more lives are available than displayed
		int excessLives = numLivesDisplayed - maxLives;
		if (excessLives > 0) {
			drawText(g, "+" + excessLives, Palette.YELLOW, Font.font("Serif", FontWeight.BOLD, 8), x + t(10), y + t(1));
		}
	}

	@Override
	public void drawHUD(GraphicsContext g, GameModel game) {
		game.score().ifPresent(score -> drawScore(g, score, "SCORE", TS, TS));
		game.highScore().ifPresent(hiscore -> drawScore(g, hiscore, "HIGH SCORE", 16 * TS, TS));
	}

	@Override
	public void drawCredit(GraphicsContext g, GameModel game) {
		drawText(g, "CREDIT  %d".formatted(game.credit()), Palette.PALE, arcadeFont(TS), t(2), t(36) - 1);
	}

	private void drawScore(GraphicsContext g, Score score, String title, double x, double y) {
		drawText(g, title, Palette.PALE, ARCADE_FONT_TS, x, y);
		var pointsText = "%02d".formatted(score.points());
		drawText(g, "%7s".formatted(pointsText), Palette.PALE, ARCADE_FONT_TS, x, y + TS + 1);
		if (score.points() != 0) {
			drawText(g, "L" + score.levelNumber(), Palette.PALE, ARCADE_FONT_TS, x + t(8), y + TS + 1);
		}
	}

	@Override
	public void drawGameReadyMessage(GraphicsContext g) {
		drawText(g, "READY", Palette.YELLOW, ARCADE_FONT_TS, t(11), t(21));
		var italic = Font.font(ARCADE_FONT_TS.getFamily(), FontPosture.ITALIC, ARCADE_FONT_TS.getSize());
		drawText(g, "!", Palette.YELLOW, italic, t(16), t(21));
	}

	@Override
	public void drawGameOverMessage(GraphicsContext g) {
		drawText(g, "GAME  OVER", Palette.RED, ARCADE_FONT_TS, t(9), t(21));
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
		g.setStroke(Palette.PALE);
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