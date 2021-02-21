package de.amr.games.pacman.ui.fx.mspacman;

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
import java.util.Arrays;
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
import de.amr.games.pacman.ui.PacManGameAnimation;
import de.amr.games.pacman.ui.fx.common.Rendering;
import de.amr.games.pacman.ui.fx.common.Helper;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Rendering for the scenes of the Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class MsPacMan_Rendering implements Rendering, PacManGameAnimation {

	private final Image sheet = new Image("/mspacman/graphics/sprites.png", false);

	private final List<Rectangle2D> symbolRegions;
	private final Map<Integer, Rectangle2D> bonusValueRegions;
	private final Map<Integer, Rectangle2D> bountyValueRegions;
	private final Map<Direction, Animation<Rectangle2D>> msPacMunchingAnim;
	private final Map<Direction, Animation<Rectangle2D>> pacManMunchingAnim; // used in intermission scene
	private final Animation<Rectangle2D> msPacSpinningAnim;
	private final List<EnumMap<Direction, Animation<Rectangle2D>>> ghostsKickingAnim;
	private final EnumMap<Direction, Animation<Rectangle2D>> ghostEyesAnim;
	private final Animation<Rectangle2D> ghostBlueAnim;
	private final Animation<Rectangle2D> ghostFlashingAnim;
	private final Animation<Integer> bonusJumpAnim;
	private final List<Animation<Image>> mazesFlashingAnims;
	private final Animation<Boolean> energizerBlinking;
	private final Animation<Rectangle2D> birdAnim;

	private final Font scoreFont;

	private int index(Direction dir) {
		return dir == RIGHT ? 0 : dir == LEFT ? 1 : dir == UP ? 2 : 3;
	}

	/* Tile region relative to given origin */
	private Rectangle2D trAt(int originX, int originY, int tileX, int tileY, int tilesWidth, int tilesHeight) {
		return new Rectangle2D(originX + tileX * RASTER, originY + tileY * RASTER, tilesWidth * RASTER,
				tilesHeight * RASTER);
	}

	/* Tile region in right half of spritesheet */
	private Rectangle2D s(int tileX, int tileY) {
		return trAt(456, 0, tileX, tileY, 1, 1);
	}

	public MsPacMan_Rendering() {

		scoreFont = Font.loadFont(getClass().getResource("/emulogic.ttf").toExternalForm(), 8);

		symbolRegions = Arrays.asList(s(3, 0), s(4, 0), s(5, 0), s(6, 0), s(7, 0), s(8, 0), s(9, 0));

		//@formatter:off

		bonusValueRegions = new HashMap<>();
		bonusValueRegions.put(100,  s(3, 1));
		bonusValueRegions.put(200,  s(4, 1));
		bonusValueRegions.put(500,  s(5, 1));
		bonusValueRegions.put(700,  s(6, 1));
		bonusValueRegions.put(1000, s(7, 1));
		bonusValueRegions.put(2000, s(8, 1));
		bonusValueRegions.put(5000, s(9, 1));
		
		bountyValueRegions = new HashMap<>();
		bountyValueRegions.put(200,  s(0, 8));
		bountyValueRegions.put(400,  s(1, 8));
		bountyValueRegions.put(800,  s(2, 8));
		bountyValueRegions.put(1600, s(3, 8));
		//@formatter:on

		// Animations

		energizerBlinking = Animation.pulse().frameDuration(10);

		mazesFlashingAnims = new ArrayList<>(6);
		for (int mazeIndex = 0; mazeIndex < 6; ++mazeIndex) {
			Map<Color, Color> exchanges = Map.of(getMazeWallBorderColor(mazeIndex), Color.WHITE, getMazeWallColor(mazeIndex),
					Color.BLACK);
			WritableImage mazeEmpty = new WritableImage(226, 248);
			mazeEmpty.getPixelWriter().setPixels(0, 0, 226, 248, sheet.getPixelReader(), 226, 248 * mazeIndex);
			Image mazeEmptyBright = Helper.exchangeColors(mazeEmpty, exchanges);
			mazesFlashingAnims.add(Animation.of(mazeEmptyBright, mazeEmpty).frameDuration(15));
		}

		msPacMunchingAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = index(dir);
			Animation<Rectangle2D> munching = Animation.of(s(1, d), s(1, d), s(2, d), s(0, d));
			munching.frameDuration(2).endless();
			msPacMunchingAnim.put(dir, munching);
		}

		msPacSpinningAnim = Animation.of(s(0, 3), s(0, 0), s(0, 1), s(0, 2));
		msPacSpinningAnim.frameDuration(10).repetitions(2);

		pacManMunchingAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = index(dir);
			pacManMunchingAnim.put(dir, Animation.of(s(0, 9 + d), s(1, 9 + d), s(2, 9)).frameDuration(2).endless());
		}

		ghostsKickingAnim = new ArrayList<>(4);
		for (int ghostType = 0; ghostType < 4; ++ghostType) {
			EnumMap<Direction, Animation<Rectangle2D>> kickingTo = new EnumMap<>(Direction.class);
			for (Direction dir : Direction.values()) {
				int d = index(dir);
				Animation<Rectangle2D> kicking = Animation.of(s(2 * d, 4 + ghostType), s(2 * d + 1, 4 + ghostType));
				kicking.frameDuration(4).endless();
				kickingTo.put(dir, kicking);
			}
			ghostsKickingAnim.add(kickingTo);
		}

		ghostEyesAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyesAnim.put(dir, Animation.of(s(8 + index(dir), 5)));
		}

		ghostBlueAnim = Animation.of(s(8, 4), s(9, 4)).frameDuration(20).endless().run();
		ghostFlashingAnim = Animation.of(s(8, 4), s(9, 4), s(10, 4), s(11, 4)).frameDuration(5).endless();

		bonusJumpAnim = Animation.of(0, 2, 0, -2).frameDuration(20).endless().run();
		birdAnim = Animation.of(//
				new Rectangle2D(489, 176, 32, 16), //
				new Rectangle2D(521, 176, 32, 16));
		birdAnim.endless().frameDuration(10);
	}

	@Override
	public Image spritesheet() {
		return sheet;
	}

	@Override
	public Font getScoreFont() {
		return scoreFont;
	}

	public Map<Direction, Animation<Rectangle2D>> pacManMunching() {
		return pacManMunchingAnim;
	}

	public Rectangle2D getHeart() {
		return s(2, 10);
	}

	public Animation<Rectangle2D> getBirdAnim() {
		return birdAnim;
	}

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex
	 * @return
	 */
	public Color getMazeWallColor(int mazeIndex) {
		switch (mazeIndex) {
		case 0:
			return Color.rgb(255, 183, 174);
		case 1:
			return Color.rgb(71, 183, 255);
		case 2:
			return Color.rgb(222, 151, 81);
		case 3:
			return Color.rgb(33, 33, 255);
		case 4:
			return Color.rgb(255, 183, 255);
		case 5:
			return Color.rgb(255, 183, 174);
		default:
			return Color.WHITE;
		}
	}

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex
	 * @return
	 */
	public Color getMazeWallBorderColor(int mazeIndex) {
		switch (mazeIndex) {
		case 0:
			return Color.rgb(255, 0, 0);
		case 1:
			return Color.rgb(222, 222, 255);
		case 2:
			return Color.rgb(222, 222, 255);
		case 3:
			return Color.rgb(255, 183, 81);
		case 4:
			return Color.rgb(255, 255, 0);
		case 5:
			return Color.rgb(255, 0, 0);
		default:
			return Color.WHITE;
		}
	}

	private Direction ensureDirection(Direction dir) {
		return dir != null ? dir : Direction.RIGHT;
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
	public void drawMaze(GraphicsContext g, int mazeNumber, int x, int y, boolean flashing) {
		int index = mazeNumber - 1;
		if (flashing) {
			g.drawImage(mazeFlashing(mazeNumber).animate(), x, y);
		} else {
			Rectangle2D fullMazeRegion = new Rectangle2D(0, 248 * index, 226, 248);
			g.drawImage(sheet, fullMazeRegion.getMinX(), fullMazeRegion.getMinY(), fullMazeRegion.getWidth(),
					fullMazeRegion.getHeight(), x, y, fullMazeRegion.getWidth(), fullMazeRegion.getHeight());
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
	public void drawLevelCounter(GraphicsContext g, GameModel game, int rightX, int y) {
		int x = rightX;
		int firstLevel = Math.max(1, game.currentLevelNumber - 6);
		for (int level = firstLevel; level <= game.currentLevelNumber; ++level) {
			byte symbol = game.levelSymbols.get(level - 1);
			// TODO how can an IndexOutOfBoundsException occur here?
			Rectangle2D region = symbolRegions.get(symbol);
			g.drawImage(sheet, region.getMinX(), region.getMinY(), RASTER, RASTER, x, y, RASTER, RASTER);
			x -= t(2);
		}
	}

	@Override
	public void drawLivesCounter(GraphicsContext g, GameModel game, int x, int y) {
		int maxLivesDisplayed = 5;
		int livesDisplayed = game.started ? game.lives - 1 : game.lives;
		for (int i = 0; i < Math.min(livesDisplayed, maxLivesDisplayed); ++i) {
			g.drawImage(sheet, 456 + RASTER, 0, RASTER, RASTER, x + t(2 * i), y, RASTER, RASTER);
		}
	}

	@Override
	public void drawScore(GraphicsContext g, GameModel game, boolean titleOnly) {
		g.setFont(scoreFont);
		g.translate(0, 2);
		g.setFill(Color.WHITE);
		g.fillText("SCORE", t(1), t(1));
		g.fillText("HIGHSCORE", t(15), t(1));
		g.translate(0, 1);
		if (!titleOnly) {
			g.setFill(getMazeWallColor(game.level.mazeNumber - 1));
			g.fillText(String.format("%08d", game.score), t(1), t(2));
			g.setFill(Color.LIGHTGRAY);
			g.fillText(String.format("L%02d", game.currentLevelNumber), t(9), t(2));
			g.setFill(getMazeWallColor(game.level.mazeNumber - 1));
			g.fillText(String.format("%08d", game.highscorePoints), t(15), t(2));
			g.setFill(Color.LIGHTGRAY);
			g.fillText(String.format("L%02d", game.highscoreLevel), t(23), t(2));
		}
		g.translate(0, -3);
	}

	@Override
	public void drawTileCovered(GraphicsContext g, V2i tile) {
		g.setFill(Color.BLACK);
		g.fillRect(tile.x * TS, tile.y * TS, TS, TS);
	}

	// draw creature sprite centered over creature collision box
	private void drawCreature(GraphicsContext g, Creature guy, Rectangle2D region) {
		if (guy.visible && region != null) {
			g.drawImage(sheet, region.getMinX(), region.getMinY(), region.getWidth(), region.getHeight(),
					guy.position.x - region.getWidth() / 2 + 4, guy.position.y - region.getHeight() / 2 + 4, region.getWidth(),
					region.getHeight());
		}
	}

	@Override
	public void drawPac(GraphicsContext g, Pac pac) {
		drawCreature(g, pac, pacSpriteRegion(pac));
	}

	@Override
	public void drawGhost(GraphicsContext g, Ghost ghost, boolean frightened) {
		drawCreature(g, ghost, ghostSpriteRegion(ghost, frightened));
	}

	@Override
	public void drawBonus(GraphicsContext g, Bonus bonus) {
		g.save();
		g.translate(0, bonusJumpAnim.animate());
		drawCreature(g, bonus, bonusSpriteRegion(bonus));
		g.restore();
	}

	public void drawMrPacMan(GraphicsContext g, Pac pacMan) {
		if (pacMan.visible) {
			Animation<Rectangle2D> munching = pacManMunching().get(pacMan.dir);
			drawRegion(g, pacMan.speed > 0 ? munching.animate() : munching.frame(1), pacMan.position.x - 4,
					pacMan.position.y - 4);
		}
	}

	public void drawBirdAnim(GraphicsContext g, double x, double y) {
		birdAnim.animate();
		drawRegion(g, birdAnim.frame(), x + 4 - birdAnim.frame().getWidth() / 2, y + 4 - birdAnim.frame().getHeight() / 2);
	}

	public void drawBlueBag(GraphicsContext g, double x, double y) {
		drawRegion(g, new Rectangle2D(488, 199, 8, 8), x, y);
	}

	public void drawJunior(GraphicsContext g, double x, double y) {
		drawRegion(g, new Rectangle2D(509, 200, 8, 8), x, y);
	}

	@Override
	public Rectangle2D bonusSpriteRegion(Bonus bonus) {
		if (bonus.edibleTicksLeft > 0) {
			return symbolRegions.get(bonus.symbol);
		}
		if (bonus.eatenTicksLeft > 0) {
			return bonusValueRegions.get(bonus.points);
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
			return bountyValueRegions.get(ghost.bounty);
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
		return msPacMunchingAnim.get(ensureDirection(dir));
	}

	@Override
	public Animation<Rectangle2D> pacDying() {
		return msPacSpinningAnim;
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
		return mazesFlashingAnims.get(mazeNumber - 1);
	}

	@Override
	public Stream<Animation<?>> mazeFlashings() {
		return mazesFlashingAnims.stream().map(Animation.class::cast);
	}

	@Override
	public Animation<Boolean> energizerBlinking() {
		return energizerBlinking;
	}
}