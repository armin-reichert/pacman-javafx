package de.amr.games.pacman.ui.fx.rendering;

import static de.amr.games.pacman.lib.Direction.LEFT;
import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Direction.UP;
import static de.amr.games.pacman.model.GhostState.DEAD;
import static de.amr.games.pacman.model.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.GhostState.LOCKED;
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
import de.amr.games.pacman.model.Bonus;
import de.amr.games.pacman.model.Creature;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.PacManGameAnimations;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class MsPacManGameRendering implements RenderingWithAnimatedSprites, PacManGameAnimations {

	private final GraphicsContext g;
	private final Image spritesheet = new Image("/mspacman/graphics/sprites.png", false);

	private final Rectangle2D[] symbols;
	private final Map<Integer, Rectangle2D> bonusValues;
	private final Map<Integer, Rectangle2D> bountyValues;
	private final Map<Direction, Animation<Rectangle2D>> pacMunching;
	private final Animation<Rectangle2D> pacSpinning;
	private final List<EnumMap<Direction, Animation<Rectangle2D>>> ghostsKicking;
	private final EnumMap<Direction, Animation<Rectangle2D>> ghostEyes;
	private final Animation<Rectangle2D> ghostBlue;
	private final Animation<Rectangle2D> ghostFlashing;
	private final Animation<Integer> bonusJumps;
	private final List<Animation<Image>> mazesFlashing;
	private final Animation<Boolean> energizerBlinking;

	private final Font scoreFont;

	private int index(Direction dir) {
		return dir == RIGHT ? 0 : dir == LEFT ? 1 : dir == UP ? 2 : 3;
	}

	private Rectangle2D r(double x, double y, int tileX, int tileY, double xTiles, double yTiles) {
		return new Rectangle2D(x + tileX * 16, y + tileY * 16, xTiles * 16, yTiles * 16);
	}

	private Rectangle2D s(int tileX, int tileY) {
		return r(456, 0, tileX, tileY, 1, 1);
	}

	public MsPacManGameRendering(GraphicsContext g) {
		this.g = g;

		scoreFont = Font.loadFont(getClass().getResource("/emulogic.ttf").toExternalForm(), 8);

		symbols = new Rectangle2D[] { s(3, 0), s(4, 0), s(5, 0), s(6, 0), s(7, 0), s(8, 0), s(9, 0) };

		//@formatter:off

		bonusValues = new HashMap<>();
		bonusValues.put(100,  s(3, 1));
		bonusValues.put(200,  s(4, 1));
		bonusValues.put(500,  s(5, 1));
		bonusValues.put(700,  s(6, 1));
		bonusValues.put(1000, s(7, 1));
		bonusValues.put(2000, s(8, 1));
		bonusValues.put(5000, s(9, 1));
		
		bountyValues = new HashMap<>();
		bountyValues.put(200,  s(0, 8));
		bountyValues.put(400,  s(1, 8));
		bountyValues.put(800,  s(2, 8));
		bountyValues.put(1600, s(3, 8));
		//@formatter:on

		// Animations

		energizerBlinking = Animation.pulse().frameDuration(10);

		mazesFlashing = new ArrayList<>(6);
		for (int mazeIndex = 0; mazeIndex < 6; ++mazeIndex) {
			Map<Color, Color> exchanges = Map.of(getMazeWallBorderColor(mazeIndex), Color.WHITE, getMazeWallColor(mazeIndex),
					Color.BLACK);
			WritableImage mazeEmpty = new WritableImage(226, 248);
			mazeEmpty.getPixelWriter().setPixels(0, 0, 226, 248, spritesheet.getPixelReader(), 226, 248 * mazeIndex);
			Image mazeEmptyBright = RenderingWithAnimatedSprites.exchangeColors(mazeEmpty, exchanges);
			mazesFlashing.add(Animation.of(mazeEmptyBright, mazeEmpty).frameDuration(15));
		}

		pacMunching = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = index(dir);
			Animation<Rectangle2D> munching = Animation.of(s(1, d), s(1, d), s(2, d), s(0, d));
			munching.frameDuration(2).endless();
			pacMunching.put(dir, munching);
		}

		pacSpinning = Animation.of(s(0, 3), s(0, 0), s(0, 1), s(0, 2));
		pacSpinning.frameDuration(10).repetitions(2);

		ghostsKicking = new ArrayList<>(4);
		for (int id = 0; id < 4; ++id) {
			EnumMap<Direction, Animation<Rectangle2D>> walkingTo = new EnumMap<>(Direction.class);
			for (Direction dir : Direction.values()) {
				int d = index(dir);
				Animation<Rectangle2D> walking = Animation.of(s(2 * d, 4 + id), s(2 * d + 1, 4 + id));
				walking.frameDuration(4).endless();
				walkingTo.put(dir, walking);
			}
			ghostsKicking.add(walkingTo);
		}

		ghostEyes = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyes.put(dir, Animation.ofSingle(s(8 + index(dir), 5)));
		}

		ghostBlue = Animation.of(s(8, 4), s(9, 4));
		ghostBlue.frameDuration(20).endless().run();

		ghostFlashing = Animation.of(s(8, 4), s(9, 4), s(10, 4), s(11, 4));
		ghostFlashing.frameDuration(5).endless();

		bonusJumps = Animation.of(0, 2, 0, -2).frameDuration(20).endless().run();
	}

	@Override
	public Image spritesheet() {
		return spritesheet;
	}

	@Override
	public GraphicsContext gc() {
		return g;
	}

	@Override
	public Font getScoreFont() {
		return scoreFont;
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

	private Direction ensureNotNull(Direction dir) {
		return dir != null ? dir : Direction.RIGHT;
	}

	@Override
	public void signalReadyState(PacManGameModel game) {
		g.setFont(scoreFont);
		g.setFill(Color.YELLOW);
		g.fillText("READY", t(11), t(21));
	}

	@Override
	public void signalGameOverState(PacManGameModel game) {
		g.setFont(scoreFont);
		g.setFill(Color.RED);
		g.fillText("GAME", t(9), t(21));
		g.fillText("OVER", t(15), t(21));
	}

	@Override
	public void drawMaze(int mazeNumber, int x, int y, boolean flashing) {
		int index = mazeNumber - 1;
		if (flashing) {
			g.drawImage(mazeFlashing(mazeNumber).animate(), x, y);
		} else {
			Rectangle2D fullMazeRegion = new Rectangle2D(0, 248 * index, 226, 248);
			g.drawImage(spritesheet, fullMazeRegion.getMinX(), fullMazeRegion.getMinY(), fullMazeRegion.getWidth(),
					fullMazeRegion.getHeight(), x, y, fullMazeRegion.getWidth(), fullMazeRegion.getHeight());
		}
	}

	@Override
	public void drawFoodTiles(Stream<V2i> tiles, Predicate<V2i> eaten) {
		tiles.filter(eaten).forEach(this::hideTile);
	}

	@Override
	public void drawEnergizerTiles(Stream<V2i> energizerTiles) {
		if (energizerBlinking.animate()) {
			energizerTiles.forEach(this::hideTile);
		}
	}

	@Override
	public void drawLevelCounter(PacManGameModel game, int rightX, int y) {
		int x = rightX;
		int firstLevel = Math.max(1, game.currentLevelNumber - 6);
		for (int level = firstLevel; level <= game.currentLevelNumber; ++level) {
			Rectangle2D region = symbols[game.levelSymbols.get(level - 1)];
			g.drawImage(spritesheet, region.getMinX(), region.getMinY(), 16, 16, x, y, 16, 16);
			x -= t(2);
		}
	}

	@Override
	public void drawLivesCounter(PacManGameModel game, int x, int y) {
		int maxLivesDisplayed = 5;
		int livesDisplayed = game.started ? game.lives - 1 : game.lives;
		for (int i = 0; i < Math.min(livesDisplayed, maxLivesDisplayed); ++i) {
			g.drawImage(spritesheet, 456 + 16, 0, 16, 16, x + t(2 * i), y, 16, 16);
		}
	}

	@Override
	public void drawScore(PacManGameModel game) {
		g.setFont(scoreFont);
		g.translate(0, 2);
		g.setFill(Color.WHITE);
		g.fillText("SCORE", t(1), t(1));
		g.fillText("HIGHSCORE", t(15), t(1));
		g.translate(0, 1);
		if (game.state != PacManGameState.INTRO && !game.attractMode) {
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
	public void hideTile(V2i tile) {
		g.setFill(Color.BLACK);
		g.fillRect(tile.x * TS, tile.y * TS, TS, TS);
	}

	private void drawRegion(Creature guy, Rectangle2D region) {
		if (guy.visible && region != null) {
			g.drawImage(spritesheet, region.getMinX(), region.getMinY(), region.getWidth(), region.getHeight(),
					guy.position.x - 4, guy.position.y - 4, region.getWidth(), region.getHeight());
		}
	}

	@Override
	public void drawPac(Pac pac, PacManGameModel game) {
		drawRegion(pac, pacSprite(pac, game));
	}

	@Override
	public void drawGhost(Ghost ghost, PacManGameModel game) {
		drawRegion(ghost, ghostSprite(ghost, game));
	}

	@Override
	public void drawBonus(Bonus bonus, PacManGameModel game) {
		g.save();
		g.translate(0, bonusJumps.animate());
		drawRegion(bonus, bonusSprite(bonus, game));
		g.restore();
	}

	@Override
	public Rectangle2D bonusSprite(Bonus bonus, PacManGameModel game) {
		if (bonus.edibleTicksLeft > 0) {
			return symbols[bonus.symbol];
		}
		if (bonus.eatenTicksLeft > 0) {
			return bonusValues.get(bonus.points);
		}
		return null;
	}

	@Override
	public Rectangle2D pacSprite(Pac pac, PacManGameModel game) {
		if (pac.dead) {
			return pacDying().hasStarted() ? pacDying().animate() : pacMunchingToDir(pac.dir).frame();
		}
		if (pac.speed == 0) {
			return pacMunchingToDir(pac.dir).frame(0);
		}
		if (!pac.couldMove) {
			return pacMunchingToDir(pac.dir).frame(1);
		}
		return pacMunchingToDir(pac.dir).animate();
	}

	@Override
	public Rectangle2D ghostSprite(Ghost ghost, PacManGameModel game) {
		if (ghost.bounty > 0) {
			return bountyValues.get(ghost.bounty);
		}
		if (ghost.is(DEAD) || ghost.is(ENTERING_HOUSE)) {
			return ghostReturningHomeToDir(ghost, ghost.dir).animate();
		}
		if (ghost.is(FRIGHTENED)) {
			return ghostFlashing().isRunning() ? ghostFlashing().frame() : ghostFrightenedToDir(ghost, ghost.dir).animate();
		}
		if (ghost.is(LOCKED) && game.pac.powerTicksLeft > 0) {
			return ghostFrightenedToDir(ghost, ghost.dir).animate();
		}
		return ghostKickingToDir(ghost, ghost.wishDir).animate(); // Looks towards wish dir!
	}

	@Override
	public Animation<Rectangle2D> pacMunchingToDir(Direction dir) {
		return pacMunching.get(ensureNotNull(dir));
	}

	@Override
	public Animation<Rectangle2D> pacDying() {
		return pacSpinning;
	}

	@Override
	public Animation<Rectangle2D> ghostKickingToDir(Ghost ghost, Direction dir) {
		return ghostsKicking.get(ghost.id).get(ensureNotNull(dir));
	}

	@Override
	public Animation<Rectangle2D> ghostFrightenedToDir(Ghost ghost, Direction dir) {
		return ghostBlue;
	}

	@Override
	public Animation<Rectangle2D> ghostFlashing() {
		return ghostFlashing;
	}

	@Override
	public Animation<Rectangle2D> ghostReturningHomeToDir(Ghost ghost, Direction dir) {
		return ghostEyes.get(ensureNotNull(dir));
	}

	@Override
	public Animation<Image> mazeFlashing(int mazeNumber) {
		return mazesFlashing.get(mazeNumber - 1);
	}

	@Override
	public Animation<Boolean> energizerBlinking() {
		return energizerBlinking;
	}
}