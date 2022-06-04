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

import static de.amr.games.pacman.model.common.actors.Ghost.PINK_GHOST;

import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.SpriteAnimation;
import de.amr.games.pacman.lib.SpriteAnimationMap;
import de.amr.games.pacman.model.pacman.PacManGame;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Spritesheet;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Pac-Man game spritesheet renderer.
 * 
 * @author Armin Reichert
 */
public class Spritesheet_PacMan extends Spritesheet implements Rendering2D {

	private static Spritesheet_PacMan cmonManYouKnowTheThing;

	public static Spritesheet_PacMan get() {
		if (cmonManYouKnowTheThing == null) {
			cmonManYouKnowTheThing = new Spritesheet_PacMan();
		}
		return cmonManYouKnowTheThing;
	}

	//@formatter:off
	static final Color[] GHOST_COLORS = {
		Color.RED,
		Color.rgb(252, 181, 255),
		Color.CYAN,
		Color.rgb(253, 192, 90)
	};
	//@formatter:on

	private static final Color MAZE_WALL_COLOR = Color.rgb(33, 33, 255);
	private static final Color FOOD_COLOR = Color.rgb(254, 189, 180);

	private final Image mazeFull, mazeEmpty, mazeFlashing;
	private final Map<Integer, Rectangle2D> symbolSprites;
	private final Map<Integer, Rectangle2D> bonusValueSprites;
	private final Map<Integer, Rectangle2D> bountyNumberSprites;
	private final Font font;

	private Spritesheet_PacMan() {
		super("/pacman/graphics/sprites.png", 16, Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

		font = U.font("/common/emulogic.ttf", 8);

		mazeFull = U.image("/pacman/graphics/maze_full.png");
		mazeEmpty = U.image("/pacman/graphics/maze_empty.png");
		mazeFlashing = U.colorsExchanged(mazeEmpty, Map.of(MAZE_WALL_COLOR, Color.WHITE));

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
	public Spritesheet getSpritesheet() {
		return this;
	}

	@Override
	public Font getArcadeFont() {
		return font;
	}

	@Override
	public Color getGhostColor(int ghostID) {
		return GHOST_COLORS[ghostID];
	}

	@Override
	public int mazeNumber(int levelNumber) {
		return 1;
	}

	@Override
	public SpriteAnimation<Image> createMazeFlashingAnimation(int mazeNumber) {
		var brightImage = U.colorsExchanged(mazeEmpty, Map.of(MAZE_WALL_COLOR, Color.WHITE));
		return new SpriteAnimation<>(brightImage, mazeEmpty).frameDuration(15);
	}

	@Override
	public void drawMazeFull(GraphicsContext g, int mazeNumber, double x, double y) {
		g.drawImage(mazeFull, x, y);
	}

	@Override
	public void drawMazeEmpty(GraphicsContext g, int mazeNumber, double x, double y) {
		g.drawImage(mazeEmpty, x, y);
	}

	@Override
	public void drawMazeBright(GraphicsContext g, int mazeNumber, double x, double y) {
		g.drawImage(mazeFlashing, x, y);
	}

	@Override
	public void drawCopyright(GraphicsContext g, int x, int y) {
		// t(3), t(32)
		String text = "\u00A9 1980 MIDWAY MFG. CO.";
		g.setFont(getArcadeFont());
		g.setFill(getGhostColor(PINK_GHOST));
		g.fillText(text, x, y);
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
	public Rectangle2D getBonusValueSprite(int number) {
		return bonusValueSprites.get(number);
	}

	@Override
	public Rectangle2D getNumberSprite(int number) {
		return bountyNumberSprites.get(number);
	}

	@Override
	public Rectangle2D getSymbolSprite(int symbol) {
		return symbolSprites.get(symbol);
	}

	@Override
	public SpriteAnimationMap<Direction, Rectangle2D> createPacMunchingAnimations() {
		SpriteAnimationMap<Direction, Rectangle2D> map = new SpriteAnimationMap<>(Direction.class);
		for (var dir : Direction.values()) {
			int d = dirOrder(dir);
			Rectangle2D wide_open = r(0, d), open = r(1, d), closed = r(2, 0);
			SpriteAnimation<Rectangle2D> animation = new SpriteAnimation<>(closed, open, wide_open, open).frameDuration(2)
					.endless();
			map.put(dir, animation);
		}
		return map;
	}

	@Override
	public SpriteAnimation<Rectangle2D> createPacDyingAnimation() {
		return new SpriteAnimation<>(r(3, 0), r(4, 0), r(5, 0), r(6, 0), r(7, 0), r(8, 0), r(9, 0), r(10, 0), r(11, 0),
				r(12, 0), r(13, 0)).frameDuration(8);
	}

	@Override
	public SpriteAnimationMap<Direction, Rectangle2D> createGhostColorAnimation(int ghostID) {
		SpriteAnimationMap<Direction, Rectangle2D> map = new SpriteAnimationMap<>(Direction.class);
		for (var dir : Direction.values()) {
			int d = dirOrder(dir);
			var animation = new SpriteAnimation<>(r(2 * d, 4 + ghostID), r(2 * d + 1, 4 + ghostID)).frameDuration(8)
					.endless();
			map.put(dir, animation);
		}
		return map;
	}

	@Override
	public SpriteAnimation<Rectangle2D> createGhostBlueAnimation() {
		return new SpriteAnimation<>(r(8, 4), r(9, 4)).frameDuration(8).endless();
	}

	@Override
	public SpriteAnimation<Rectangle2D> createGhostFlashingAnimation() {
		return new SpriteAnimation<>(r(8, 4), r(9, 4), r(10, 4), r(11, 4)).frameDuration(6);
	}

	@Override
	public SpriteAnimationMap<Direction, Rectangle2D> createGhostEyesAnimation() {
		SpriteAnimationMap<Direction, Rectangle2D> map = new SpriteAnimationMap<>(Direction.class);
		for (var dir : Direction.values()) {
			int d = dirOrder(dir);
			map.put(dir, new SpriteAnimation<>(r(8 + d, 5)));
		}
		return map;
	}

	// Pac-Man specific:

	public Rectangle2D getNail() {
		return r(8, 6);
	}

	public SpriteAnimation<Rectangle2D> createBigPacManMunchingAnimation() {
		return new SpriteAnimation<>(r(2, 1, 2, 2), r(4, 1, 2, 2), r(6, 1, 2, 2)).frameDuration(4).endless();
	}

	public SpriteAnimation<Rectangle2D> createBlinkyStretchedAnimation() {
		return new SpriteAnimation<>(r(9, 6), r(10, 6), r(11, 6), r(12, 6));
	}

	public SpriteAnimation<Rectangle2D> createBlinkyDamagedAnimation() {
		return new SpriteAnimation<>(r(8, 7), r(9, 7));
	}

	public SpriteAnimation<Rectangle2D> createBlinkyPatchedAnimation() {
		return new SpriteAnimation<>(r(10, 7), r(11, 7)).frameDuration(4).endless();
	}

	public SpriteAnimation<Rectangle2D> createBlinkyNakedAnimation() {
		return new SpriteAnimation<>(r(8, 8, 2, 1), r(10, 8, 2, 1)).frameDuration(4).endless();
	}
}