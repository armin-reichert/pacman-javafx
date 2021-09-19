package de.amr.games.pacman.ui.fx._2d.rendering;

import static de.amr.games.pacman.lib.Direction.DOWN;
import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
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
 * Base class for Pac-Man and Ms. Pac-Man spritesheet-based rendering.
 * 
 * @author Armin Reichert
 */
public abstract class Rendering2D_Common {

	public static final Font ARCADE_FONT = Font.loadFont(resource("/emulogic.ttf"), 8);

	public static InputStream resource(String path) {
		return Rendering2D_Common.class.getResourceAsStream(path);
	}

	/**
	 * @param source    source image
	 * @param exchanges map of color exchanges
	 * @return copy of source image with colors exchanged as given
	 */
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

	protected final Image spritesheet;
	protected final int rasterSize;
	protected List<Direction> directionOrder = Arrays.asList(RIGHT, LEFT, UP, DOWN);

	public Rendering2D_Common(String spritesheetPath, int rasterSize) {
		spritesheet = new Image(resource(spritesheetPath));
		this.rasterSize = rasterSize;
	}

	public Image getSpritesheet() {
		return spritesheet;
	}

	public int getRasterSize() {
		return rasterSize;
	}

	/**
	 * @param dir direction
	 * @return index used for this direction in the spritesheet
	 */
	public int dirIndex(Direction dir) {
		return directionOrder.indexOf(dir);
	}

	public Image createSubImage(Rectangle2D region) {
		return createSubImage((int) region.getMinX(), (int) region.getMinY(), (int) region.getWidth(),
				(int) region.getHeight());
	}

	public Image createSubImage(int x, int y, int width, int height) {
		WritableImage subImage = new WritableImage(width, height);
		subImage.getPixelWriter().setPixels(0, 0, width, height, spritesheet.getPixelReader(), x, y);
		return subImage;
	}

	/**
	 * @param col grid column (x)
	 * @param row grid row (y)
	 * @return region at given coordinates
	 */
	public Rectangle2D region(int col, int row) {
		return region(col, row, 1, 1);
	}

	/**
	 * @param col     grid column (x)
	 * @param row     grid row (y)
	 * @param numCols number of grid columns
	 * @param numRows number of grid rows
	 * @return region at given grid coordinates
	 */
	public Rectangle2D region(int col, int row, int numCols, int numRows) {
		return region(0, 0, col, row, numCols, numRows);
	}

	/**
	 * @param originX origin x-coordinate
	 * @param originY origin y-coordinate
	 * @param col     grid column (x)
	 * @param row     grid row (y)
	 * @param numCols number of grid columns
	 * @param numRows number of grid rows
	 * @return region at given grid coordinates relative to given origin
	 */
	public Rectangle2D region(int originX, int originY, int col, int row, int numCols, int numRows) {
		return new Rectangle2D(originX + col * rasterSize, originY + row * rasterSize, numCols * rasterSize,
				numRows * rasterSize);
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
		g.drawImage(spritesheet, region.getMinX(), region.getMinY(), region.getWidth(), region.getHeight(), x, y,
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