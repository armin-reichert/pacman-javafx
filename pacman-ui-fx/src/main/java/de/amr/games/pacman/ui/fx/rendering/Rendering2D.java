package de.amr.games.pacman.ui.fx.rendering;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;

import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.pacman.Bonus;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Standard implementation of scene rendering using sprites.
 * 
 * @author Armin Reichert
 */
public abstract class Rendering2D {

	protected final Image spritesheet;
	protected final int cellSize;

	public Rendering2D(String spritesheetPath, int cellSize) {
		spritesheet = Rendering2D_Assets.image(spritesheetPath);
		this.cellSize = cellSize;
	}

	public Image getSpritesheet() {
		return spritesheet;
	}

	public Image subImage(Rectangle2D r) {
		return subImage((int) r.getMinX(), (int) r.getMinY(), (int) r.getWidth(), (int) r.getHeight());
	}

	public Image subImage(int x, int y, int width, int height) {
		WritableImage subImage = new WritableImage(width, height);
		subImage.getPixelWriter().setPixels(0, 0, width, height, spritesheet.getPixelReader(), x, y);
		return subImage;
	}

	/**
	 * @param col grid column (x)
	 * @param row grid row (y)
	 * @return grid cell at given coordinates
	 */
	protected Rectangle2D sprite(int col, int row) {
		return cells(col, row, 1, 1);
	}

	/**
	 * @param col     grid column (x)
	 * @param row     grid row (y)
	 * @param numCols number of grid columns
	 * @param numRows number of grid rows
	 * @return grid cell region at given coordinates of given size
	 */
	protected Rectangle2D cells(int col, int row, int numCols, int numRows) {
		return cellsStartingAt(0, 0, col, row, numCols, numRows);
	}

	/**
	 * @param startX  absolute x-coordinate of left-upper corner of region
	 * @param startY  absolute y-coordinate of left-upper corner of region
	 * @param col     grid column (x)
	 * @param row     grid row (y)
	 * @param numCols number of grid columns
	 * @param numRows number of grid rows
	 * @return grid cell region at given coordinates of given size
	 */
	protected Rectangle2D cellsStartingAt(int startX, int startY, int col, int row, int numCols, int numRows) {
		return new Rectangle2D(startX + col * cellSize, startY + row * cellSize, numCols * cellSize, numRows * cellSize);
	}

	/**
	 * @param dir direction
	 * @return index used for this direction in the spritesheet
	 */
	protected int dirIndex(Direction dir) {
		return dir == RIGHT ? 0 : dir == LEFT ? 1 : dir == UP ? 2 : 3;
	}

	/**
	 * @param bonus game bonus
	 * @return sprite bounds for bonus depending on its state
	 */
	public Rectangle2D bonusSprite(Bonus bonus) {
		if (bonus.edibleTicksLeft > 0) {
			return getSymbolSprites().get(bonus.symbol);
		}
		if (bonus.eatenTicksLeft > 0) {
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
		if (entity.visible && spriteBounds != null) {
			g.drawImage(spritesheet, spriteBounds.getMinX(), spriteBounds.getMinY(), spriteBounds.getWidth(),
					spriteBounds.getHeight(), entity.position.x - spriteBounds.getWidth() / 2 + HTS,
					entity.position.y - spriteBounds.getHeight() / 2 + HTS, spriteBounds.getWidth(), spriteBounds.getHeight());
		}
	}

	public void renderSprite(GraphicsContext g, Rectangle2D spriteBounds, double x, double y) {
		g.drawImage(spritesheet, spriteBounds.getMinX(), spriteBounds.getMinY(), spriteBounds.getWidth(),
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