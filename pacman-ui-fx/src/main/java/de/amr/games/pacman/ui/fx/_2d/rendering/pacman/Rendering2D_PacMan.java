/*
MIT License

Copyright (c) 2021 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx._2d.rendering.pacman;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.pacman.PacManGame;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Pac-Man game-specific rendering.
 * 
 * @author Armin Reichert
 */
public class Rendering2D_PacMan extends Rendering2D {

	private static final Color MAZE_TOP_COLOR = Color.AZURE;
	private static final Color MAZE_SIDE_COLOR = Color.rgb(33, 33, 255);
	private static final Color FOOD_COLOR = Color.rgb(254, 189, 180);

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
		mazeFlashingImage = colorsExchanged(mazeEmptyImage, Collections.singletonMap(getMazeSideColor(1), Color.WHITE));

		//@formatter:off
		symbolSprites = Map.of(
			PacManGame.CHERRIES,   r(2, 3),
			PacManGame.STRAWBERRY, r(3, 3),
			PacManGame.PEACH,      r(4, 3),
			PacManGame.APPLE,      r(5, 3),
			PacManGame.GRAPES,     r(6, 3),
			PacManGame.GALAXIAN,   r(7, 3),
			PacManGame.BELL,       r(8, 3),
			PacManGame.KEY,        r(9, 3)
		);

		bonusValueSprites = Map.of(
			100,  r(0, 9, 1, 1),
			300,  r(1, 9, 1, 1),
			500,  r(2, 9, 1, 1),
			700,  r(3, 9, 1, 1),
			1000, r(4, 9, 2, 1), // left-aligned 
			2000, r(3, 10, 3, 1),
			3000, r(3, 11, 3, 1),
			5000, r(3, 12, 3, 1)
		);
		
		bountyNumberSprites = Map.of(
			200,  r(0, 8, 1, 1),
			400,  r(1, 8, 1, 1),
			800,  r(2, 8, 1, 1),
			1600, r(3, 8, 1, 1)
		);
		//@formatter:on
	}

	@Override
	public Color getMazeTopColor(int mazeNumber) {
		return MAZE_TOP_COLOR;
	}

	@Override
	public Color getMazeSideColor(int mazeNumber) {
		return MAZE_SIDE_COLOR;
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
	public Color getFoodColor(int mazeNumber) {
		return FOOD_COLOR;
	}

	@Override
	public Rectangle2D getLifeSprite() {
		return r(8, 1);
	}

	@Override
	public Map<Integer, Rectangle2D> getBonusValueSprites() {
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

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createPlayerMunchingAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> animations = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = dirIndex(dir);
			Rectangle2D wide_open = r(0, d), open = r(1, d), closed = r(2, 0);
			var animation = TimedSequence.of(closed, open, wide_open, open).frameDuration(2).endless();
			animations.put(dir, animation);
		}
		return animations;
	}

	@Override
	public TimedSequence<Rectangle2D> createPlayerDyingAnimation() {
		return TimedSequence
				.of(r(3, 0), r(4, 0), r(5, 0), r(6, 0), r(7, 0), r(8, 0), r(9, 0), r(10, 0), r(11, 0), r(12, 0), r(13, 0))
				.frameDuration(8);
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createGhostKickingAnimations(int ghostID) {
		EnumMap<Direction, TimedSequence<Rectangle2D>> animations = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = dirIndex(dir);
			var animation = TimedSequence.of(r(2 * d, 4 + ghostID), r(2 * d + 1, 4 + ghostID)).frameDuration(8).endless();
			animations.put(dir, animation);
		}
		return animations;
	}

	@Override
	public TimedSequence<Rectangle2D> createGhostFrightenedAnimation() {
		return TimedSequence.of(r(8, 4), r(9, 4)).frameDuration(8).endless();
	}

	@Override
	public TimedSequence<Rectangle2D> createGhostFlashingAnimation() {
		return TimedSequence.of(r(8, 4), r(9, 4), r(10, 4), r(11, 4)).frameDuration(6);
	}

	@Override
	public Map<Direction, TimedSequence<Rectangle2D>> createGhostReturningHomeAnimations() {
		Map<Direction, TimedSequence<Rectangle2D>> animations = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = dirIndex(dir);
			animations.put(dir, TimedSequence.of(r(8 + d, 5)));
		}
		return animations;
	}

	// Pac-Man specific:

	public Rectangle2D getNail() {
		return r(8, 6);
	}

	public TimedSequence<Rectangle2D> createBigPacManMunchingAnimation() {
		return TimedSequence.of(r(2, 1, 2, 2), r(4, 1, 2, 2), r(6, 1, 2, 2)).frameDuration(4).endless();
	}

	public TimedSequence<Rectangle2D> createBlinkyStretchedAnimation() {
		return TimedSequence.of(r(9, 6), r(10, 6), r(11, 6), r(12, 6));
	}

	public TimedSequence<Rectangle2D> createBlinkyDamagedAnimation() {
		return TimedSequence.of(r(8, 7), r(9, 7));
	}

	public TimedSequence<Rectangle2D> createBlinkyPatchedAnimation() {
		return TimedSequence.of(r(10, 7), r(11, 7)).frameDuration(4).endless();
	}

	public TimedSequence<Rectangle2D> createBlinkyNakedAnimation() {
		return TimedSequence.of(r(8, 8, 2, 1), r(10, 8, 2, 1)).frameDuration(4).endless();
	}

}