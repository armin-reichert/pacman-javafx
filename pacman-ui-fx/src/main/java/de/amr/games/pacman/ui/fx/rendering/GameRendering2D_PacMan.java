package de.amr.games.pacman.ui.fx.rendering;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.GameEntity;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * 2D rendering for the scenes of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class GameRendering2D_PacMan extends GameRendering2D {

	private final Image mazeFull = new Image(getClass().getResource("/pacman/graphics/maze_full.png").toExternalForm());
	private final Image mazeEmpty = new Image(getClass().getResource("/pacman/graphics/maze_empty.png").toExternalForm());
	private Image mazeEmptyBright;

	public GameRendering2D_PacMan() {
		super("/pacman/graphics/sprites.png");

		symbolSprites = Arrays.asList(sprite(2, 3), sprite(3, 3), sprite(4, 3), sprite(5, 3), sprite(6, 3), sprite(7, 3),
				sprite(8, 3), sprite(9, 3));

		//@formatter:off
		bonusValueSprites = new HashMap<>();
		bonusValueSprites.put(100,  cells(0, 9, 1, 1));
		bonusValueSprites.put(300,  cells(1, 9, 1, 1));
		bonusValueSprites.put(500,  cells(2, 9, 1, 1));
		bonusValueSprites.put(700,  cells(3, 9, 1, 1));
		bonusValueSprites.put(1000, cells(4, 9, 2, 1)); // left-aligned 
		bonusValueSprites.put(2000, cells(3, 10, 3, 1));
		bonusValueSprites.put(3000, cells(3, 11, 3, 1));
		bonusValueSprites.put(5000, cells(3, 12, 3, 1));

		bountyNumberSprites = new HashMap<>();
		bountyNumberSprites.put(200,  cells(0, 8, 1, 1));
		bountyNumberSprites.put(400,  cells(1, 8, 1, 1));
		bountyNumberSprites.put(800,  cells(2, 8, 1, 1));
		bountyNumberSprites.put(1600, cells(3, 8, 1, 1));
		//@formatter:on

		// Animations

		mazeEmptyBright = colorsExchanged(mazeEmpty, Map.of(getMazeWallBorderColor(0), Color.WHITE));
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

	public void drawNail(GraphicsContext g, GameEntity nail) {
		drawEntity(g, nail, sprite(8, 6));
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createPlayerMunchingAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> pacManMunchingAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			TimedSequence<Rectangle2D> animation = TimedSequence.of(sprite(2, 0), sprite(1, index(dir)),
					sprite(0, index(dir)), sprite(1, index(dir)));
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
	public TimedSequence<Rectangle2D> createBlinkyStretchedAnimation() {
		return TimedSequence.of(sprite(9, 6), sprite(10, 6), sprite(11, 6), sprite(12, 6));
	}

	@Override
	public TimedSequence<Rectangle2D> createBlinkyDamagedAnimation() {
		return TimedSequence.of(sprite(8, 7), sprite(9, 7));
	}

	@Override
	public TimedSequence<Rectangle2D> createBlinkyPatchedAnimation() {
		return TimedSequence.of(sprite(10, 7), sprite(11, 7)).frameDuration(4).endless();
	}

	@Override
	public TimedSequence<Rectangle2D> createBlinkyNakedAnimation() {
		return TimedSequence.of(cells(8, 8, 2, 1), cells(10, 8, 2, 1)).frameDuration(4).endless();
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createSpouseMunchingAnimations() {
		return null;
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createGhostKickingAnimations(int ghostID) {
		EnumMap<Direction, TimedSequence<Rectangle2D>> walkingTo = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			TimedSequence<Rectangle2D> animation = TimedSequence.of(sprite(2 * index(dir), 4 + ghostID),
					sprite(2 * index(dir) + 1, 4 + ghostID));
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
			ghostEyesAnim.put(dir, TimedSequence.of(sprite(8 + index(dir), 5)));
		}
		return ghostEyesAnim;
	}

	@Override
	public TimedSequence<Rectangle2D> createBigPacManMunchingAnimation() {
		return TimedSequence.of(//
				cells(2, 1, 2, 2), //
				cells(4, 1, 2, 2), //
				cells(6, 1, 2, 2)).frameDuration(4).endless();
	}
}