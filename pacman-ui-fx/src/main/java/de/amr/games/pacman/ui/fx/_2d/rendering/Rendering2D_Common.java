package de.amr.games.pacman.ui.fx._2d.rendering;

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
 * Base class for Pac-Man and Ms. Pac-Man spritesheet-based rendering.
 * 
 * @author Armin Reichert
 */
public abstract class Rendering2D_Common extends Spritesheet {

	public static final Font ARCADE_FONT = Font.loadFont(resource("/emulogic.ttf"), 8);

	public Rendering2D_Common(String path, int rasterSize) {
		super(path, rasterSize);
	}

	/**
	 * @param ghostID 0=Blinky, 1=Pinky, 2=Inky, 3=Clyde/Sue
	 * @return color of ghost
	 */
	public Color getGhostColor(int ghostID) {
		return ghostID == 0 ? Color.RED : ghostID == 1 ? Color.PINK : ghostID == 2 ? Color.CYAN : Color.ORANGE;
	}

	/**
	 * @param bonus game bonus
	 * @return sprite (region) for bonus symbol depending on its state (edible/eaten)
	 */
	public Rectangle2D bonusSprite(Bonus bonus) {
		if (bonus.state == Bonus.EDIBLE) {
			return getSymbolSprites().get(bonus.symbol);
		}
		if (bonus.state == Bonus.EATEN) {
			return getBonusValuesSprites().get(bonus.points);
		}
		throw new IllegalStateException();
	}

	/**
	 * @return font used in score and game state display
	 */
	public Font getScoreFont() {
		return ARCADE_FONT;
	}

	/**
	 * Renders a game entity centered over its tile position
	 * 
	 * @param g      the graphics context
	 * @param entity the entity getting rendered
	 * @param region region of entity sprite in spritesheet
	 */
	public void renderEntity(GraphicsContext g, GameEntity entity, Rectangle2D region) {
		if (entity.isVisible()) {
			// draw sprite centered over entity bounding box
			renderSprite(g, region, entity.position().x - region.getWidth() / 2 + HTS,
					entity.position().y - region.getHeight() / 2 + HTS);
		}
	}

	/**
	 * Renders a sprite at a given location.
	 * 
	 * @param g      the graphics context
	 * @param region sprite region in spritesheet
	 * @param x      render location x
	 * @param y      render location y
	 */
	public void renderSprite(GraphicsContext g, Rectangle2D region, double x, double y) {
		g.drawImage(getImage(), region.getMinX(), region.getMinY(), region.getWidth(), region.getHeight(), x, y,
				region.getWidth(), region.getHeight());
	}

	// Maze

	/**
	 * @param mazeNumber the 1-based maze number
	 * @return color of maze walls on top
	 */
	public abstract Color getMazeTopColor(int mazeNumber);

	/**
	 * @param mazeNumber the 1-based maze number
	 * @return color of maze walls on side
	 */
	public abstract Color getMazeSideColor(int mazeNumber);

	/**
	 * @param gameVariant Pac-Man vs. Ms. Pac-Man
	 * @param mazeNumber  the 1-based maze number
	 * @return color of pellets in this maze
	 */
	public abstract Color getFoodColor(int mazeNumber);

	public abstract void renderMazeFull(GraphicsContext g, int mazeNumber, double x, double y);

	public abstract void renderMazeEmpty(GraphicsContext g, int mazeNumber, double x, double y);

	public abstract void renderMazeFlashing(GraphicsContext g, int mazeNumber, double x, double y);

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