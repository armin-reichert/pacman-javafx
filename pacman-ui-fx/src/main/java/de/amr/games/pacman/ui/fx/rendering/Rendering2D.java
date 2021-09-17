package de.amr.games.pacman.ui.fx.rendering;

import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;

import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.pacman.Bonus;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Standard implementation of scene rendering, using a spritesheet.
 * 
 * @author Armin Reichert
 */
public abstract class Rendering2D {

	public abstract Spritesheet spritesheet();

	/**
	 * @param bonus game bonus
	 * @return sprite bounds for bonus depending on its state
	 */
	public Rectangle2D bonusSprite(Bonus bonus) {
		if (bonus.state == Bonus.EDIBLE) {
			return getSymbolSprites().get(bonus.symbol);
		}
		if (bonus.state == Bonus.EATEN) {
			return getBonusValuesSprites().get(bonus.points);
		}
		return null; // should not happen
	}

	/**
	 * @return font used in score and game state display
	 */
	public Font getScoreFont() {
		return Rendering2D_Assets.ARCADE_FONT;
	}

	/**
	 * Renders a game entity centered over its tile position
	 * 
	 * @param g      the graphics context
	 * @param entity the entity getting rendered
	 * @param sb     sprite bounds in spritsheet
	 */
	public void renderEntity(GraphicsContext g, GameEntity entity, Rectangle2D sb) {
		if (entity.isVisible() && sb != null) {
			g.drawImage(spritesheet().getSource(), sb.getMinX(), sb.getMinY(), sb.getWidth(), sb.getHeight(),
					entity.position().x - sb.getWidth() / 2 + HTS, entity.position().y - sb.getHeight() / 2 + HTS, sb.getWidth(),
					sb.getHeight());
		}
	}

	/**
	 * Renders a sprite at a given location.
	 * 
	 * @param g  the graphics context
	 * @param sb sprite bounds in spritesheet
	 * @param x  render location x
	 * @param y  render location y
	 */
	public void renderSprite(GraphicsContext g, Rectangle2D sb, double x, double y) {
		g.drawImage(spritesheet().getSource(), sb.getMinX(), sb.getMinY(), sb.getWidth(), sb.getHeight(), x, y,
				sb.getWidth(), sb.getHeight());
	}

	// Maze

	public abstract void renderMazeFull(GraphicsContext g, int mazeNumber, double x, double y);

	public abstract void renderMazeEmpty(GraphicsContext g, int mazeNumber, double x, double y);

	public abstract void renderMazeFlashing(GraphicsContext g, int mazeNumber, double x, double y);

	public abstract Color getMazeWallColor(int i);

	// Animations

	public abstract Map<Direction, TimedSequence<Rectangle2D>> createPlayerMunchingAnimations();

	public abstract TimedSequence<Rectangle2D> createPlayerDyingAnimation();

	public abstract Map<Direction, TimedSequence<Rectangle2D>> createGhostKickingAnimations(int ghostID);

	public abstract TimedSequence<Rectangle2D> createGhostFrightenedAnimation();

	public abstract TimedSequence<Rectangle2D> createGhostFlashingAnimation();

	public abstract Map<Direction, TimedSequence<Rectangle2D>> createGhostReturningHomeAnimations();

	// Sprites

	public abstract Rectangle2D getLifeSprite();

	public abstract Map<Integer, Rectangle2D> getBountyNumberSprites();

	public abstract Map<Integer, Rectangle2D> getBonusValuesSprites();

	public abstract Map<String, Rectangle2D> getSymbolSprites();
}