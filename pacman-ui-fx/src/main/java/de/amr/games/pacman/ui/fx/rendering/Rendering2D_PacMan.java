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

	private final Spritesheet spritesheet;
	private final Image mazeFull;
	private final Image mazeEmpty;
	private final Image mazeEmptyBright;
	private final Map<Integer, Rectangle2D> bonusValueSprites;
	private final Map<String, Rectangle2D> symbolSprites;
	private final Map<Integer, Rectangle2D> bountyNumberSprites;

	public Rendering2D_PacMan(String resourcePath) {
		spritesheet = new Spritesheet(resourcePath + "sprites.png", 16);

		mazeFull = Rendering2D_Assets.image(resourcePath + "maze_full.png");
		mazeEmpty = Rendering2D_Assets.image(resourcePath + "maze_empty.png");
		mazeEmptyBright = Rendering2D_Assets.colorsExchanged(mazeEmpty, Map.of(getMazeWallBorderColor(0), Color.WHITE));

		//@formatter:off
		symbolSprites = Map.of(
				PacManGame.CHERRIES,   spritesheet().sprite(2, 3),
				PacManGame.STRAWBERRY, spritesheet().sprite(3, 3),
				PacManGame.PEACH,      spritesheet().sprite(4, 3),
				PacManGame.APPLE,      spritesheet().sprite(5, 3),
				PacManGame.GRAPES,     spritesheet().sprite(6, 3),
				PacManGame.GALAXIAN,   spritesheet().sprite(7, 3),
				PacManGame.BELL,       spritesheet().sprite(8, 3),
				PacManGame.KEY,        spritesheet().sprite(9, 3)
		);

		bonusValueSprites = Map.of(
			100,  spritesheet().cells(0, 9, 1, 1),
			300,  spritesheet().cells(1, 9, 1, 1),
			500,  spritesheet().cells(2, 9, 1, 1),
			700,  spritesheet().cells(3, 9, 1, 1),
			1000, spritesheet().cells(4, 9, 2, 1), // left-aligned 
			2000, spritesheet().cells(3, 10, 3, 1),
			3000, spritesheet().cells(3, 11, 3, 1),
			5000, spritesheet().cells(3, 12, 3, 1)
		);
		
		bountyNumberSprites = Map.of(
			200,  spritesheet().cells(0, 8, 1, 1),
			400,  spritesheet().cells(1, 8, 1, 1),
			800,  spritesheet().cells(2, 8, 1, 1),
			1600, spritesheet().cells(3, 8, 1, 1)
		);
		//@formatter:on
	}

	@Override
	public Spritesheet spritesheet() {
		return spritesheet;
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
		return spritesheet().sprite(8, 1);
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
		return spritesheet().sprite(8, 6);
	}

	public TimedSequence<Rectangle2D> createBigPacManMunchingAnimation() {
		return TimedSequence.of(//
				spritesheet().cells(2, 1, 2, 2), //
				spritesheet().cells(4, 1, 2, 2), //
				spritesheet().cells(6, 1, 2, 2))//
				.frameDuration(4).endless();
	}

	public TimedSequence<Rectangle2D> createBlinkyStretchedAnimation() {
		return TimedSequence.of(spritesheet().sprite(9, 6), spritesheet().sprite(10, 6), spritesheet().sprite(11, 6),
				spritesheet().sprite(12, 6));
	}

	public TimedSequence<Rectangle2D> createBlinkyDamagedAnimation() {
		return TimedSequence.of(spritesheet().sprite(8, 7), spritesheet().sprite(9, 7));
	}

	public TimedSequence<Rectangle2D> createBlinkyPatchedAnimation() {
		return TimedSequence.of(spritesheet().sprite(10, 7), spritesheet().sprite(11, 7)).frameDuration(4).endless();
	}

	public TimedSequence<Rectangle2D> createBlinkyNakedAnimation() {
		return TimedSequence.of(spritesheet().cells(8, 8, 2, 1), spritesheet().cells(10, 8, 2, 1)).frameDuration(4)
				.endless();
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createPlayerMunchingAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> munchingAnimation = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = spritesheet().dirIndex(dir);
			TimedSequence<Rectangle2D> animation = TimedSequence.of(//
					spritesheet().sprite(2, 0), //
					spritesheet().sprite(1, d), //
					spritesheet().sprite(0, d), //
					spritesheet().sprite(1, d))//
					.frameDuration(2).endless();
			munchingAnimation.put(dir, animation);
		}
		return munchingAnimation;
	}

	@Override
	public TimedSequence<Rectangle2D> createPlayerDyingAnimation() {
		return TimedSequence.of(//
				spritesheet().sprite(3, 0), spritesheet().sprite(4, 0), //
				spritesheet().sprite(5, 0), spritesheet().sprite(6, 0), //
				spritesheet().sprite(7, 0), spritesheet().sprite(8, 0), //
				spritesheet().sprite(9, 0), spritesheet().sprite(10, 0), //
				spritesheet().sprite(11, 0), spritesheet().sprite(12, 0), //
				spritesheet().sprite(13, 0))//
				.frameDuration(8);
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createGhostKickingAnimations(int ghostID) {
		EnumMap<Direction, TimedSequence<Rectangle2D>> walkingTo = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = spritesheet().dirIndex(dir);
			TimedSequence<Rectangle2D> animation = TimedSequence.of(//
					spritesheet().sprite(2 * d, 4 + ghostID), //
					spritesheet().sprite(2 * d + 1, 4 + ghostID))//
					.frameDuration(4).endless();
			walkingTo.put(dir, animation);
		}
		return walkingTo;
	}

	@Override
	public TimedSequence<Rectangle2D> createGhostFrightenedAnimation() {
		return TimedSequence.of(//
				spritesheet().sprite(8, 4), spritesheet().sprite(9, 4))//
				.frameDuration(20).endless();
	}

	@Override
	public TimedSequence<Rectangle2D> createGhostFlashingAnimation() {
		return TimedSequence.of(//
				spritesheet().sprite(8, 4), spritesheet().sprite(9, 4), //
				spritesheet().sprite(10, 4), spritesheet().sprite(11, 4))//
				.frameDuration(6);
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createGhostReturningHomeAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> ghostEyesAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = spritesheet().dirIndex(dir);
			ghostEyesAnim.put(dir, TimedSequence.of(spritesheet().sprite(8 + d, 5)));
		}
		return ghostEyesAnim;
	}
}