package de.amr.games.pacman.ui.fx.rendering;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.pacman.PacManBonus;
import de.amr.games.pacman.ui.animation.TimedSequence;
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

	public static final GameRendering2D_MsPacMan RENDERING_MS_PACMAN = new GameRendering2D_MsPacMan();
	public static final GameRendering2D_PacMan RENDERING_PACMAN = new GameRendering2D_PacMan();

	/** Spritesheet grid cell size */
	public static final int GRID_CELLSIZE = 16;

	public final Image spritesheet;
	protected final Font scoreFont;

	protected List<Rectangle2D> symbolSprites;
	protected Map<Integer, Rectangle2D> bonusValueSprites;
	protected Map<Integer, Rectangle2D> bountyNumberSprites;
	protected List<TimedSequence<Image>> mazeFlashingAnimations;

	public GameRendering2D(String spritesheetPath) {
		spritesheet = new Image(getClass().getResource(spritesheetPath).toExternalForm());
		scoreFont = Font.loadFont(getClass().getResource("/emulogic.ttf").toExternalForm(), 8);
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

	protected Image exchangeColors(Image source, Map<Color, Color> exchanges) {
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

	/**
	 * @param bonus game bonus
	 * @return sprite for bonus depending on its state
	 */
	public Rectangle2D bonusSprite(PacManBonus bonus) {
		if (bonus.edibleTicksLeft > 0) {
			return symbolSprites.get(bonus.symbol);
		}
		if (bonus.eatenTicksLeft > 0) {
			return bonusValueSprites.get(bonus.points);
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

	public abstract void drawMaze(GraphicsContext gc, int mazeNumber, int i, int t, boolean b);

	public void drawFoodTiles(GraphicsContext g, Stream<V2i> tiles, Predicate<V2i> eaten) {
		tiles.filter(eaten).forEach(tile -> drawTileCovered(g, tile));
	}

	public void drawTileCovered(GraphicsContext g, V2i tile) {
		g.setFill(Color.BLACK);
		g.fillRect(tile.x * TS, tile.y * TS, TS, TS);
	}

	public void drawGameState(GraphicsContext g, AbstractGameModel game, PacManGameState gameState) {
		if (gameState == PacManGameState.GAME_OVER) {
			g.setFont(scoreFont);
			g.setFill(Color.RED);
			g.fillText("GAME", t(9), t(21));
			g.fillText("OVER", t(15), t(21));
		} else if (gameState == PacManGameState.READY) {
			g.setFont(scoreFont);
			g.setFill(Color.YELLOW);
			g.fillText("READY", t(11), t(21));
		}
	}

	public abstract Color getMazeWallColor(int i);

	public void drawLevelCounter(GraphicsContext g, AbstractGameModel game, int rightX, int y) {
		int x = rightX;
		int firstLevel = Math.max(1, game.currentLevelNumber - 6);
		for (int level = firstLevel; level <= game.currentLevelNumber; ++level) {
			Rectangle2D region = symbolSprites.get(game.levelSymbols.get(level - 1));
			g.drawImage(spritesheet, region.getMinX(), region.getMinY(), region.getWidth(), region.getHeight(), x, y,
					region.getWidth(), region.getHeight());
			x -= t(2);
		}
	}

	// Animations

	public TimedSequence<?> mazeFlashing(int mazeNumber) {
		return mazeFlashingAnimations.get(mazeNumber - 1);
	}

	public abstract Map<Direction, TimedSequence<Rectangle2D>> createPlayerMunchingAnimations();

	public abstract TimedSequence<Rectangle2D> createPlayerDyingAnimation();

	public abstract Map<Direction, TimedSequence<Rectangle2D>> createSpouseMunchingAnimations();

	public abstract Map<Direction, TimedSequence<Rectangle2D>> createGhostKickingAnimations(int ghostID);

	public abstract TimedSequence<Rectangle2D> createGhostFrightenedAnimation();

	public abstract TimedSequence<Rectangle2D> createGhostFlashingAnimation();

	public abstract Map<Direction, TimedSequence<Rectangle2D>> createGhostReturningHomeAnimations();

	// sprites, images

	public abstract Rectangle2D getLifeImage();

	public Map<Integer, Rectangle2D> getBountyNumberSpritesMap() {
		return bountyNumberSprites;
	}

	public Map<Integer, Rectangle2D> getBonusNumbersSpritesMap() {
		return bonusValueSprites;
	}

	public List<Rectangle2D> getSymbolSprites() {
		return symbolSprites;
	}

	public TimedSequence<Integer> createBonusAnimation() {
		return null;
	}

	public TimedSequence<Rectangle2D> createFlapAnimation() {
		return null;
	}

	public TimedSequence<Rectangle2D> createStorkFlyingAnimation() {
		return null;
	}

	public Rectangle2D getHeart() {
		return null;
	}

	public Rectangle2D getJunior() {
		return null;
	}

	public Rectangle2D getBlueBag() {
		return null;
	}

	public TimedSequence<Rectangle2D> createBlinkyStretchedAnimation() {
		return null;
	}

	public TimedSequence<Rectangle2D> createBlinkyDamagedAnimation() {
		return null;
	}
}