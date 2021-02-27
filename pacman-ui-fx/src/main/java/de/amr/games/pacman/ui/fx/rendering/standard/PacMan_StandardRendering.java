package de.amr.games.pacman.ui.fx.rendering.standard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.Flap;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.JuniorBag;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.pacman.PacManBonus;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Default rendering for the scenes of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacMan_StandardRendering extends StandardRendering {

	private final Image mazeFull = new Image("/pacman/graphics/maze_full.png", false);
	private final Image mazeEmpty = new Image("/pacman/graphics/maze_empty.png", false);

	private Animation<Rectangle2D> bigPacMan;
	private Animation<Rectangle2D> blinkyStretched;
	private Animation<Rectangle2D> blinkyDamaged;
	private Animation<Rectangle2D> blinkyPatched;
	private Animation<Rectangle2D> blinkyHalfNaked;

	public PacMan_StandardRendering() {
		super("/pacman/graphics/sprites.png");

		symbolSprites = Arrays.asList(sprite(2, 3), sprite(3, 3), sprite(4, 3), sprite(5, 3), sprite(6, 3), sprite(7, 3),
				sprite(8, 3), sprite(9, 3));

		//@formatter:off
		bonusValueSprites = new HashMap<>();
		bonusValueSprites.put(100,  gridCells(0, 9, 1, 1));
		bonusValueSprites.put(300,  gridCells(1, 9, 1, 1));
		bonusValueSprites.put(500,  gridCells(2, 9, 1, 1));
		bonusValueSprites.put(700,  gridCells(3, 9, 1, 1));
		bonusValueSprites.put(1000, gridCells(4, 9, 2, 1)); // left-aligned 
		bonusValueSprites.put(2000, gridCells(3, 10, 3, 1));
		bonusValueSprites.put(3000, gridCells(3, 11, 3, 1));
		bonusValueSprites.put(5000, gridCells(3, 12, 3, 1));

		bountyValueSprites = new HashMap<>();
		bountyValueSprites.put(200,  gridCells(0, 8, 1, 1));
		bountyValueSprites.put(400,  gridCells(1, 8, 1, 1));
		bountyValueSprites.put(800,  gridCells(2, 8, 1, 1));
		bountyValueSprites.put(1600, gridCells(3, 8, 1, 1));
		//@formatter:on

		// Animations

		Image mazeEmptyBright = exchangeColors(mazeEmpty, Map.of(getMazeWallBorderColor(0), Color.WHITE));
		mazeFlashingAnim = Arrays.asList(Animation.of(mazeEmptyBright, mazeEmpty).frameDuration(15));

		pacManMunchingAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			Animation<Rectangle2D> animation = Animation.of(sprite(2, 0), sprite(1, index(dir)), sprite(0, index(dir)),
					sprite(1, index(dir)));
			animation.frameDuration(2).endless().run();
			pacManMunchingAnim.put(dir, animation);
		}

		playerDyingAnim = Animation.of(sprite(3, 0), sprite(4, 0), sprite(5, 0), sprite(6, 0), sprite(7, 0), sprite(8, 0),
				sprite(9, 0), sprite(10, 0), sprite(11, 0), sprite(12, 0), sprite(13, 0)).frameDuration(8);

		bigPacMan = Animation.of(//
				gridCells(2, 1, 2, 2), //
				gridCells(4, 1, 2, 2), //
				gridCells(6, 1, 2, 2)).frameDuration(4).endless().run();

		ghostsKickingAnim = new ArrayList<>(4);
		for (int id = 0; id < 4; ++id) {
			EnumMap<Direction, Animation<Rectangle2D>> walkingTo = new EnumMap<>(Direction.class);
			for (Direction dir : Direction.values()) {
				Animation<Rectangle2D> animation = Animation.of(sprite(2 * index(dir), 4 + id),
						sprite(2 * index(dir) + 1, 4 + id));
				animation.frameDuration(10).endless();
				walkingTo.put(dir, animation);
			}
			ghostsKickingAnim.add(walkingTo);
		}

		ghostEyesAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyesAnim.put(dir, Animation.of(sprite(8 + index(dir), 5)));
		}

		ghostFrightenedAnim = Animation.of(sprite(8, 4), sprite(9, 4));
		ghostFrightenedAnim.frameDuration(20).endless();

		ghostFlashingAnim = Animation.of(sprite(8, 4), sprite(9, 4), sprite(10, 4), sprite(11, 4));
		ghostFlashingAnim.frameDuration(5).endless();

		blinkyPatched = Animation.of(sprite(10, 7), sprite(11, 7)).restart().frameDuration(4).endless();
		blinkyDamaged = Animation.of(sprite(8, 7), sprite(9, 7));
		blinkyStretched = Animation.of(sprite(9, 6), sprite(10, 6), sprite(11, 6), sprite(12, 6));
		blinkyHalfNaked = Animation.of(gridCells(8, 8, 2, 1), gridCells(10, 8, 2, 1)).endless().frameDuration(4).restart();
	}

	@Override
	public Color getMazeWallBorderColor(int mazeIndex) {
		return Color.rgb(33, 33, 255);
	}

	@Override
	public Color getMazeWallColor(int mazeIndex) {
		return Color.BLACK;
	}

	@Override
	public Animation<Rectangle2D> playerMunching(Pac pac, Direction dir) {
		return pacManMunchingAnim.get(ensureDirection(dir));
	}

	@Override
	public Animation<?> spouseMunching(Pac spouse, Direction dir) {
		return null;
	}

	@Override
	public void drawMaze(GraphicsContext g, int mazeNumber, int x, int y, boolean flashing) {
		if (flashing) {
			g.drawImage((Image) mazeFlashing(mazeNumber).animate(), x, y);
		} else {
			g.drawImage(mazeFull, x, y);
		}
	}

	@Override
	public void drawLifeCounterSymbol(GraphicsContext g, int x, int y) {
		drawSprite(g, sprite(8, 1), x, y);
	}

	@Override
	public void drawBonus(GraphicsContext g, PacManBonus bonus) {
		drawEntity(g, bonus, bonusSprite(bonus));
	}

	// Pac-Man only:

	@Override
	public void drawBigPacMan(GraphicsContext g, Pac bigPac) {
		drawEntity(g, bigPac, bigPacMan.animate());
	}

	@Override
	public void drawNail(GraphicsContext g, GameEntity nail) {
		drawEntity(g, nail, sprite(8, 6));
	}

	@Override
	public void drawBlinkyStretched(GraphicsContext g, Ghost blinky, V2d nailPosition, int stretching) {
		drawSprite(g, blinkyStretched.frame(stretching), nailPosition.x - 4, nailPosition.y - 4);
		if (stretching < 3) {
			drawGhost(g, blinky, false);
		} else {
			drawEntity(g, blinky, blinkyDamaged.frame(blinky.dir == Direction.UP ? 0 : 1));
		}
	}

	@Override
	public void drawBlinkyPatched(GraphicsContext g, Ghost blinky) {
		drawEntity(g, blinky, blinkyPatched.animate());
	}

	@Override
	public void drawBlinkyNaked(GraphicsContext g, Ghost blinky) {
		drawEntity(g, blinky, blinkyHalfNaked.animate());
	}

	// Ms. Pac-Man only:

	@Override
	public void drawSpouse(GraphicsContext g, Pac pac) {
	}

	@Override
	public void drawFlap(GraphicsContext g, Flap flap) {
	}

	@Override
	public void drawHeart(GraphicsContext g, GameEntity heart) {
	}

	@Override
	public void drawStork(GraphicsContext g, GameEntity stork) {
	}

	@Override
	public void drawJuniorBag(GraphicsContext g, JuniorBag bag) {
	}

	@Override
	public Animation<?> flapFlapping() {
		return null;
	}

	@Override
	public Animation<?> storkFlying() {
		return null;
	}
}