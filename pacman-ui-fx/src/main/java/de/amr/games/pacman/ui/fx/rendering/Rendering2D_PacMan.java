package de.amr.games.pacman.ui.fx.rendering;

import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * 2D rendering for the scenes of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class Rendering2D_PacMan extends Rendering2D {

	private final Image mazeFull = Rendering2D_Assets.image("/pacman/graphics/maze_full.png");
	private final Image mazeEmpty = Rendering2D_Assets.image("/pacman/graphics/maze_empty.png");
	private Image mazeEmptyBright;
	private Map<Integer, Rectangle2D> bonusValueSprites;
	private Map<String, Rectangle2D> symbolSprites;
	private Map<Integer, Rectangle2D> bountyNumberSprites;

	public Rendering2D_PacMan() {
		super("/pacman/graphics/sprites.png", 16);
		//@formatter:off
		symbolSprites = Map.of(
				"CHERRIES", 	sprite(2, 3),
				"STRAWBERRY", sprite(3, 3),
				"PEACH",			sprite(4, 3),
				"APPLE",			sprite(5, 3),
				"GRAPES",			sprite(6, 3),
				"GALAXIAN",		sprite(7, 3),
				"BELL",				sprite(8, 3),
				"KEY",				sprite(9, 3)
				
		);

		bonusValueSprites = Map.of(
			100,  cells(0, 9, 1, 1),
			300,  cells(1, 9, 1, 1),
			500,  cells(2, 9, 1, 1),
			700,  cells(3, 9, 1, 1),
			1000, cells(4, 9, 2, 1), // left-aligned 
			2000, cells(3, 10, 3, 1),
			3000, cells(3, 11, 3, 1),
			5000, cells(3, 12, 3, 1)
		);
		
		bountyNumberSprites = Map.of(
			200,  cells(0, 8, 1, 1),
			400,  cells(1, 8, 1, 1),
			800,  cells(2, 8, 1, 1),
			1600, cells(3, 8, 1, 1)
		);
		//@formatter:on

		mazeEmptyBright = Rendering2D_Assets.colorsExchanged(mazeEmpty, Map.of(getMazeWallBorderColor(0), Color.WHITE));
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
		return sprite(8, 1);
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
		return sprite(8, 6);
	}

	public TimedSequence<Rectangle2D> createBigPacManMunchingAnimation() {
		return TimedSequence.of(//
				cells(2, 1, 2, 2), //
				cells(4, 1, 2, 2), //
				cells(6, 1, 2, 2)).frameDuration(4).endless();
	}

	public TimedSequence<Rectangle2D> createBlinkyStretchedAnimation() {
		return TimedSequence.of(sprite(9, 6), sprite(10, 6), sprite(11, 6), sprite(12, 6));
	}

	public TimedSequence<Rectangle2D> createBlinkyDamagedAnimation() {
		return TimedSequence.of(sprite(8, 7), sprite(9, 7));
	}

	public TimedSequence<Rectangle2D> createBlinkyPatchedAnimation() {
		return TimedSequence.of(sprite(10, 7), sprite(11, 7)).frameDuration(4).endless();
	}

	public TimedSequence<Rectangle2D> createBlinkyNakedAnimation() {
		return TimedSequence.of(cells(8, 8, 2, 1), cells(10, 8, 2, 1)).frameDuration(4).endless();
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createPlayerMunchingAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> pacManMunchingAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			TimedSequence<Rectangle2D> animation = TimedSequence.of(sprite(2, 0), sprite(1, dirIndex(dir)),
					sprite(0, dirIndex(dir)), sprite(1, dirIndex(dir)));
			animation.frameDuration(2).endless();
			pacManMunchingAnim.put(dir, animation);
		}
		return pacManMunchingAnim;
	}

	@Override
	public TimedSequence<Rectangle2D> createPlayerDyingAnimation() {
		return TimedSequence.of(sprite(3, 0), sprite(4, 0), sprite(5, 0), sprite(6, 0), sprite(7, 0), sprite(8, 0),
				sprite(9, 0), sprite(10, 0), sprite(11, 0), sprite(12, 0), sprite(13, 0)).frameDuration(8);
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createGhostKickingAnimations(int ghostID) {
		EnumMap<Direction, TimedSequence<Rectangle2D>> walkingTo = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			TimedSequence<Rectangle2D> animation = TimedSequence.of(sprite(2 * dirIndex(dir), 4 + ghostID),
					sprite(2 * dirIndex(dir) + 1, 4 + ghostID));
			animation.frameDuration(4).endless();
			walkingTo.put(dir, animation);
		}
		return walkingTo;
	}

	@Override
	public TimedSequence<Rectangle2D> createGhostFrightenedAnimation() {
		return TimedSequence.of(sprite(8, 4), sprite(9, 4)).frameDuration(20).endless();
	}

	@Override
	public TimedSequence<Rectangle2D> createGhostFlashingAnimation() {
		return TimedSequence.of(sprite(8, 4), sprite(9, 4), sprite(10, 4), sprite(11, 4)).frameDuration(6);
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createGhostReturningHomeAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> ghostEyesAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyesAnim.put(dir, TimedSequence.of(sprite(8 + dirIndex(dir), 5)));
		}
		return ghostEyesAnim;
	}
}