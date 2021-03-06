package de.amr.games.pacman.ui.fx.rendering;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * 2D-rendering for the Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class Rendering2D_MsPacMan extends Rendering2D {

	private static String RESOURCE_PATH = "/mspacman/graphics/";

	private final List<Image> mazeFullImages;
	private final List<Image> mazeEmptyImages;
	private final List<Image> mazeFlashImages;
	private final Map<Integer, Rectangle2D> bonusValueSprites;
	private final Map<String, Rectangle2D> symbolSprites;
	private final Map<Integer, Rectangle2D> bountyNumberSprites;

	public Rendering2D_MsPacMan() {
		super(RESOURCE_PATH + "sprites.png", 16);

		//@formatter:off
		symbolSprites = Map.of(
			"CHERRIES", 	rightSide(3,0),
			"STRAWBERRY", rightSide(4,0),
			"PEACH",			rightSide(5,0),
			"PRETZEL",		rightSide(6,0),
			"APPLE",			rightSide(7,0),
			"PEAR",				rightSide(8,0),
			"BANANA",			rightSide(9,0)
		);

		bonusValueSprites = Map.of(
			 100, rightSide(3, 1), 
			 200, rightSide(4, 1), 
			 500, rightSide(5, 1), 
			 700, rightSide(6, 1), 
			1000, rightSide(7, 1), 
			2000, rightSide(8, 1),
			5000, rightSide(9, 1)
		);

		bountyNumberSprites = Map.of(
			 200, rightSide(0, 8), 
			 400, rightSide(1, 8), 
			 800, rightSide(2, 8), 
			1600, rightSide(3, 8)
		);
		//@formatter:on

		mazeFullImages = new ArrayList<>(6);
		mazeEmptyImages = new ArrayList<>(6);
		mazeFlashImages = new ArrayList<>(6);
		for (int mazeIndex = 0; mazeIndex < 6; ++mazeIndex) {
			Image mazeFullImage = sheet().subImage(0, 248 * mazeIndex, 226, 248);
			Image mazeEmptyImage = sheet().subImage(226, 248 * mazeIndex, 226, 248);
			Image mazeFlashImage = Rendering2D_Assets.colorsExchanged(mazeEmptyImage, //
					Map.of(//
							getMazeWallBorderColor(mazeIndex), Color.WHITE, //
							getMazeWallColor(mazeIndex), Color.BLACK));
			mazeFullImages.add(mazeFullImage);
			mazeEmptyImages.add(mazeEmptyImage);
			mazeFlashImages.add(mazeFlashImage);
		}
	}

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex
	 * @return
	 */
	@Override
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

	@Override
	public Image getMazeFullImage(int mazeNumber) {
		return mazeFullImages.get(mazeNumber - 1);
	}

	@Override
	public Image getMazeEmptyImage(int mazeNumber) {
		return mazeEmptyImages.get(mazeNumber - 1);
	}

	@Override
	public Image getMazeFlashImage(int mazeNumber) {
		return mazeFlashImages.get(mazeNumber - 1);
	}

	@Override
	public Map<Integer, Rectangle2D> getBonusValuesSpritesMap() {
		return bonusValueSprites;
	}

	@Override
	public Map<Integer, Rectangle2D> getBountyNumberSpritesMap() {
		return bountyNumberSprites;
	}

	@Override
	public Map<String, Rectangle2D> getSymbolSprites() {
		return symbolSprites;
	}

	/*
	 * Animations.
	 */

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createPlayerMunchingAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> msPacManMunchingAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = sheet().dirIndex(dir);
			TimedSequence<Rectangle2D> munching = TimedSequence.of(//
					rightSide(0, d), rightSide(0, d), rightSide(1, d), rightSide(2, d))//
					.frameDuration(2).endless();
			msPacManMunchingAnim.put(dir, munching);
		}
		return msPacManMunchingAnim;
	}

	@Override
	public TimedSequence<Rectangle2D> createPlayerDyingAnimation() {
		return TimedSequence.of(rightSide(0, 3), rightSide(0, 0), rightSide(0, 1), rightSide(0, 2)).frameDuration(10)
				.repetitions(2);
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createGhostKickingAnimations(int ghostID) {
		EnumMap<Direction, TimedSequence<Rectangle2D>> kickingTo = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = sheet().dirIndex(dir);
			TimedSequence<Rectangle2D> kicking = TimedSequence.of(rightSide(2 * d, 4 + ghostID),
					rightSide(2 * d + 1, 4 + ghostID));
			kicking.frameDuration(4).endless();
			kickingTo.put(dir, kicking);
		}
		return kickingTo;
	}

	@Override
	public TimedSequence<Rectangle2D> createGhostFrightenedAnimation() {
		return TimedSequence.of(rightSide(8, 4), rightSide(9, 4)).frameDuration(20).endless();
	}

	@Override
	public TimedSequence<Rectangle2D> createGhostFlashingAnimation() {
		return TimedSequence.of(rightSide(8, 4), rightSide(9, 4), rightSide(10, 4), rightSide(11, 4)).frameDuration(4);
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createGhostReturningHomeAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> ghostEyesAnim = new EnumMap<>(Direction.class);
		Direction.stream()
				.forEach(dir -> ghostEyesAnim.put(dir, TimedSequence.of(rightSide(8 + sheet().dirIndex(dir), 5))));
		return ghostEyesAnim;
	}

	public Map<Direction, TimedSequence<Rectangle2D>> createSpouseMunchingAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> pacManMunchingAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = sheet().dirIndex(dir);
			pacManMunchingAnim.put(dir,
					TimedSequence.of(rightSide(0, 9 + d), rightSide(1, 9 + d), rightSide(2, 9)).frameDuration(2).endless());
		}
		return pacManMunchingAnim;
	}

	public TimedSequence<Rectangle2D> createFlapAnimation() {
		return TimedSequence.of( //
				new Rectangle2D(456, 208, 32, 32), //
				new Rectangle2D(488, 208, 32, 32), //
				new Rectangle2D(520, 208, 32, 32), //
				new Rectangle2D(488, 208, 32, 32), //
				new Rectangle2D(456, 208, 32, 32)//
		).repetitions(1).frameDuration(4);
	}

	public TimedSequence<Rectangle2D> createStorkFlyingAnimation() {
		return TimedSequence.of(//
				new Rectangle2D(489, 176, 32, 16), //
				new Rectangle2D(521, 176, 32, 16)//
		).endless().frameDuration(10);
	}

	public TimedSequence<Integer> createBonusAnimation() {
		return TimedSequence.of(2, -2).frameDuration(20).endless();
	}

	/*
	 * Sprites.
	 */

	@Override
	public Rectangle2D getLifeImage() {
		return rightSide(1, 0);
	}

	public Rectangle2D getHeart() {
		return rightSide(2, 10);
	}

	public Rectangle2D getJunior() {
		return new Rectangle2D(509, 200, 8, 8);
	}

	public Rectangle2D getBlueBag() {
		return new Rectangle2D(488, 199, 8, 8);
	}

	/* Tiles in right half of spritesheet */
	public Rectangle2D rightSide(int tileX, int tileY) {
		return sheet().cellsStartingAt(456, 0, tileX, tileY, 1, 1);
	}
}