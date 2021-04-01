package de.amr.games.pacman.ui.fx.rendering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui.animation.TimedSequence;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * 2D rendering for the the Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class GameRendering2D_MsPacMan extends GameRendering2D {

	public GameRendering2D_MsPacMan() {
		super("/mspacman/graphics/sprites.png");

		symbolSprites = Arrays.asList(s(3, 0), s(4, 0), s(5, 0), s(6, 0), s(7, 0), s(8, 0), s(9, 0));

		//@formatter:off
		bonusValueSprites = Map.of(
			100,  s(3, 1),
			200,  s(4, 1),
			500,  s(5, 1),
			700,  s(6, 1),
			1000, s(7, 1),
			2000, s(8, 1),
			5000, s(9, 1)
		);
		
		bountyNumberSprites = Map.of(
			200,  s(0, 8),
			400,  s(1, 8),
			800,  s(2, 8),
			1600, s(3, 8)
		);
		//@formatter:on

		mazeFlashingAnimations = new ArrayList<>(6);
		for (int mazeIndex = 0; mazeIndex < 6; ++mazeIndex) {
			Map<Color, Color> exchanges = Map.of(//
					getMazeWallBorderColor(mazeIndex), Color.WHITE, //
					getMazeWallColor(mazeIndex), Color.BLACK);
			WritableImage mazeEmpty = new WritableImage(226, 248);
			mazeEmpty.getPixelWriter().setPixels(0, 0, 226, 248, spritesheet.getPixelReader(), 226, 248 * mazeIndex);
			Image mazeEmptyBright = exchangeColors(mazeEmpty, exchanges);
			mazeFlashingAnimations.add(TimedSequence.of(mazeEmptyBright, mazeEmpty).frameDuration(15));
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
	public void drawMaze(GraphicsContext g, int mazeNumber, int x, int y, boolean flashing) {
		if (flashing) {
			g.drawImage(getMazeFlashingAnimation(mazeNumber).animate(), x, y);
		} else {
			Rectangle2D image = getMazeSprite(mazeNumber);
			g.drawImage(spritesheet, image.getMinX(), image.getMinY(), image.getWidth(), image.getHeight(), x, y,
					image.getWidth(), image.getHeight());
		}
	}

	@Override
	public Rectangle2D getMazeSprite(int mazeNumber) {
		return new Rectangle2D(0, 248 * (mazeNumber - 1), 226, 248);
	}

	/*
	 * Animations.
	 */

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createPlayerMunchingAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> msPacManMunchingAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = index(dir);
			TimedSequence<Rectangle2D> munching = TimedSequence.of(s(1, d), s(1, d), s(2, d), s(0, d));
			munching.frameDuration(2).endless();
			msPacManMunchingAnim.put(dir, munching);
		}
		return msPacManMunchingAnim;
	}

	@Override
	public TimedSequence<Rectangle2D> createPlayerDyingAnimation() {
		return TimedSequence.of(s(0, 3), s(0, 0), s(0, 1), s(0, 2)).frameDuration(10).repetitions(2);
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createSpouseMunchingAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> pacManMunchingAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = index(dir);
			pacManMunchingAnim.put(dir, TimedSequence.of(s(0, 9 + d), s(1, 9 + d), s(2, 9)).frameDuration(2).endless());
		}
		return pacManMunchingAnim;
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createGhostKickingAnimations(int ghostID) {
		EnumMap<Direction, TimedSequence<Rectangle2D>> kickingTo = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = index(dir);
			TimedSequence<Rectangle2D> kicking = TimedSequence.of(s(2 * d, 4 + ghostID), s(2 * d + 1, 4 + ghostID));
			kicking.frameDuration(4).endless();
			kickingTo.put(dir, kicking);
		}
		return kickingTo;
	}

	@Override
	public TimedSequence<Rectangle2D> createGhostFrightenedAnimation() {
		return TimedSequence.of(s(8, 4), s(9, 4)).frameDuration(20).endless();
	}

	@Override
	public TimedSequence<Rectangle2D> createGhostFlashingAnimation() {
		return TimedSequence.of(s(8, 4), s(9, 4), s(10, 4), s(11, 4)).frameDuration(4);
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createGhostReturningHomeAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> ghostEyesAnim = new EnumMap<>(Direction.class);
		Direction.stream().forEach(dir -> ghostEyesAnim.put(dir, TimedSequence.of(s(8 + index(dir), 5))));
		return ghostEyesAnim;
	}

	@Override
	public TimedSequence<Rectangle2D> createFlapAnimation() {
		return TimedSequence.of( //
				new Rectangle2D(456, 208, 32, 32), //
				new Rectangle2D(488, 208, 32, 32), //
				new Rectangle2D(520, 208, 32, 32), //
				new Rectangle2D(488, 208, 32, 32), //
				new Rectangle2D(456, 208, 32, 32)//
		).repetitions(1).frameDuration(4);
	}

	@Override
	public TimedSequence<Rectangle2D> createStorkFlyingAnimation() {
		return TimedSequence.of(//
				new Rectangle2D(489, 176, 32, 16), //
				new Rectangle2D(521, 176, 32, 16)//
		).endless().frameDuration(10);
	}

	@Override
	public TimedSequence<Integer> createBonusAnimation() {
		return TimedSequence.of(0, 2, 0, -2).frameDuration(20).endless();
	}

	/*
	 * Sprites.
	 */

	@Override
	public Rectangle2D getLifeImage() {
		return s(0, 0);
	}

	@Override
	public Rectangle2D getHeart() {
		return s(2, 10);
	}

	@Override
	public Rectangle2D getJunior() {
		return new Rectangle2D(509, 200, 8, 8);
	}

	@Override
	public Rectangle2D getBlueBag() {
		return new Rectangle2D(488, 199, 8, 8);
	}

	/* Tiles in right half of spritesheet */
	public Rectangle2D s(int tileX, int tileY) {
		return cellsStartingAt(456, 0, tileX, tileY, 1, 1);
	}
}