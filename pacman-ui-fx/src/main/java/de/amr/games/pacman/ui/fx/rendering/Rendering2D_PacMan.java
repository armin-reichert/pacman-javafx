package de.amr.games.pacman.ui.fx.rendering;

import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.pacman.PacManGame;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * 2D-rendering for the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class Rendering2D_PacMan extends Rendering2D {

	private static String RESOURCE_PATH = "/pacman/graphics/";

	private final Image mazeFull;
	private final Image mazeEmpty;
	private final Image mazeEmptyBright;
	private final Map<Integer, Rectangle2D> bonusValueSprites;
	private final Map<String, Rectangle2D> symbolSprites;
	private final Map<Integer, Rectangle2D> bountyNumberSprites;

	public Rendering2D_PacMan() {
		super(RESOURCE_PATH + "sprites.png", 16);

		mazeFull = Rendering2D_Assets.image(RESOURCE_PATH + "maze_full.png");
		mazeEmpty = Rendering2D_Assets.image(RESOURCE_PATH + "maze_empty.png");
		mazeEmptyBright = Rendering2D_Assets.colorsExchanged(mazeEmpty, Map.of(getMazeWallBorderColor(0), Color.WHITE));

		//@formatter:off
		symbolSprites = Map.of(
				PacManGame.CHERRIES,   sheet().sprite(2, 3),
				PacManGame.STRAWBERRY, sheet().sprite(3, 3),
				PacManGame.PEACH,      sheet().sprite(4, 3),
				PacManGame.APPLE,      sheet().sprite(5, 3),
				PacManGame.GRAPES,     sheet().sprite(6, 3),
				PacManGame.GALAXIAN,   sheet().sprite(7, 3),
				PacManGame.BELL,       sheet().sprite(8, 3),
				PacManGame.KEY,        sheet().sprite(9, 3)
		);

		bonusValueSprites = Map.of(
			100,  sheet().cells(0, 9, 1, 1),
			300,  sheet().cells(1, 9, 1, 1),
			500,  sheet().cells(2, 9, 1, 1),
			700,  sheet().cells(3, 9, 1, 1),
			1000, sheet().cells(4, 9, 2, 1), // left-aligned 
			2000, sheet().cells(3, 10, 3, 1),
			3000, sheet().cells(3, 11, 3, 1),
			5000, sheet().cells(3, 12, 3, 1)
		);
		
		bountyNumberSprites = Map.of(
			200,  sheet().cells(0, 8, 1, 1),
			400,  sheet().cells(1, 8, 1, 1),
			800,  sheet().cells(2, 8, 1, 1),
			1600, sheet().cells(3, 8, 1, 1)
		);
		//@formatter:on
	}

	public Color getMazeWallBorderColor(int mazeIndex) {
		return Color.rgb(33, 33, 255);
	}

	@Override
	public Color getMazeWallColor(int mazeIndex) {
		return Color.BLACK;
	}

	@Override
	public Image getMazeFullImage(int mazeNumber) {
		return mazeFull;
	}

	@Override
	public Image getMazeEmptyImage(int mazeNumber) {
		return mazeEmpty;
	}

	@Override
	public Image getMazeFlashImage(int mazeNumber) {
		return mazeEmptyBright;
	}

	@Override
	public Rectangle2D getLifeImage() {
		return sheet().sprite(8, 1);
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
		return sheet().sprite(8, 6);
	}

	public TimedSequence<Rectangle2D> createBigPacManMunchingAnimation() {
		return TimedSequence.of(//
				sheet().cells(2, 1, 2, 2), //
				sheet().cells(4, 1, 2, 2), //
				sheet().cells(6, 1, 2, 2))//
				.frameDuration(4).endless();
	}

	public TimedSequence<Rectangle2D> createBlinkyStretchedAnimation() {
		return TimedSequence.of(sheet().sprite(9, 6), sheet().sprite(10, 6), sheet().sprite(11, 6), sheet().sprite(12, 6));
	}

	public TimedSequence<Rectangle2D> createBlinkyDamagedAnimation() {
		return TimedSequence.of(sheet().sprite(8, 7), sheet().sprite(9, 7));
	}

	public TimedSequence<Rectangle2D> createBlinkyPatchedAnimation() {
		return TimedSequence.of(sheet().sprite(10, 7), sheet().sprite(11, 7)).frameDuration(4).endless();
	}

	public TimedSequence<Rectangle2D> createBlinkyNakedAnimation() {
		return TimedSequence.of(sheet().cells(8, 8, 2, 1), sheet().cells(10, 8, 2, 1)).frameDuration(4).endless();
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createPlayerMunchingAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> munchingAnimation = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = sheet().dirIndex(dir);
			TimedSequence<Rectangle2D> animation = TimedSequence.of(//
					sheet().sprite(2, 0), //
					sheet().sprite(1, d), //
					sheet().sprite(0, d), //
					sheet().sprite(1, d))//
					.frameDuration(2).endless();
			munchingAnimation.put(dir, animation);
		}
		return munchingAnimation;
	}

	@Override
	public TimedSequence<Rectangle2D> createPlayerDyingAnimation() {
		return TimedSequence.of(//
				sheet().sprite(3, 0), sheet().sprite(4, 0), //
				sheet().sprite(5, 0), sheet().sprite(6, 0), //
				sheet().sprite(7, 0), sheet().sprite(8, 0), //
				sheet().sprite(9, 0), sheet().sprite(10, 0), //
				sheet().sprite(11, 0), sheet().sprite(12, 0), //
				sheet().sprite(13, 0))//
				.frameDuration(8);
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createGhostKickingAnimations(int ghostID) {
		EnumMap<Direction, TimedSequence<Rectangle2D>> walkingTo = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = sheet().dirIndex(dir);
			TimedSequence<Rectangle2D> animation = TimedSequence.of(//
					sheet().sprite(2 * d, 4 + ghostID), //
					sheet().sprite(2 * d + 1, 4 + ghostID))//
					.frameDuration(4).endless();
			walkingTo.put(dir, animation);
		}
		return walkingTo;
	}

	@Override
	public TimedSequence<Rectangle2D> createGhostFrightenedAnimation() {
		return TimedSequence.of(//
				sheet().sprite(8, 4), sheet().sprite(9, 4))//
				.frameDuration(20).endless();
	}

	@Override
	public TimedSequence<Rectangle2D> createGhostFlashingAnimation() {
		return TimedSequence.of(//
				sheet().sprite(8, 4), sheet().sprite(9, 4), //
				sheet().sprite(10, 4), sheet().sprite(11, 4))//
				.frameDuration(6);
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createGhostReturningHomeAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> ghostEyesAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = sheet().dirIndex(dir);
			ghostEyesAnim.put(dir, TimedSequence.of(sheet().sprite(8 + d, 5)));
		}
		return ghostEyesAnim;
	}
}