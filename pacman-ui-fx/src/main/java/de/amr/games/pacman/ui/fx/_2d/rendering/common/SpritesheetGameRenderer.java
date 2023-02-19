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
import static de.amr.games.pacman.ui.fx._2d.rendering.common.ArcadeTheme.SCREEN_FONT;

import java.util.List;
import java.util.Optional;

import de.amr.games.pacman.lib.anim.EntityAnimation;
import de.amr.games.pacman.lib.anim.EntityAnimationMap;
import de.amr.games.pacman.lib.anim.FixedEntityAnimation;
import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Entity;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.mspacman.MovingBonus;
import de.amr.games.pacman.ui.fx._2d.rendering.common.ArcadeTheme.Palette;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Common rendering functionality for renderers using a spritesheet.
 * 
 * @author Armin Reichert
 */
public abstract class SpritesheetGameRenderer implements Rendering2D {

	public abstract Spritesheet spritesheet();

	public void hideTileContent(GraphicsContext g, int mazeNumber, Vector2i tile) {
		g.setFill(mazeBackgroundColor(mazeNumber));
		g.fillRect(t(tile.x()), t(tile.y()), TS, TS);
	}

	@Override
	public Font screenFont(double size) {
		return size == TS ? SCREEN_FONT : Font.font(SCREEN_FONT.getFamily(), size);
	}

	@Override
	public Color ghostColor(int ghostID) {
		return ArcadeTheme.GHOST_COLORS[ghostID].normalDress();
	}

	@Override
	public Color mazeBackgroundColor(int mazeNumber) {
		return Color.BLACK;
	}

	public abstract Rectangle2D ghostRegion(int ghostID, Direction dir);

	public abstract Rectangle2D ghostValueRegion(int index);

	public abstract Image ghostValueImage(int index);

	public abstract Rectangle2D lifeSymbolRegion();

	public abstract Rectangle2D bonusSymbolRegion(int symbol);

	public abstract Rectangle2D bonusValueRegion(int symbol);

	public abstract Image bonusSymbolImage(int symbol);

	public abstract Image bonusValueImage(int symbol);

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

	public void drawSprite(GraphicsContext g, Rectangle2D r, double x, double y) {
		if (r != null) {
			g.drawImage(spritesheet().source(), r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight(), x, y, r.getWidth(),
					r.getHeight());
		}
	}

	public void drawSpriteCenteredOverBox(GraphicsContext g, Rectangle2D r, double x, double y) {
		if (r != null) {
			double dx = HTS - r.getWidth() / 2;
			double dy = HTS - r.getHeight() / 2;
			drawSprite(g, r, x + dx, y + dy);
		}
	}

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
	public void drawGhostFacingRight(GraphicsContext g, int id, int x, int y) {
		drawSpriteCenteredOverBox(g, ghostRegion(id, Direction.RIGHT), x, y);
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
	public void drawLevelCounter(GraphicsContext g, Optional<Integer> levelNumber, List<Byte> levelCounter) {
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
	public void drawScore(GraphicsContext g, int points, int levelNumber, String title, Color color, double x, double y) {
		var font = SCREEN_FONT;
		drawText(g, title, color, font, x, y);
		var pointsText = "%02d".formatted(points);
		drawText(g, "%7s".formatted(pointsText), color, font, x, y + TS + 1);
		if (points != 0) {
			drawText(g, "L" + levelNumber, color, font, x + t(8), y + TS + 1);
		}
	}

	@Override
	public void drawCredit(GraphicsContext g, int credit) {
		drawText(g, "CREDIT  %d".formatted(credit), Palette.PALE, screenFont(TS), t(2), t(36) - 1);
	}
}