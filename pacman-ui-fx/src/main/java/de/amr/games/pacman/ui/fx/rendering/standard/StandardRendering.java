package de.amr.games.pacman.ui.fx.rendering.standard;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.common.GhostState.DEAD;
import static de.amr.games.pacman.model.common.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.common.GhostState.LOCKED;
import static de.amr.games.pacman.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.world.PacManGameWorld.TS;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.common.PacManGameState;
import de.amr.games.pacman.model.pacman.PacManBonus;
import de.amr.games.pacman.ui.animation.GhostAnimations;
import de.amr.games.pacman.ui.animation.MazeAnimations;
import de.amr.games.pacman.ui.animation.PlayerAnimations;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Standard implementation of scene rendering using sprites.
 * 
 * @author Armin Reichert
 */
public abstract class StandardRendering implements FXRendering, MazeAnimations, PlayerAnimations, GhostAnimations {

	/** Spritesheet grid cell size */
	public static final int GRID_CELLSIZE = 16;

	protected final Image spritesheet;
	protected final Font scoreFont;

	protected List<Rectangle2D> symbolSprites;
	protected Map<Integer, Rectangle2D> bonusValueSprites;
	protected Map<Integer, Rectangle2D> bountyValueSprites;

	protected Animation<Boolean> energizerBlinking;
	protected List<Animation<Image>> mazeFlashingAnim;

	protected Map<Direction, Animation<Rectangle2D>> pacManMunchingAnim;
	protected Animation<Rectangle2D> playerDyingAnim;

	protected List<Map<Direction, Animation<Rectangle2D>>> ghostsKickingAnim;
	protected Map<Direction, Animation<Rectangle2D>> ghostEyesAnim;
	protected Animation<Rectangle2D> ghostFrightenedAnim;
	protected Animation<Rectangle2D> ghostFlashingAnim;

	public StandardRendering(String spritesheetURL) {
		spritesheet = new Image(spritesheetURL);
		scoreFont = Font.loadFont(getClass().getResource("/emulogic.ttf").toExternalForm(), 8);
		energizerBlinking = Animation.pulse().frameDuration(15);
	}

	/**
	 * @param col grid column (x)
	 * @param row grid row (y)
	 * @return grid cell at given coordinates
	 */
	protected Rectangle2D sprite(int col, int row) {
		return gridCells(col, row, 1, 1);
	}

	/**
	 * @param col     grid column (x)
	 * @param row     grid row (y)
	 * @param numCols number of grid columns
	 * @param numRows number of grid rows
	 * @return grid cell region at given coordinates of given size
	 */
	protected Rectangle2D gridCells(int col, int row, int numCols, int numRows) {
		return gridCellsStartingAt(0, 0, col, row, numCols, numRows);
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
	protected Rectangle2D gridCellsStartingAt(int startX, int startY, int col, int row, int numCols, int numRows) {
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
			return bonusValueSprites.get(bonus.points);
		}
		return null; // should not happen
	}

	/**
	 * @param player player (Pac-Man or Ms. Pac-Man)
	 * @return sprite for player depending on its state
	 */
	public Rectangle2D playerSprite(Pac player) {
		if (player.dead) {
			return playerDying().hasStarted() ? playerDying().animate()
					: (Rectangle2D) playerMunching(player, player.dir).frame();
		}
		if (player.speed == 0) {
			return (Rectangle2D) playerMunching(player, player.dir).frame(0);
		}
		if (!player.couldMove) {
			return (Rectangle2D) playerMunching(player, player.dir).frame(1);
		}
		return (Rectangle2D) playerMunching(player, player.dir).animate();
	}

	/**
	 * @param ghost      ghost
	 * @param frightened if ghost is frightened
	 * @return sprite for ghost depending on its state
	 */
	public Rectangle2D ghostSprite(Ghost ghost, boolean frightened) {
		if (ghost.bounty > 0) {
			return bountyValueSprites.get(ghost.bounty);
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return ghostReturningHome(ghost, ghost.dir).animate();
		}
		if (ghost.is(FRIGHTENED)) {
			return ghostFlashing().isRunning() ? ghostFlashing().frame() : ghostFrightened(ghost, ghost.dir).animate();
		}
		if (ghost.is(LOCKED) && frightened) {
			return ghostFrightened(ghost, ghost.dir).animate();
		}
		// Sprite looking towards *wish* dir!
		return ghostKicking(ghost, ghost.wishDir).animate();
	}

	@Override
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

	@Override
	public void drawSprite(GraphicsContext g, Rectangle2D sprite, double x, double y) {
		g.drawImage(spritesheet, sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(), x, y,
				sprite.getWidth(), sprite.getHeight());
	}

	@Override
	public void drawLivesCounter(GraphicsContext g, GameModel game, int x, int y) {
		int maxLivesDisplayed = 5;
		int livesDisplayed = game.started ? game.lives - 1 : game.lives;
		for (int i = 0; i < Math.min(livesDisplayed, maxLivesDisplayed); ++i) {
			drawLifeCounterSymbol(g, x + t(2 * i), y);
		}
		if (game.lives > maxLivesDisplayed) {
			g.setFill(Color.YELLOW);
			g.setFont(Font.font("Sans Serif", FontWeight.BOLD, 6));
			g.fillText("+" + (game.lives - maxLivesDisplayed), x + t(10), y + t(1) - 2);
		}
	}

	@Override
	public void drawPlayer(GraphicsContext g, Pac player) {
		drawEntity(g, player, playerSprite(player));
	}

	@Override
	public void drawGhost(GraphicsContext g, Ghost ghost, boolean frightened) {
		drawEntity(g, ghost, ghostSprite(ghost, frightened));
	}

	@Override
	public void drawTileCovered(GraphicsContext g, V2i tile) {
		g.setFill(Color.BLACK);
		g.fillRect(tile.x * TS, tile.y * TS, TS, TS);
	}

	@Override
	public void drawFoodTiles(GraphicsContext g, Stream<V2i> tiles, Predicate<V2i> eaten) {
		tiles.filter(eaten).forEach(tile -> drawTileCovered(g, tile));
	}

	@Override
	public void drawEnergizerTiles(GraphicsContext g, Stream<V2i> energizerTiles) {
		if (!energizerBlinking.frame()) {
			energizerTiles.forEach(tile -> drawTileCovered(g, tile));
		}
	}

	@Override
	public void drawGameState(GraphicsContext g, GameModel game) {
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

	@Override
	public void drawScore(GraphicsContext g, GameModel game, boolean titleOnly) {
		g.setFont(getScoreFont());
		g.translate(0, 2);
		g.setFill(Color.WHITE);
		g.fillText("SCORE", t(1), t(1));
		g.fillText("HIGH SCORE", t(15), t(1));
		g.translate(0, 1);
		if (!titleOnly) {
			Color pointsColor = getMazeWallColor(game.level.mazeNumber - 1);
			if (pointsColor == Color.BLACK) {
				pointsColor = Color.YELLOW;
			}
			g.setFill(pointsColor);
			g.fillText(String.format("%08d", game.score), t(1), t(2));
			g.setFill(Color.LIGHTGRAY);
			g.fillText(String.format("L%02d", game.levelNumber), t(9), t(2));
			g.setFill(pointsColor);
			g.fillText(String.format("%08d", game.highscorePoints), t(15), t(2));
			g.setFill(Color.LIGHTGRAY);
			g.fillText(String.format("L%02d", game.highscoreLevel), t(23), t(2));
		}
		g.translate(0, -3);
	}

	@Override
	public void drawLevelCounter(GraphicsContext g, GameModel game, int rightX, int y) {
		int x = rightX;
		int firstLevel = Math.max(1, game.levelNumber - 6);
		for (int level = firstLevel; level <= game.levelNumber; ++level) {
			Rectangle2D region = symbolSprites.get(game.levelSymbols.get(level - 1));
			g.drawImage(spritesheet, region.getMinX(), region.getMinY(), region.getWidth(), region.getHeight(), x, y,
					region.getWidth(), region.getHeight());
			x -= t(2);
		}
	}

	// Animations

	@Override
	public MazeAnimations mazeAnimations() {
		return this;
	}

	@Override
	public PlayerAnimations playerAnimations() {
		return this;
	}

	@Override
	public GhostAnimations ghostAnimations() {
		return this;
	}

	@Override
	public Animation<?> mazeFlashing(int mazeNumber) {
		return mazeFlashingAnim.get(mazeNumber - 1);
	}

	@Override
	public Stream<Animation<?>> mazeFlashings() {
		return mazeFlashingAnim.stream().map(Animation.class::cast);
	}

	@Override
	public Animation<Boolean> energizerBlinking() {
		return energizerBlinking;
	}

	@Override
	public Animation<Rectangle2D> playerDying() {
		return playerDyingAnim;
	}

	@Override
	public Animation<Rectangle2D> ghostKicking(Ghost ghost, Direction dir) {
		return ghostsKickingAnim.get(ghost.id).get(ensureDirection(dir));
	}

	@Override
	public Animation<Rectangle2D> ghostFrightened(Ghost ghost, Direction dir) {
		return ghostFrightenedAnim;
	}

	@Override
	public Animation<Rectangle2D> ghostFlashing() {
		return ghostFlashingAnim;
	}

	@Override
	public Animation<Rectangle2D> ghostReturningHome(Ghost ghost, Direction dir) {
		return ghostEyesAnim.get(ensureDirection(dir));
	}
}