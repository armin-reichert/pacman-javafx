package de.amr.games.pacman.ui.fx.rendering;

import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.pacman.PacManGame;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Pac-Man game-specific rendering.
 * 
 * @author Armin Reichert
 */
public class Rendering2D_PacMan extends Rendering2D_Common {

	private static final Color PACMAN_MAZE_TOP_COLOR = Color.rgb(255, 255, 255);
	private static final Color PACMAN_MAZE_SIDE_COLOR = Color.rgb(33, 33, 255);

	private final Image mazeFullImage;
	private final Image mazeEmptyImage;
	private final Image mazeFlashingImage;
	private final Map<Integer, Rectangle2D> bonusValueSprites;
	private final Map<String, Rectangle2D> symbolSprites;
	private final Map<Integer, Rectangle2D> bountyNumberSprites;

	public Rendering2D_PacMan() {
		super("/pacman/graphics/sprites.png", 16);
		mazeFullImage = new Image(resource("/pacman/graphics/maze_full.png"));
		mazeEmptyImage = new Image(resource("/pacman/graphics/maze_empty.png"));
		mazeFlashingImage = colorsExchanged(mazeEmptyImage, Map.of(getMazeSideColor(1), Color.WHITE));

		//@formatter:off
		symbolSprites = Map.of(
				PacManGame.CHERRIES,   region(2, 3),
				PacManGame.STRAWBERRY, region(3, 3),
				PacManGame.PEACH,      region(4, 3),
				PacManGame.APPLE,      region(5, 3),
				PacManGame.GRAPES,     region(6, 3),
				PacManGame.GALAXIAN,   region(7, 3),
				PacManGame.BELL,       region(8, 3),
				PacManGame.KEY,        region(9, 3)
		);

		bonusValueSprites = Map.of(
			100,  region(0, 9, 1, 1),
			300,  region(1, 9, 1, 1),
			500,  region(2, 9, 1, 1),
			700,  region(3, 9, 1, 1),
			1000, region(4, 9, 2, 1), // left-aligned 
			2000, region(3, 10, 3, 1),
			3000, region(3, 11, 3, 1),
			5000, region(3, 12, 3, 1)
		);
		
		bountyNumberSprites = Map.of(
			200,  region(0, 8, 1, 1),
			400,  region(1, 8, 1, 1),
			800,  region(2, 8, 1, 1),
			1600, region(3, 8, 1, 1)
		);
		//@formatter:on
	}

	@Override
	public Color getMazeTopColor(int mazeNumber) {
		return PACMAN_MAZE_TOP_COLOR;
	}

	@Override
	public Color getMazeSideColor(int mazeNumber) {
		return PACMAN_MAZE_SIDE_COLOR;
	}

	@Override
	public void renderMazeFull(GraphicsContext g, int mazeNumber, double x, double y) {
		g.drawImage(mazeFullImage, x, y);
	}

	@Override
	public void renderMazeEmpty(GraphicsContext g, int mazeNumber, double x, double y) {
		g.drawImage(mazeEmptyImage, x, y);
	}

	@Override
	public void renderMazeFlashing(GraphicsContext g, int mazeNumber, double x, double y) {
		g.drawImage(mazeFlashingImage, x, y);
	}

	@Override
	public Rectangle2D getLifeSprite() {
		return region(8, 1);
	}

	@Override
	public Map<Integer, Rectangle2D> getBonusValuesSprites() {
		return bonusValueSprites;
	}

	@Override
	public Map<Integer, Rectangle2D> getBountyNumberSprites() {
		return bountyNumberSprites;
	}

	@Override
	public Map<String, Rectangle2D> getSymbolSprites() {
		return symbolSprites;
	}

	public Rectangle2D getNail() {
		return region(8, 6);
	}

	public TimedSequence<Rectangle2D> createBigPacManMunchingAnimation() {
		return TimedSequence.of(//
				region(2, 1, 2, 2), //
				region(4, 1, 2, 2), //
				region(6, 1, 2, 2))//
				.frameDuration(4).endless();
	}

	public TimedSequence<Rectangle2D> createBlinkyStretchedAnimation() {
		return TimedSequence.of(region(9, 6), region(10, 6), region(11, 6), region(12, 6));
	}

	public TimedSequence<Rectangle2D> createBlinkyDamagedAnimation() {
		return TimedSequence.of(region(8, 7), region(9, 7));
	}

	public TimedSequence<Rectangle2D> createBlinkyPatchedAnimation() {
		return TimedSequence.of(region(10, 7), region(11, 7)).frameDuration(4).endless();
	}

	public TimedSequence<Rectangle2D> createBlinkyNakedAnimation() {
		return TimedSequence.of(region(8, 8, 2, 1), region(10, 8, 2, 1)).frameDuration(4).endless();
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createPlayerMunchingAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> munchingAnimation = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = dirIndex(dir);
			TimedSequence<Rectangle2D> animation = TimedSequence.of(//
					region(2, 0), //
					region(1, d), //
					region(0, d), //
					region(1, d))//
					.frameDuration(2).endless();
			munchingAnimation.put(dir, animation);
		}
		return munchingAnimation;
	}

	@Override
	public TimedSequence<Rectangle2D> createPlayerDyingAnimation() {
		return TimedSequence.of(//
				region(3, 0), region(4, 0), //
				region(5, 0), region(6, 0), //
				region(7, 0), region(8, 0), //
				region(9, 0), region(10, 0), //
				region(11, 0), region(12, 0), //
				region(13, 0))//
				.frameDuration(8);
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createGhostKickingAnimations(int ghostID) {
		EnumMap<Direction, TimedSequence<Rectangle2D>> walkingTo = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = dirIndex(dir);
			TimedSequence<Rectangle2D> animation = TimedSequence.of(//
					region(2 * d, 4 + ghostID), //
					region(2 * d + 1, 4 + ghostID))//
					.frameDuration(4).endless();
			walkingTo.put(dir, animation);
		}
		return walkingTo;
	}

	@Override
	public TimedSequence<Rectangle2D> createGhostFrightenedAnimation() {
		return TimedSequence.of(//
				region(8, 4), region(9, 4))//
				.frameDuration(20).endless();
	}

	@Override
	public TimedSequence<Rectangle2D> createGhostFlashingAnimation() {
		return TimedSequence.of(//
				region(8, 4), region(9, 4), //
				region(10, 4), region(11, 4))//
				.frameDuration(6);
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createGhostReturningHomeAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> ghostEyesAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = dirIndex(dir);
			ghostEyesAnim.put(dir, TimedSequence.of(region(8 + d, 5)));
		}
		return ghostEyesAnim;
	}
}