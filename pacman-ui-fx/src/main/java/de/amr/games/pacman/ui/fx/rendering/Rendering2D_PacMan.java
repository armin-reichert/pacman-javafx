package de.amr.games.pacman.ui.fx.rendering;

import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * 2D-rendering for the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class Rendering2D_PacMan extends Rendering2D {

	private final Image mazeFull;
	private final Image mazeEmpty;
	private final Image mazeEmptyBright;
	private final Map<Integer, Rectangle2D> bonusValueSprites;
	private final Map<String, Rectangle2D> symbolSprites;
	private final Map<Integer, Rectangle2D> bountyNumberSprites;

	public Rendering2D_PacMan() {
		super("/pacman/graphics/sprites.png", 16);

		mazeFull = Rendering2D_Assets.image("/pacman/graphics/maze_full.png");
		mazeEmpty = Rendering2D_Assets.image("/pacman/graphics/maze_empty.png");
		mazeEmptyBright = Rendering2D_Assets.colorsExchanged(mazeEmpty, Map.of(getMazeWallBorderColor(0), Color.WHITE));

		//@formatter:off
		symbolSprites = Map.of(
				"CHERRIES", 	getSpritesheet().sprite(2, 3),
				"STRAWBERRY", getSpritesheet().sprite(3, 3),
				"PEACH",			getSpritesheet().sprite(4, 3),
				"APPLE",			getSpritesheet().sprite(5, 3),
				"GRAPES",			getSpritesheet().sprite(6, 3),
				"GALAXIAN",		getSpritesheet().sprite(7, 3),
				"BELL",				getSpritesheet().sprite(8, 3),
				"KEY",				getSpritesheet().sprite(9, 3)
		);

		bonusValueSprites = Map.of(
			100,  getSpritesheet().cells(0, 9, 1, 1),
			300,  getSpritesheet().cells(1, 9, 1, 1),
			500,  getSpritesheet().cells(2, 9, 1, 1),
			700,  getSpritesheet().cells(3, 9, 1, 1),
			1000, getSpritesheet().cells(4, 9, 2, 1), // left-aligned 
			2000, getSpritesheet().cells(3, 10, 3, 1),
			3000, getSpritesheet().cells(3, 11, 3, 1),
			5000, getSpritesheet().cells(3, 12, 3, 1)
		);
		
		bountyNumberSprites = Map.of(
			200,  getSpritesheet().cells(0, 8, 1, 1),
			400,  getSpritesheet().cells(1, 8, 1, 1),
			800,  getSpritesheet().cells(2, 8, 1, 1),
			1600, getSpritesheet().cells(3, 8, 1, 1)
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
		return getSpritesheet().sprite(8, 1);
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

	public Rectangle2D getNail() {
		return getSpritesheet().sprite(8, 6);
	}

	public TimedSequence<Rectangle2D> createBigPacManMunchingAnimation() {
		return TimedSequence.of(//
				getSpritesheet().cells(2, 1, 2, 2), //
				getSpritesheet().cells(4, 1, 2, 2), //
				getSpritesheet().cells(6, 1, 2, 2)).frameDuration(4).endless();
	}

	public TimedSequence<Rectangle2D> createBlinkyStretchedAnimation() {
		return TimedSequence.of(getSpritesheet().sprite(9, 6), getSpritesheet().sprite(10, 6),
				getSpritesheet().sprite(11, 6), getSpritesheet().sprite(12, 6));
	}

	public TimedSequence<Rectangle2D> createBlinkyDamagedAnimation() {
		return TimedSequence.of(getSpritesheet().sprite(8, 7), getSpritesheet().sprite(9, 7));
	}

	public TimedSequence<Rectangle2D> createBlinkyPatchedAnimation() {
		return TimedSequence.of(getSpritesheet().sprite(10, 7), getSpritesheet().sprite(11, 7)).frameDuration(4).endless();
	}

	public TimedSequence<Rectangle2D> createBlinkyNakedAnimation() {
		return TimedSequence.of(getSpritesheet().cells(8, 8, 2, 1), getSpritesheet().cells(10, 8, 2, 1)).frameDuration(4)
				.endless();
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createPlayerMunchingAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> pacManMunchingAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			TimedSequence<Rectangle2D> animation = TimedSequence.of(getSpritesheet().sprite(2, 0),
					getSpritesheet().sprite(1, getSpritesheet().dirIndex(dir)),
					getSpritesheet().sprite(0, getSpritesheet().dirIndex(dir)),
					getSpritesheet().sprite(1, getSpritesheet().dirIndex(dir)));
			animation.frameDuration(2).endless();
			pacManMunchingAnim.put(dir, animation);
		}
		return pacManMunchingAnim;
	}

	@Override
	public TimedSequence<Rectangle2D> createPlayerDyingAnimation() {
		return TimedSequence.of(getSpritesheet().sprite(3, 0), getSpritesheet().sprite(4, 0), getSpritesheet().sprite(5, 0),
				getSpritesheet().sprite(6, 0), getSpritesheet().sprite(7, 0), getSpritesheet().sprite(8, 0),
				getSpritesheet().sprite(9, 0), getSpritesheet().sprite(10, 0), getSpritesheet().sprite(11, 0),
				getSpritesheet().sprite(12, 0), getSpritesheet().sprite(13, 0)).frameDuration(8);
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createGhostKickingAnimations(int ghostID) {
		EnumMap<Direction, TimedSequence<Rectangle2D>> walkingTo = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			TimedSequence<Rectangle2D> animation = TimedSequence.of(
					getSpritesheet().sprite(2 * getSpritesheet().dirIndex(dir), 4 + ghostID),
					getSpritesheet().sprite(2 * getSpritesheet().dirIndex(dir) + 1, 4 + ghostID));
			animation.frameDuration(4).endless();
			walkingTo.put(dir, animation);
		}
		return walkingTo;
	}

	@Override
	public TimedSequence<Rectangle2D> createGhostFrightenedAnimation() {
		return TimedSequence.of(getSpritesheet().sprite(8, 4), getSpritesheet().sprite(9, 4)).frameDuration(20).endless();
	}

	@Override
	public TimedSequence<Rectangle2D> createGhostFlashingAnimation() {
		return TimedSequence.of(getSpritesheet().sprite(8, 4), getSpritesheet().sprite(9, 4),
				getSpritesheet().sprite(10, 4), getSpritesheet().sprite(11, 4)).frameDuration(6);
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createGhostReturningHomeAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> ghostEyesAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyesAnim.put(dir, TimedSequence.of(getSpritesheet().sprite(8 + getSpritesheet().dirIndex(dir), 5)));
		}
		return ghostEyesAnim;
	}
}