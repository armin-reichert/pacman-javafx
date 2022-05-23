/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
import de.amr.games.pacman.lib.TimedSeq;
import de.amr.games.pacman.model.pacman.PacManGame;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx.util.U;
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

	private static Rendering2D_PacMan it;

	public static Rendering2D_PacMan get() {
		if (it == null) {
			it = new Rendering2D_PacMan();
		}
		return it;
	}

	private final Image mazeFullImage;
	private final Image mazeEmptyImage;
	private final Image mazeFlashingImage;
	private final Map<Integer, Rectangle2D> bonusValueSprites;
	private final Map<Integer, Rectangle2D> symbolSprites;
	private final Map<Integer, Rectangle2D> bountyNumberSprites;

	private Rendering2D_PacMan() {
		super("/pacman/graphics/sprites.png", 16, Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

		mazeFullImage = U.image("/pacman/graphics/maze_full.png");
		mazeEmptyImage = U.image("/pacman/graphics/maze_empty.png");
		mazeFlashingImage = colorsExchanged(mazeEmptyImage, Collections.singletonMap(getMazeSideColor(1), Color.WHITE));

		//@formatter:off
		symbolSprites = Map.of(
			PacManGame.CHERRIES,   ss.r(2, 3),
			PacManGame.STRAWBERRY, ss.r(3, 3),
			PacManGame.PEACH,      ss.r(4, 3),
			PacManGame.APPLE,      ss.r(5, 3),
			PacManGame.GRAPES,     ss.r(6, 3),
			PacManGame.GALAXIAN,   ss.r(7, 3),
			PacManGame.BELL,       ss.r(8, 3),
			PacManGame.KEY,        ss.r(9, 3)
		);

		bonusValueSprites = Map.of(
			100,  ss.r(0, 9, 1, 1),
			300,  ss.r(1, 9, 1, 1),
			500,  ss.r(2, 9, 1, 1),
			700,  ss.r(3, 9, 1, 1),
			1000, ss.r(4, 9, 2, 1), // left-aligned 
			2000, ss.r(3, 10, 3, 1),
			3000, ss.r(3, 11, 3, 1),
			5000, ss.r(3, 12, 3, 1)
		);
		
		bountyNumberSprites = Map.of(
			200,  ss.r(0, 8, 1, 1),
			400,  ss.r(1, 8, 1, 1),
			800,  ss.r(2, 8, 1, 1),
			1600, ss.r(3, 8, 1, 1)
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
	public Color getGhostHouseDoorColor(int mazeNumber) {
		return Color.rgb(252, 181, 255);
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
	public void renderMazeBright(GraphicsContext g, int mazeNumber, double x, double y) {
		g.drawImage(mazeFlashingImage, x, y);
	}

	@Override
	public Color getFoodColor(int mazeNumber) {
		return FOOD_COLOR;
	}

	@Override
	public Rectangle2D getLifeSprite() {
		return ss.r(8, 1);
	}

	@Override
	public Rectangle2D getBonusValueSprite(int number) {
		return bonusValueSprites.get(number);
	}

	@Override
	public Rectangle2D getBountyNumberSprite(int number) {
		return bountyNumberSprites.get(number);
	}

	@Override
	public Rectangle2D getSymbolSprite(int symbol) {
		return symbolSprites.get(symbol);
	}

	@Override
	public Map<Direction, TimedSeq<Rectangle2D>> createPlayerMunchingAnimations() {
		Map<Direction, TimedSeq<Rectangle2D>> animations = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = ss.dirIndex(dir);
			Rectangle2D wide_open = ss.r(0, d), open = ss.r(1, d), closed = ss.r(2, 0);
			var animation = TimedSeq.of(closed, open, wide_open, open).frameDuration(2).endless();
			animations.put(dir, animation);
		}
		return animations;
	}

	@Override
	public TimedSeq<Rectangle2D> createPlayerDyingAnimation() {
		return TimedSeq.of(ss.r(3, 0), ss.r(4, 0), ss.r(5, 0), ss.r(6, 0), ss.r(7, 0), ss.r(8, 0), ss.r(9, 0), ss.r(10, 0),
				ss.r(11, 0), ss.r(12, 0), ss.r(13, 0)).frameDuration(8);
	}

	@Override
	public Map<Direction, TimedSeq<Rectangle2D>> createGhostKickingAnimations(int ghostID) {
		EnumMap<Direction, TimedSeq<Rectangle2D>> animations = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = ss.dirIndex(dir);
			var animation = TimedSeq.of(ss.r(2 * d, 4 + ghostID), ss.r(2 * d + 1, 4 + ghostID)).frameDuration(8).endless();
			animations.put(dir, animation);
		}
		return animations;
	}

	@Override
	public TimedSeq<Rectangle2D> createGhostFrightenedAnimation() {
		return TimedSeq.of(ss.r(8, 4), ss.r(9, 4)).frameDuration(8).endless();
	}

	@Override
	public TimedSeq<Rectangle2D> createGhostFlashingAnimation() {
		return TimedSeq.of(ss.r(8, 4), ss.r(9, 4), ss.r(10, 4), ss.r(11, 4)).frameDuration(6);
	}

	@Override
	public Map<Direction, TimedSeq<Rectangle2D>> createGhostReturningHomeAnimations() {
		Map<Direction, TimedSeq<Rectangle2D>> animations = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = ss.dirIndex(dir);
			animations.put(dir, TimedSeq.of(ss.r(8 + d, 5)));
		}
		return animations;
	}

	// Pac-Man specific:

	public Rectangle2D getNail() {
		return ss.r(8, 6);
	}

	public TimedSeq<Rectangle2D> createBigPacManMunchingAnimation() {
		return TimedSeq.of(ss.r(2, 1, 2, 2), ss.r(4, 1, 2, 2), ss.r(6, 1, 2, 2)).frameDuration(4).endless();
	}

	public TimedSeq<Rectangle2D> createBlinkyStretchedAnimation() {
		return TimedSeq.of(ss.r(9, 6), ss.r(10, 6), ss.r(11, 6), ss.r(12, 6));
	}

	public TimedSeq<Rectangle2D> createBlinkyDamagedAnimation() {
		return TimedSeq.of(ss.r(8, 7), ss.r(9, 7));
	}

	public TimedSeq<Rectangle2D> createBlinkyPatchedAnimation() {
		return TimedSeq.of(ss.r(10, 7), ss.r(11, 7)).frameDuration(4).endless();
	}

	public TimedSeq<Rectangle2D> createBlinkyNakedAnimation() {
		return TimedSeq.of(ss.r(8, 8, 2, 1), ss.r(10, 8, 2, 1)).frameDuration(4).endless();
	}
}