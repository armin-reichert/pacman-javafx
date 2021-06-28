package de.amr.games.pacman.ui.fx.rendering;

import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;

import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.pacman.Bonus;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Standard implementation of scene rendering, using a spritesheet.
 * 
 * @author Armin Reichert
 */
public abstract class Rendering2D {

	protected final Spritesheet spritesheet;

	public Rendering2D(String spritesheetPath, int cellSize) {
		spritesheet = new Spritesheet(spritesheetPath, cellSize);
	}
	
	public Spritesheet getSpritesheet() {
		return spritesheet;
	}

	/**
	 * @param bonus game bonus
	 * @return sprite bounds for bonus depending on its state
	 */
	public Rectangle2D bonusSprite(Bonus bonus) {
		if (bonus.state == Bonus.EDIBLE) {
			return getSymbolSprites().get(bonus.symbol);
		}
		if (bonus.state == Bonus.EATEN) {
			return getBonusValuesSpritesMap().get(bonus.points);
		}
		return null; // should not happen
	}

	public Font getScoreFont() {
		return Rendering2D_Assets.ARCADE_FONT;
	}

	/**
	 * Draws a game entity centered over its collision box (one square tile)
	 * 
	 * @param g            the graphics context
	 * @param entity       the guy
	 * @param spriteBounds sprite (region) in spritsheet
	 */
	public void renderEntity(GraphicsContext g, GameEntity entity, Rectangle2D spriteBounds) {
		if (entity.isVisible() && spriteBounds != null) {
			g.drawImage(spritesheet.getSource(), spriteBounds.getMinX(), spriteBounds.getMinY(), spriteBounds.getWidth(),
					spriteBounds.getHeight(), entity.position().x - spriteBounds.getWidth() / 2 + HTS,
					entity.position().y - spriteBounds.getHeight() / 2 + HTS, spriteBounds.getWidth(), spriteBounds.getHeight());
		}
	}

	public void renderSprite(GraphicsContext g, Rectangle2D spriteBounds, double x, double y) {
		g.drawImage(spritesheet.getSource(), spriteBounds.getMinX(), spriteBounds.getMinY(), spriteBounds.getWidth(),
				spriteBounds.getHeight(), x, y, spriteBounds.getWidth(), spriteBounds.getHeight());
	}

	public abstract Image getMazeFullImage(int mazeNumber);

	public abstract Image getMazeEmptyImage(int mazeNumber);

	public abstract Image getMazeFlashImage(int mazeNumber);

	public abstract Color getMazeWallColor(int i);

	// Animations

	public abstract Map<Direction, TimedSequence<Rectangle2D>> createPlayerMunchingAnimations();

	public abstract TimedSequence<Rectangle2D> createPlayerDyingAnimation();

	public abstract Map<Direction, TimedSequence<Rectangle2D>> createGhostKickingAnimations(int ghostID);

	public abstract TimedSequence<Rectangle2D> createGhostFrightenedAnimation();

	public abstract TimedSequence<Rectangle2D> createGhostFlashingAnimation();

	public abstract Map<Direction, TimedSequence<Rectangle2D>> createGhostReturningHomeAnimations();

	public abstract Rectangle2D getLifeImage();

	public abstract Map<Integer, Rectangle2D> getBountyNumberSpritesMap();

	public abstract Map<Integer, Rectangle2D> getBonusValuesSpritesMap();

	public abstract Map<String, Rectangle2D> getSymbolSprites();
}