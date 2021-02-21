package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.guys.Bonus;
import de.amr.games.pacman.model.guys.Ghost;
import de.amr.games.pacman.model.guys.Pac;
import de.amr.games.pacman.ui.PacManGameAnimation;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Common interface for the scene renderings.
 * 
 * @author Armin Reichert
 */
public abstract class SpritesheetBasedRendering implements PacManGameAnimation {

	/** Spritesheet raster size */
	public static final int RASTER = 16;

	public static Rectangle2D tileRegion(int tileX, int tileY) {
		return tileRegion(tileX, tileY, 1, 1);
	}

	public static Rectangle2D tileRegion(int tileX, int tileY, int cols, int rows) {
		return new Rectangle2D(tileX * RASTER, tileY * RASTER, cols * RASTER, rows * RASTER);
	}

	protected final Image spritesheet;
	protected final Font scoreFont;
	protected final Animation<Boolean> energizerBlinking;
	protected List<Rectangle2D> symbolRegions;
	protected Map<Integer, Rectangle2D> bonusValueRegions;
	protected Map<Integer, Rectangle2D> bountyValueRegions;
	protected Map<Direction, Animation<Rectangle2D>> pacManMunchingAnim;
	protected List<EnumMap<Direction, Animation<Rectangle2D>>> ghostsKickingAnim;
	protected EnumMap<Direction, Animation<Rectangle2D>> ghostEyesAnim;
	protected Animation<Rectangle2D> ghostBlueAnim;
	protected Animation<Rectangle2D> ghostFlashingAnim;

	public SpritesheetBasedRendering(Image spritesheet) {
		this.spritesheet = spritesheet;
		scoreFont = Font.loadFont(getClass().getResource("/emulogic.ttf").toExternalForm(), 8);
		energizerBlinking = Animation.pulse().frameDuration(15);
	}

	/* Tile region relative to given origin */
	public Rectangle2D tileRegionAt(int originX, int originY, int tileX, int tileY, int tilesWidth, int tilesHeight) {
		return new Rectangle2D(originX + tileX * RASTER, originY + tileY * RASTER, tilesWidth * RASTER,
				tilesHeight * RASTER);
	}

	public void drawRegion(GraphicsContext g, Rectangle2D region, double x, double y) {
		g.drawImage(spritesheet, region.getMinX(), region.getMinY(), region.getWidth(), region.getHeight(), x, y,
				region.getWidth(), region.getHeight());
	}

	protected int index(Direction dir) {
		return dir == RIGHT ? 0 : dir == LEFT ? 1 : dir == UP ? 2 : 3;
	}

	protected Direction ensureDirection(Direction dir) {
		return dir != null ? dir : Direction.RIGHT;
	}

	public abstract Rectangle2D bonusSpriteRegion(Bonus bonus);

	public abstract Rectangle2D pacSpriteRegion(Pac pac);

	public abstract Rectangle2D ghostSpriteRegion(Ghost ghost, boolean frightened);

	public Font getScoreFont() {
		return scoreFont;
	}

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex
	 * @return
	 */
	public abstract Color getMazeWallBorderColor(int mazeIndex);

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex
	 * @return
	 */
	public abstract Color getMazeWallColor(int mazeIndex);

	public abstract void drawPac(GraphicsContext g, Pac pac);

	public abstract void drawGhost(GraphicsContext g, Ghost ghost, boolean frightened);

	public abstract void drawBonus(GraphicsContext g, Bonus bonus);

	public abstract void drawTileCovered(GraphicsContext g, V2i tile);

	public abstract void drawMaze(GraphicsContext g, int mazeNumber, int x, int y, boolean flashing);

	public abstract void drawFoodTiles(GraphicsContext g, Stream<V2i> tiles, Predicate<V2i> eaten);

	public abstract void drawEnergizerTiles(GraphicsContext g, Stream<V2i> energizerTiles);

	public void signalGameState(GraphicsContext g, GameModel game) {
		if (game.state == PacManGameState.GAME_OVER || game.attractMode) {
			g.setFont(scoreFont);
			g.setFill(Color.RED);
			g.fillText("GAME", t(9), t(21));
			g.fillText("OVER", t(15), t(21));
		} else if (game.state == PacManGameState.READY) {
			g.setFont(scoreFont);
			g.setFill(Color.YELLOW);
			g.fillText("READY", t(11), t(21));
		}
	}

	public void drawScore(GraphicsContext g, GameModel game, boolean titleOnly) {
		g.setFont(getScoreFont());
		g.translate(0, 2);
		g.setFill(Color.WHITE);
		g.fillText("SCORE", t(1), t(1));
		g.fillText("HIGHSCORE", t(15), t(1));
		g.translate(0, 1);
		if (!titleOnly) {
			Color pointsColor = getMazeWallColor(game.level.mazeNumber - 1);
			if (pointsColor == Color.BLACK) {
				pointsColor = Color.YELLOW;
			}
			g.setFill(pointsColor);
			g.fillText(String.format("%08d", game.score), t(1), t(2));
			g.setFill(Color.LIGHTGRAY);
			g.fillText(String.format("L%02d", game.currentLevelNumber), t(9), t(2));
			g.setFill(pointsColor);
			g.fillText(String.format("%08d", game.highscorePoints), t(15), t(2));
			g.setFill(Color.LIGHTGRAY);
			g.fillText(String.format("L%02d", game.highscoreLevel), t(23), t(2));
		}
		g.translate(0, -3);
	}

	public abstract void drawLivesCounter(GraphicsContext g, GameModel game, int x, int y);

	public abstract void drawLevelCounter(GraphicsContext g, GameModel game, int x, int y);
}