package de.amr.games.pacman.ui.fx.rendering;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;

import java.util.List;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.pacman.PacManBonus;
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
public abstract class GameRendering2D {

	/** Spritesheet grid cell size */
	public static final int GRID_CELLSIZE = 16;

	public static final GameRendering2D_MsPacMan RENDERING_MS_PACMAN = new GameRendering2D_MsPacMan();
	public static final GameRendering2D_PacMan RENDERING_PACMAN = new GameRendering2D_PacMan();

	public static GameRendering2D rendering(GameVariant gameVariant) {
		if (gameVariant == GameVariant.MS_PACMAN) {
			return RENDERING_MS_PACMAN;
		}
		if (gameVariant == GameVariant.PACMAN) {
			return RENDERING_PACMAN;
		}
		throw new IllegalArgumentException("Unknown game variant: " + gameVariant);
	}

	public static Image colorsExchanged(Image source, Map<Color, Color> exchanges) {
		WritableImage newImage = new WritableImage((int) source.getWidth(), (int) source.getHeight());
		for (int x = 0; x < source.getWidth(); ++x) {
			for (int y = 0; y < source.getHeight(); ++y) {
				Color oldColor = source.getPixelReader().getColor(x, y);
				for (Map.Entry<Color, Color> entry : exchanges.entrySet()) {
					if (oldColor.equals(entry.getKey())) {
						newImage.getPixelWriter().setColor(x, y, entry.getValue());
					}
				}
			}
		}
		return newImage;
	}

	public final Image spritesheet;
	public final Font scoreFont;
	protected List<Rectangle2D> symbolSprites;
	protected Map<Integer, Rectangle2D> bountyNumberSprites;

	public GameRendering2D(String spritesheetPath) {
		spritesheet = image(spritesheetPath);
		scoreFont = Font.loadFont(getClass().getResource("/emulogic.ttf").toExternalForm(), 8);
	}

	public Image image(String path) {
		return new Image(getClass().getResource(path).toExternalForm());
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
		return new Rectangle2D(startX + col * GRID_CELLSIZE, startY + row * GRID_CELLSIZE, numCols * GRID_CELLSIZE,
				numRows * GRID_CELLSIZE);
	}

	/**
	 * @param dir direction
	 * @return index used for this direction in spritesheet
	 */
	protected int index(Direction dir) {
		return dir == RIGHT ? 0 : dir == LEFT ? 1 : dir == UP ? 2 : 3;
	}

	/**
	 * @param dir direction or null
	 * @return direction or default value if null
	 */
	protected Direction ensureDirection(Direction dir) {
		return dir != null ? dir : Direction.RIGHT;
	}

	/**
	 * @param bonus game bonus
	 * @return sprite for bonus depending on its state
	 */
	public Rectangle2D bonusSprite(PacManBonus bonus) {
		if (bonus.edibleTicksLeft > 0) {
			return symbolSprites.get(bonus.symbol);
		}
		if (bonus.eatenTicksLeft > 0) {
			return getBonusValuesSpritesMap().get(bonus.points);
		}
		return null; // should not happen
	}

	public Font getScoreFont() {
		return scoreFont;
	}

	/**
	 * Draws a game entity centered over its collision box (of size one tile)
	 * 
	 * @param g      the graphics context
	 * @param entity the guy
	 * @param sprite sprite (region) in spritsheet
	 */
	protected void drawEntity(GraphicsContext g, GameEntity entity, Rectangle2D sprite) {
		if (entity.visible && sprite != null) {
			g.drawImage(spritesheet, sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(),
					entity.position.x - sprite.getWidth() / 2 + HTS, entity.position.y - sprite.getHeight() / 2 + HTS,
					sprite.getWidth(), sprite.getHeight());
		}
	}

	public void drawSprite(GraphicsContext g, Rectangle2D sprite, double x, double y) {
		g.drawImage(spritesheet, sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(), x, y,
				sprite.getWidth(), sprite.getHeight());
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

	public Map<Integer, Rectangle2D> getBountyNumberSpritesMap() {
		return bountyNumberSprites;
	}

	public abstract Map<Integer, Rectangle2D> getBonusValuesSpritesMap();

	public List<Rectangle2D> getSymbolSprites() {
		return symbolSprites;
	}

}