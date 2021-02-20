package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.guys.GhostState.DEAD;
import static de.amr.games.pacman.model.guys.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.guys.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.guys.GhostState.LOCKED;
import static de.amr.games.pacman.world.PacManGameWorld.TS;
import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
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
import de.amr.games.pacman.model.guys.Creature;
import de.amr.games.pacman.model.guys.Ghost;
import de.amr.games.pacman.model.guys.Pac;
import de.amr.games.pacman.ui.fx.common.SceneRendering;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Rendering for the scenes of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacMan_SceneRendering implements SceneRendering {

	private final Image spritesheet = new Image("/pacman/graphics/sprites.png", false);
	private final Image mazeFull = new Image("/pacman/graphics/maze_full.png", false);
	private final Image mazeEmpty = new Image("/pacman/graphics/maze_empty.png", false);

	private final Font scoreFont;

	private final Rectangle2D[] symbolTileRegions;
	private final Map<Integer, Rectangle2D> numberTileRegions;

	private final Map<Direction, Animation<Rectangle2D>> pacMunchingAnim;
	private final Animation<Rectangle2D> pacCollapsingAnim;
	private final List<EnumMap<Direction, Animation<Rectangle2D>>> ghostsKickingAnim;
	private final EnumMap<Direction, Animation<Rectangle2D>> ghostEyesAnim;
	private final Animation<Rectangle2D> ghostBlueAnim;
	private final Animation<Rectangle2D> ghostFlashingAnim;

	private final Animation<Image> mazeFlashingAnim;
	private final Animation<Boolean> energizerBlinking;

	private int index(Direction dir) {
		return dir == RIGHT ? 0 : dir == LEFT ? 1 : dir == UP ? 2 : 3;
	}

	private Direction ensureDirection(Direction dir) {
		return dir != null ? dir : Direction.RIGHT;
	}

	private Rectangle2D tileRegion(int col, int row) {
		return new Rectangle2D(RASTER * col, RASTER * row, RASTER, RASTER);
	}

	private Rectangle2D tileRegion(int col, int row, int tilesX, int tilesY) {
		return new Rectangle2D(RASTER * col, RASTER * row, RASTER * tilesX, RASTER * tilesY);
	}

	private void drawCreatureRegion(GraphicsContext g, Creature guy, Rectangle2D region) {
		if (guy.visible && region != null) {
			g.drawImage(spritesheet, region.getMinX(), region.getMinY(), region.getWidth(), region.getHeight(),
					guy.position.x - 4, guy.position.y - 4, region.getWidth(), region.getHeight());
		}
	}

	public PacMan_SceneRendering() {

		scoreFont = Font.loadFont(getClass().getResource("/emulogic.ttf").toExternalForm(), 8);

		symbolTileRegions = new Rectangle2D[] { tileRegion(2, 3), tileRegion(3, 3), tileRegion(4, 3), tileRegion(5, 3),
				tileRegion(6, 3), tileRegion(7, 3), tileRegion(8, 3), tileRegion(9, 3) };

		//@formatter:off
		numberTileRegions = new HashMap<>();
		numberTileRegions.put(200,  tileRegion(0, 8, 1, 1));
		numberTileRegions.put(400,  tileRegion(1, 8, 1, 1));
		numberTileRegions.put(800,  tileRegion(2, 8, 1, 1));
		numberTileRegions.put(1600, tileRegion(3, 8, 1, 1));
		
		numberTileRegions.put(100,  tileRegion(0, 9, 1, 1));
		numberTileRegions.put(300,  tileRegion(1, 9, 1, 1));
		numberTileRegions.put(500,  tileRegion(2, 9, 1, 1));
		numberTileRegions.put(700,  tileRegion(3, 9, 1, 1));
		
		numberTileRegions.put(1000, tileRegion(4, 9, 2, 1)); // left-aligned 
		numberTileRegions.put(2000, tileRegion(3, 10, 3, 1));
		numberTileRegions.put(3000, tileRegion(3, 11, 3, 1));
		numberTileRegions.put(5000, tileRegion(3, 12, 3, 1));
		//@formatter:on

		// Animations

		Image mazeEmptyBright = SceneRendering.exchangeColors(mazeEmpty, Map.of(Color.rgb(33, 33, 255), Color.WHITE));
		mazeFlashingAnim = Animation.of(mazeEmptyBright, mazeEmpty).frameDuration(15);

		energizerBlinking = Animation.pulse().frameDuration(15);

		pacMunchingAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			Animation<Rectangle2D> animation = Animation.of(tileRegion(2, 0), tileRegion(1, index(dir)),
					tileRegion(0, index(dir)), tileRegion(1, index(dir)));
			animation.frameDuration(2).endless().run();
			pacMunchingAnim.put(dir, animation);
		}

		pacCollapsingAnim = Animation.of(tileRegion(3, 0), tileRegion(4, 0), tileRegion(5, 0), tileRegion(6, 0),
				tileRegion(7, 0), tileRegion(8, 0), tileRegion(9, 0), tileRegion(10, 0), tileRegion(11, 0), tileRegion(12, 0),
				tileRegion(13, 0));
		pacCollapsingAnim.frameDuration(8);

		ghostsKickingAnim = new ArrayList<>(4);
		for (int id = 0; id < 4; ++id) {
			EnumMap<Direction, Animation<Rectangle2D>> walkingTo = new EnumMap<>(Direction.class);
			for (Direction dir : Direction.values()) {
				Animation<Rectangle2D> animation = Animation.of(tileRegion(2 * index(dir), 4 + id),
						tileRegion(2 * index(dir) + 1, 4 + id));
				animation.frameDuration(10).endless();
				walkingTo.put(dir, animation);
			}
			ghostsKickingAnim.add(walkingTo);
		}

		ghostEyesAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyesAnim.put(dir, Animation.ofSingle(tileRegion(8 + index(dir), 5)));
		}

		ghostBlueAnim = Animation.of(tileRegion(8, 4), tileRegion(9, 4));
		ghostBlueAnim.frameDuration(20).endless();

		ghostFlashingAnim = Animation.of(tileRegion(8, 4), tileRegion(9, 4), tileRegion(10, 4), tileRegion(11, 4));
		ghostFlashingAnim.frameDuration(5).endless();
	}

	@Override
	public Image spritesheet() {
		return spritesheet;
	}

	@Override
	public Font getScoreFont() {
		return scoreFont;
	}

	@Override
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

	@Override
	public void drawTileCovered(GraphicsContext g, V2i tile) {
		g.setFill(Color.BLACK);
		g.fillRect(tile.x * TS, tile.y * TS, TS, TS);
	}

	@Override
	public void drawMaze(GraphicsContext g, int mazeNumber, int x, int y, boolean flashing) {
		if (flashing) {
			g.drawImage(mazeFlashing(mazeNumber).animate(), x, y);
		} else {
			g.drawImage(mazeFull, x, y);
		}
	}

	@Override
	public void drawScore(GraphicsContext g, GameModel game, boolean titleOnly) {
		g.setFont(scoreFont);
		g.translate(0, 1);
		g.setFill(Color.WHITE);
		g.fillText("SCORE", t(1), t(1));
		g.fillText("HIGH SCORE", t(15), t(1));
		g.translate(0, 1);
		if (!titleOnly) {
			g.setFill(Color.YELLOW);
			g.fillText(String.format("%08d", game.score), t(1), t(2));
			g.setFill(Color.LIGHTGRAY);
			g.fillText(String.format("L%02d", game.currentLevelNumber), t(9), t(2));
			g.setFill(Color.YELLOW);
			g.fillText(String.format("%08d", game.highscorePoints), t(15), t(2));
			g.setFill(Color.LIGHTGRAY);
			g.fillText(String.format("L%02d", game.highscoreLevel), t(23), t(2));
		}
		g.translate(0, -2);
	}

	@Override
	public void drawLevelCounter(GraphicsContext g, GameModel game, int rightX, int y) {
		int x = rightX;
		int firstLevel = Math.max(1, game.currentLevelNumber - 6);
		for (int level = firstLevel; level <= game.currentLevelNumber; ++level) {
			Rectangle2D region = symbolTileRegions[game.levelSymbols.get(level - 1)];
			g.drawImage(spritesheet, region.getMinX(), region.getMinY(), region.getWidth(), region.getHeight(), x, y,
					region.getWidth(), region.getHeight());
			x -= t(2);
		}
	}

	@Override
	public void drawLivesCounter(GraphicsContext g, GameModel game, int x, int y) {
		int maxLivesDisplayed = 5;
		int livesDisplayed = game.started ? game.lives - 1 : game.lives;
		Rectangle2D region = tileRegion(8, 1);
		for (int i = 0; i < Math.min(livesDisplayed, maxLivesDisplayed); ++i) {
			g.drawImage(spritesheet, region.getMinX(), region.getMinY(), region.getWidth(), region.getHeight(), x + t(2 * i),
					y, region.getWidth(), region.getHeight());
		}
	}

	@Override
	public void drawFoodTiles(GraphicsContext g, Stream<V2i> tiles, Predicate<V2i> eaten) {
		tiles.filter(eaten).forEach(tile -> drawTileCovered(g, tile));
	}

	@Override
	public void drawEnergizerTiles(GraphicsContext g, Stream<V2i> energizerTiles) {
		if (energizerBlinking.animate()) {
			energizerTiles.forEach(tile -> drawTileCovered(g, tile));
		}
	}

	@Override
	public void drawPac(GraphicsContext g, Pac pac) {
		drawCreatureRegion(g, pac, pacSpriteRegion(pac));
	}

	@Override
	public void drawGhost(GraphicsContext g, Ghost ghost, boolean frightened) {
		drawCreatureRegion(g, ghost, ghostSpriteRegion(ghost, frightened));
	}

	@Override
	public void drawBonus(GraphicsContext g, Bonus bonus) {
		drawCreatureRegion(g, bonus, bonusSpriteRegion(bonus));
	}

	@Override
	public Rectangle2D bonusSpriteRegion(Bonus bonus) {
		if (bonus.edibleTicksLeft > 0) {
			return symbolTileRegions[bonus.symbol];
		}
		if (bonus.eatenTicksLeft > 0) {
			return numberTileRegions.get(bonus.points);
		}
		return null;
	}

	@Override
	public Rectangle2D pacSpriteRegion(Pac pac) {
		if (pac.dead) {
			return pacDying().hasStarted() ? pacDying().animate() : pacMunchingToDir(pac, pac.dir).frame();
		}
		if (pac.speed == 0) {
			return pacMunchingToDir(pac, pac.dir).frame(0);
		}
		if (!pac.couldMove) {
			return pacMunchingToDir(pac, pac.dir).frame(1);
		}
		return pacMunchingToDir(pac, pac.dir).animate();
	}

	@Override
	public Rectangle2D ghostSpriteRegion(Ghost ghost, boolean frightened) {
		if (ghost.bounty > 0) {
			return numberTileRegions.get(ghost.bounty);
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return ghostReturningHomeToDir(ghost, ghost.dir).animate();
		}
		if (ghost.is(FRIGHTENED)) {
			return ghostFlashing().isRunning() ? ghostFlashing().frame() : ghostFrightenedToDir(ghost, ghost.dir).animate();
		}
		if (ghost.is(LOCKED) && frightened) {
			return ghostFrightenedToDir(ghost, ghost.dir).animate();
		}
		return ghostKickingToDir(ghost, ghost.wishDir).animate(); // Looks towards wish dir!
	}

	@Override
	public Animation<Rectangle2D> pacMunchingToDir(Pac pac, Direction dir) {
		return pacMunchingAnim.get(ensureDirection(dir));
	}

	@Override
	public Animation<Rectangle2D> pacDying() {
		return pacCollapsingAnim;
	}

	@Override
	public Animation<Rectangle2D> ghostKickingToDir(Ghost ghost, Direction dir) {
		return ghostsKickingAnim.get(ghost.id).get(ensureDirection(dir));
	}

	@Override
	public Animation<Rectangle2D> ghostFrightenedToDir(Ghost ghost, Direction dir) {
		return ghostBlueAnim;
	}

	@Override
	public Animation<Rectangle2D> ghostFlashing() {
		return ghostFlashingAnim;
	}

	@Override
	public Animation<Rectangle2D> ghostReturningHomeToDir(Ghost ghost, Direction dir) {
		return ghostEyesAnim.get(ensureDirection(dir));
	}

	@Override
	public Animation<Image> mazeFlashing(int mazeNumber) {
		return mazeFlashingAnim;
	}

	@Override
	public Stream<Animation<?>> mazeFlashings() {
		return Stream.of(mazeFlashingAnim);
	}

	@Override
	public Animation<Boolean> energizerBlinking() {
		return energizerBlinking;
	}
}