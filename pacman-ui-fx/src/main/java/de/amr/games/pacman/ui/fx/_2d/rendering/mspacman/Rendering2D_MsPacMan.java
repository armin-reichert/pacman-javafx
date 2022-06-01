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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSeq;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpriteAnimation;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Spritesheet;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Ms. Pac-Man game-specific rendering.
 * 
 * @author Armin Reichert
 */
public class Rendering2D_MsPacMan implements Rendering2D {

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

	private static final Color[] FOOD_COLORS = { 
		Color.rgb(222, 222, 255), 
		Color.rgb(255, 255, 0), 
		Color.rgb(255, 0, 0),
		Color.rgb(222, 222, 255), 
		Color.rgb(0, 255, 255), 
		Color.rgb(222, 222, 255), 
	};
	//@formatter:on

	private static Rendering2D_MsPacMan it;

	public static Rendering2D_MsPacMan get() {
		if (it == null) {
			it = new Rendering2D_MsPacMan("/mspacman/graphics/sprites.png", 16, Direction.RIGHT, Direction.LEFT, Direction.UP,
					Direction.DOWN);
		}
		return it;
	}

	private final Spritesheet ss;
	private final Image midwayLogo;
	private final List<Rectangle2D> mazeFullSprites;
	private final List<Rectangle2D> mazeEmptySprites;
	private final List<Image> mazeFlashImages;
	private final Map<Integer, Rectangle2D> bonusValueSprites;
	private final Map<Integer, Rectangle2D> symbolSprites;
	private final Map<Integer, Rectangle2D> bountyNumberSprites;
	private final Font font;

	/**
	 * @param col column
	 * @param row row
	 * @return Sprite at given row and column from the right-hand-side of the spritesheet
	 */
	public Rectangle2D rhs(int col, int row) {
		return ss.r(456, 0, col, row, 1, 1);
	}

	private Rendering2D_MsPacMan(String path, int rasterSize, Direction... dirOrder) {
		ss = new Spritesheet(path, rasterSize, dirOrder);
		font = U.font("/common/emulogic.ttf", 8);
		midwayLogo = U.image("/mspacman/graphics/midway.png");

		//@formatter:off
		symbolSprites = Map.of(
			MsPacManGame.CHERRIES,   rhs(3,0),
			MsPacManGame.STRAWBERRY, rhs(4,0),
			MsPacManGame.PEACH,      rhs(5,0),
			MsPacManGame.PRETZEL,    rhs(6,0),
			MsPacManGame.APPLE,      rhs(7,0),
			MsPacManGame.PEAR,       rhs(8,0),
			MsPacManGame.BANANA,     rhs(9,0)
		);

		bonusValueSprites = Map.of(
			 100, rhs(3, 1), 
			 200, rhs(4, 1), 
			 500, rhs(5, 1), 
			 700, rhs(6, 1), 
			1000, rhs(7, 1), 
			2000, rhs(8, 1),
			5000, rhs(9, 1)
		);

		bountyNumberSprites = Map.of(
			 200, rhs(0, 8), 
			 400, rhs(1, 8), 
			 800, rhs(2, 8), 
			1600, rhs(3, 8)
		);
		//@formatter:on

		final int numMazes = 6;
		mazeFullSprites = new ArrayList<>(numMazes);
		mazeEmptySprites = new ArrayList<>(numMazes);
		mazeFlashImages = new ArrayList<>(numMazes);
		for (int mazeIndex = 0; mazeIndex < numMazes; ++mazeIndex) {
			Rectangle2D mazeFullRegion = new Rectangle2D(0, 248 * mazeIndex, 226, 248);
			Rectangle2D mazeEmptyRegion = new Rectangle2D(226, 248 * mazeIndex, 226, 248);
			Image mazeFlashImage = U.colorsExchanged(ss.extractRegion(mazeEmptyRegion), Map.of( //
					MAZE_SIDE_COLORS[mazeIndex], Color.WHITE, //
					MAZE_TOP_COLORS[mazeIndex], Color.BLACK) //
			);
			mazeFullSprites.add(mazeFullRegion);
			mazeEmptySprites.add(mazeEmptyRegion);
			mazeFlashImages.add(mazeFlashImage);
		}
	}

	@Override
	public Spritesheet spritesheet() {
		return ss;
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
	public void drawCopyright(GraphicsContext g, int x, int y) {
		// x=t(4), y=t(28)
		double scale = ArcadeWorld.TILES_Y / midwayLogo.getHeight();
		g.drawImage(midwayLogo, x, y + 3, scale * midwayLogo.getWidth(), scale * midwayLogo.getHeight());
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
	public void drawMazeFull(GraphicsContext g, int mazeNumber, double x, double y) {
		drawSprite(g, mazeFullSprites.get(mazeNumber - 1), x, y);
	}

	@Override
	public void drawMazeEmpty(GraphicsContext g, int mazeNumber, double x, double y) {
		drawSprite(g, mazeEmptySprites.get(mazeNumber - 1), x, y);
	}

	@Override
	public void drawMazeBright(GraphicsContext g, int mazeNumber, double x, double y) {
		g.drawImage(mazeFlashImages.get(mazeNumber - 1), x, y);
	}

	@Override
	public Color getFoodColor(int mazeNumber) {
		return FOOD_COLORS[mazeNumber - 1];
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

	/*
	 * Animations.
	 */

	@Override
	public Map<Direction, SpriteAnimation> createPlayerMunchingAnimations() {
		Map<Direction, SpriteAnimation> animations = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = ss.dirIndex(dir);
			Rectangle2D wide_open = rhs(0, d), open = rhs(1, d), closed = rhs(2, d);
			SpriteAnimation munching = SpriteAnimation.of(open, wide_open, open, closed).frameDuration(2).endless();
			animations.put(dir, munching);
		}
		return animations;
	}

	@Override
	public SpriteAnimation createPlayerDyingAnimation() {
		Rectangle2D right = rhs(1, 0), left = rhs(1, 1), up = rhs(1, 2), down = rhs(1, 3);
		// TODO not yet 100% accurate
		return SpriteAnimation.of(down, left, up, right, down, left, up, right, down, left, up).frameDuration(8);
	}

	@Override
	public Map<Direction, SpriteAnimation> createGhostKickingAnimations(int ghostID) {
		EnumMap<Direction, SpriteAnimation> animations = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = ss.dirIndex(dir);
			SpriteAnimation kicking = SpriteAnimation.of(rhs(2 * d, 4 + ghostID), rhs(2 * d + 1, 4 + ghostID))
					.frameDuration(8).endless();
			animations.put(dir, kicking);
		}
		return animations;
	}

	@Override
	public SpriteAnimation createGhostFrightenedAnimation() {
		return SpriteAnimation.of(rhs(8, 4), rhs(9, 4)).frameDuration(8).endless();
	}

	@Override
	public SpriteAnimation createGhostFlashingAnimation() {
		return SpriteAnimation.of(rhs(8, 4), rhs(9, 4), rhs(10, 4), rhs(11, 4)).frameDuration(4);
	}

	@Override
	public Map<Direction, SpriteAnimation> createGhostReturningHomeAnimations() {
		Map<Direction, SpriteAnimation> animations = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = ss.dirIndex(dir);
			animations.put(dir, SpriteAnimation.of(rhs(8 + d, 5)));
		}
		return animations;
	}

	@Override
	public Rectangle2D getLifeSprite() {
		return rhs(1, 0);
	}

	// Ms. Pac-Man specific:

	public TimedSeq<Integer> createBonusJumpAnimation() {
		return TimedSeq.of(2, 0, -2).frameDuration(8).endless();
	}

	public Map<Direction, SpriteAnimation> createHusbandMunchingAnimations() {
		Map<Direction, SpriteAnimation> animations = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = ss.dirIndex(dir);
			animations.put(dir, SpriteAnimation.of(rhs(0, 9 + d), rhs(1, 9 + d), rhs(2, 9)).frameDuration(2).endless());
		}
		return animations;
	}

	public SpriteAnimation createFlapAnimation() {
		return SpriteAnimation.of( //
				new Rectangle2D(456, 208, 32, 32), //
				new Rectangle2D(488, 208, 32, 32), //
				new Rectangle2D(520, 208, 32, 32), //
				new Rectangle2D(488, 208, 32, 32), //
				new Rectangle2D(456, 208, 32, 32)//
		).repetitions(1).frameDuration(4);
	}

	public SpriteAnimation createStorkFlyingAnimation() {
		return SpriteAnimation.of( //
				new Rectangle2D(489, 176, 32, 16), //
				new Rectangle2D(521, 176, 32, 16) //
		).endless().frameDuration(8);
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