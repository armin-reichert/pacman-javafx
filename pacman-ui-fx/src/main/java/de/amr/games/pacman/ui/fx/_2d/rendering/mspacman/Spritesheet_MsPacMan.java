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
package de.amr.games.pacman.ui.fx._2d.rendering.mspacman;

import static de.amr.games.pacman.model.common.world.World.t;

import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.animation.GenericAnimation;
import de.amr.games.pacman.lib.animation.GenericAnimationMap;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Spritesheet;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Ms. Pac-Man sprites and animations.
 * 
 * @author Armin Reichert
 */
public class Spritesheet_MsPacMan extends Spritesheet implements Rendering2D {

	private static Spritesheet_MsPacMan cmonManYouKnowTheThing;

	public static Spritesheet_MsPacMan get() {
		if (cmonManYouKnowTheThing == null) {
			cmonManYouKnowTheThing = new Spritesheet_MsPacMan();
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
	
	static final Color[] MAZE_TOP_COLORS = { 
		Color.rgb(255, 183, 174), 
		Color.rgb(71, 183, 255), 
		Color.rgb(222, 151, 81), 
		Color.rgb(33, 33, 255), 
		Color.rgb(255, 183, 255), 
		Color.rgb(255, 183, 174), 
	};

	static final Color[] MAZE_SIDE_COLORS = { 
		Color.rgb(255, 0, 0), 
		Color.rgb(222, 222, 255), 
		Color.rgb(222, 222, 255), 
		Color.rgb(255, 183, 81), 
		Color.rgb(255, 255, 0), 
		Color.rgb(255, 0, 0), 
	};

	static final Color[] FOOD_COLORS = { 
		Color.rgb(222, 222, 255), 
		Color.rgb(255, 255, 0), 
		Color.rgb(255, 0, 0),
		Color.rgb(222, 222, 255), 
		Color.rgb(0, 255, 255), 
		Color.rgb(222, 222, 255), 
	};
	//@formatter:on

	private final Image midwayLogo;
	private final Image[] mazesFull;
	private final Image[] mazesEmpty;
	private final Image[] mazesBlackWhite;
	private final Font font;

	private Spritesheet_MsPacMan() {
		super("/mspacman/graphics/sprites.png", 16, //
				Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

		font = U.font("/common/emulogic.ttf", 8);
		midwayLogo = U.image("/mspacman/graphics/midway.png");

		int numMazes = 6;
		mazesFull = new Image[numMazes];
		mazesEmpty = new Image[numMazes];
		mazesBlackWhite = new Image[numMazes];
		for (int mazeIndex = 0; mazeIndex < numMazes; ++mazeIndex) {
			mazesFull[mazeIndex] = subImage(0, 248 * mazeIndex, 226, 248);
			var empty = subImage(228, 248 * mazeIndex, 226, 248);
			mazesEmpty[mazeIndex] = empty;
			var bw = U.colorsExchanged(empty, Map.of( //
					MAZE_SIDE_COLORS[mazeIndex], Color.WHITE, //
					MAZE_TOP_COLORS[mazeIndex], Color.BLACK) //
			);
			mazesBlackWhite[mazeIndex] = bw;
		}
	}

	/**
	 * @param col column
	 * @param row row
	 * @return Sprite at given row and column from the right-hand-side of the spritesheet
	 */
	public Rectangle2D rhs(int col, int row) {
		return r(456, 0, col, row, 1, 1);
	}

	@Override
	public Image getSpriteImage(Rectangle2D sprite) {
		return subImage(sprite);
	}

	@Override
	public void drawSprite(GraphicsContext g, Rectangle2D s, double x, double y) {
		g.drawImage(source, s.getMinX(), s.getMinY(), s.getWidth(), s.getHeight(), x, y, s.getWidth(), s.getHeight());
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
	public Rectangle2D getPacSprite(Direction dir, Mouth mouth) {
		int d = dirIndex(dir);
		return switch (mouth) {
		case WIDE_OPEN -> rhs(0, d);
		case OPEN -> rhs(1, d);
		case CLOSED -> rhs(2, d);
		};
	}

	@Override
	public Rectangle2D getGhostSprite(int ghostID, Direction dir) {
		return rhs(2 * dirIndex(dir) + 1, 4 + ghostID);
	}

	@Override
	public void drawCopyright(GraphicsContext g) {
		int x = t(6);
		int y = t(28);
		g.drawImage(midwayLogo, x, y + 3, 30, 32);
		g.setFill(Color.RED);
		g.setFont(Font.font("Dialog", 11.0));
		g.fillText("\u00a9", x + t(5), y + t(2) + 2); // (c) symbol
		g.setFont(getArcadeFont());
		g.fillText("MIDWAY MFG CO", x + t(7), y + t(2));
		g.fillText("1980/1981", x + t(8), y + t(4));
	}

	@Override
	public int mazeNumber(int levelNumber) {
		return switch (levelNumber) {
		case 1, 2 -> 1;
		case 3, 4, 5 -> 2;
		case 6, 7, 8, 9 -> 3;
		case 10, 11, 12, 13 -> 4;
		default -> (levelNumber - 14) % 8 < 4 ? 5 : 6;
		};
	}

	@Override
	public GenericAnimation<Image> createMazeFlashingAnimation(int mazeNumber) {
		int mazeIndex = mazeNumber - 1;
		var mazeEmpty = subImage(228, 248 * mazeIndex, 226, 248);
		var brightImage = U.colorsExchanged(mazeEmpty, Map.of( //
				MAZE_SIDE_COLORS[mazeIndex], Color.WHITE, //
				MAZE_TOP_COLORS[mazeIndex], Color.BLACK) //
		);
		var animation = new GenericAnimation<>(brightImage, mazeEmpty);
		animation.frameDuration(10);
		return animation;
	}

	@Override
	public Image getMazeFullImage(int mazeNumber) {
		return mazesFull[mazeNumber - 1];
	}

	@Override
	public Image getMazeEmptyImage(int mazeNumber) {
		return mazesEmpty[mazeNumber - 1];
	}

	@Override
	public Color getFoodColor(int mazeNumber) {
		return FOOD_COLORS[mazeNumber - 1];
	}

	@Override
	public Rectangle2D getLifeSprite() {
		return rhs(1, 0);
	}

	/*
	 * Animations.
	 */

	@Override
	public GenericAnimationMap<Direction, Rectangle2D> createPacMunchingAnimation() {
		GenericAnimationMap<Direction, Rectangle2D> map = new GenericAnimationMap<>(Direction.class);
		for (var dir : Direction.values()) {
			int d = dirIndex(dir);
			Rectangle2D wide_open = rhs(0, d), open = rhs(1, d), closed = rhs(2, d);
			var munching = new GenericAnimation<>(open, wide_open, open, closed);
			munching.frameDuration(2);
			munching.endless();
			map.put(dir, munching);
		}
		return map;
	}

	@Override
	public GenericAnimation<Rectangle2D> createPacDyingAnimation() {
		Rectangle2D right = rhs(1, 0), left = rhs(1, 1), up = rhs(1, 2), down = rhs(1, 3);
		// TODO not yet 100% accurate
		var animation = new GenericAnimation<>(down, left, up, right, down, left, up, right, down, left, up);
		animation.frameDuration(8);
		return animation;
	}

	@Override
	public GenericAnimationMap<Direction, Rectangle2D> createGhostColorAnimation(int ghostID) {
		GenericAnimationMap<Direction, Rectangle2D> map = new GenericAnimationMap<>(Direction.class);
		for (var dir : Direction.values()) {
			int d = dirIndex(dir);
			var feet = new GenericAnimation<>(rhs(2 * d, 4 + ghostID), rhs(2 * d + 1, 4 + ghostID));
			feet.frameDuration(8);
			feet.endless();
			map.put(dir, feet);
		}
		return map;
	}

	@Override
	public GenericAnimation<Rectangle2D> createGhostBlueAnimation() {
		var animation = new GenericAnimation<>(rhs(8, 4), rhs(9, 4));
		animation.frameDuration(8);
		animation.endless();
		return animation;
	}

	@Override
	public GenericAnimation<Rectangle2D> createGhostFlashingAnimation() {
		var animation = new GenericAnimation<>(rhs(8, 4), rhs(9, 4), rhs(10, 4), rhs(11, 4));
		animation.frameDuration(4);
		return animation;
	}

	@Override
	public GenericAnimationMap<Direction, Rectangle2D> createGhostEyesAnimation() {
		GenericAnimationMap<Direction, Rectangle2D> map = new GenericAnimationMap<>(Direction.class);
		for (var dir : Direction.values()) {
			int d = dirIndex(dir);
			map.put(dir, new GenericAnimation<>(rhs(8 + d, 5)));
		}
		return map;
	}

	@Override
	public GenericAnimation<Rectangle2D> createBonusSymbolAnimation() {
		return new GenericAnimation<>(rhs(3, 0), rhs(4, 0), rhs(5, 0), rhs(6, 0), rhs(7, 0), rhs(8, 0), rhs(9, 0));
	}

	@Override
	public GenericAnimation<Rectangle2D> createBonusValueAnimation() {
		return new GenericAnimation<>(rhs(3, 1), rhs(4, 1), rhs(5, 1), rhs(6, 1), rhs(7, 1), rhs(8, 1), rhs(9, 1));
	}

	@Override
	public GenericAnimation<Rectangle2D> createGhostValueAnimation() {
		return new GenericAnimation<>(rhs(0, 8), rhs(1, 8), rhs(2, 8), rhs(3, 8));
	}

	// Ms. Pac-Man specific:

	public GenericAnimationMap<Direction, Rectangle2D> createHusbandMunchingAnimations() {
		GenericAnimationMap<Direction, Rectangle2D> map = new GenericAnimationMap<>(Direction.class);
		for (var dir : Direction.values()) {
			int d = dirIndex(dir);
			var animation = new GenericAnimation<>(rhs(0, 9 + d), rhs(1, 9 + d), rhs(2, 9));
			animation.frameDuration(2);
			animation.endless();
			map.put(dir, animation);
		}
		return map;
	}

	public GenericAnimation<Rectangle2D> createFlapAnimation() {
		var animation = new GenericAnimation<>( //
				new Rectangle2D(456, 208, 32, 32), //
				new Rectangle2D(488, 208, 32, 32), //
				new Rectangle2D(520, 208, 32, 32), //
				new Rectangle2D(488, 208, 32, 32), //
				new Rectangle2D(456, 208, 32, 32)//
		);
		animation.frameDuration(4);
		return animation;
	}

	public GenericAnimation<Rectangle2D> createStorkFlyingAnimation() {
		var animation = new GenericAnimation<>( //
				new Rectangle2D(489, 176, 32, 16), //
				new Rectangle2D(521, 176, 32, 16) //
		);
		animation.endless();
		animation.frameDuration(8);
		return animation;
	}

	public Rectangle2D getHeart() {
		return rhs(2, 10);
	}

	public Rectangle2D getJunior() {
		return new Rectangle2D(509, 200, 8, 8);
	}

	public Rectangle2D getBlueBag() {
		return new Rectangle2D(488, 199, 8, 8);
	}
}