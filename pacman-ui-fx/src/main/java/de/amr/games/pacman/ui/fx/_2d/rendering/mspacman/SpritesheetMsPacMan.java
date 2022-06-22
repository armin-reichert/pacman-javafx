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

import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.animation.SingleSpriteAnimation;
import de.amr.games.pacman.lib.animation.SpriteAnimationMap;
import de.amr.games.pacman.lib.animation.SpriteArray;
import de.amr.games.pacman.model.mspacman.Flap;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Spritesheet;
import de.amr.games.pacman.ui.fx.util.Ufx;
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
public class SpritesheetMsPacMan extends Spritesheet implements Rendering2D {

	private static SpritesheetMsPacMan cmonManYouKnowTheThing;

	public static SpritesheetMsPacMan get() {
		if (cmonManYouKnowTheThing == null) {
			cmonManYouKnowTheThing = new SpritesheetMsPacMan();
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
	private final Image[] mazesEmptyBW;
	private final Font font;

	private SpritesheetMsPacMan() {
		super("/mspacman/graphics/sprites.png", 16, Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

		font = Ufx.font("/common/emulogic.ttf", 8);
		midwayLogo = Ufx.image("/mspacman/graphics/midway.png");

		int numMazes = 6;
		mazesFull = new Image[numMazes];
		mazesEmpty = new Image[numMazes];
		mazesEmptyBW = new Image[numMazes];
		for (int i = 0; i < numMazes; ++i) {
			mazesFull[i] = subImage(0, 248 * i, 226, 248);
			mazesEmpty[i] = subImage(228, 248 * i, 226, 248);
			mazesEmptyBW[i] = Ufx.colorsExchanged(mazesEmpty[i], //
					Map.of(MAZE_SIDE_COLORS[i], Color.WHITE, MAZE_TOP_COLORS[i], Color.BLACK));
		}
	}

	@Override
	public Image source() {
		return source;
	}

	/**
	 * @param col column
	 * @param row row
	 * @return Sprite at given row and column from the right-hand-side of the spritesheet
	 */
	public Rectangle2D rhs(int col, int row) {
		return tilesAtOrigin(456, 0, col, row, 1, 1);
	}

	@Override
	public Image getSpriteImage(Rectangle2D sprite) {
		return subImage(sprite);
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
	public Color getFoodColor(int mazeNumber) {
		return FOOD_COLORS[mazeNumber - 1];
	}

	@Override
	public Rectangle2D getGhostSprite(int ghostID, Direction dir) {
		return rhs(2 * dirIndex(dir) + 1, 4 + ghostID);
	}

	@Override
	public Rectangle2D getBonusSymbolSprite(int symbol) {
		return rhs(3 + symbol, 0);
	}

	@Override
	public Rectangle2D getBonusValueSprite(int symbol) {
		return rhs(3 + symbol, 1);
	}

	@Override
	public Image getMazeFullImage(int mazeNumber) {
		return mazesFull[mazeNumber - 1];
	}

	@Override
	public Rectangle2D getLifeSprite() {
		return rhs(1, 0);
	}

	public Rectangle2D getHeart() {
		return rhs(2, 10);
	}

	public Rectangle2D getBlueBag() {
		return new Rectangle2D(488, 199, 8, 8);
	}

	public Rectangle2D getJunior() {
		return new Rectangle2D(509, 200, 8, 8);
	}

	@Override
	public void drawCopyright(GraphicsContext g, int tileY) {
		int x = t(6);
		int y = t(tileY - 1);
		g.drawImage(midwayLogo, x, y + 2, t(4) - 2, t(4));
		g.setFill(Color.RED);
		g.setFont(Font.font("Dialog", 11));
		g.fillText("\u00a9", x + t(5), y + t(2) + 2); // (c) symbol
		g.setFont(getArcadeFont());
		g.fillText("MIDWAY MFG CO", x + t(7), y + t(2));
		g.fillText("1980/1981", x + t(8), y + t(4));
	}

	public void drawFlap(GraphicsContext g, Flap flap) {
		if (flap.visible) {
			Rectangle2D sprite = (Rectangle2D) flap.animation.animate();
			drawEntity(g, flap, sprite);
			g.setFont(getArcadeFont());
			g.setFill(Color.rgb(222, 222, 255));
			g.fillText(String.valueOf(flap.number), flap.position.x + sprite.getWidth() - 25, flap.position.y + 18);
			g.fillText(flap.text, flap.position.x + sprite.getWidth(), flap.position.y);
		}
	}

	@Override
	public SingleSpriteAnimation<Image> createMazeFlashingAnimation(int mazeNumber) {
		int mazeIndex = mazeNumber - 1;
		var mazeEmpty = subImage(228, 248 * mazeIndex, 226, 248);
		var brightImage = Ufx.colorsExchanged(mazeEmpty, Map.of( //
				MAZE_SIDE_COLORS[mazeIndex], Color.WHITE, //
				MAZE_TOP_COLORS[mazeIndex], Color.BLACK) //
		);
		var animation = new SingleSpriteAnimation<>(brightImage, mazeEmpty);
		animation.frameDuration(10);
		return animation;
	}

	@Override
	public SpriteAnimationMap<Direction, Rectangle2D> createPacMunchingAnimationMap() {
		var enumMap = new EnumMap<Direction, SingleSpriteAnimation<Rectangle2D>>(Direction.class);
		var animationByDir = new SpriteAnimationMap<>(enumMap);
		for (var dir : Direction.values()) {
			int d = dirIndex(dir);
			var wideOpen = rhs(0, d);
			var open = rhs(1, d);
			var closed = rhs(2, d);
			var munching = new SingleSpriteAnimation<>(open, wideOpen, open, closed);
			munching.frameDuration(2);
			munching.repeatForever();
			animationByDir.put(dir, munching);
		}
		return animationByDir;
	}

	@Override
	public SingleSpriteAnimation<Rectangle2D> createPacDyingAnimation() {
		var right = rhs(1, 0);
		var left = rhs(1, 1);
		var up = rhs(1, 2);
		var down = rhs(1, 3);
		// TODO not yet 100% accurate
		var animation = new SingleSpriteAnimation<>(down, left, up, right, down, left, up, right, down, left, up);
		animation.frameDuration(8);
		return animation;
	}

	@Override
	public SpriteAnimationMap<Direction, Rectangle2D> createGhostColorAnimationMap(int ghostID) {
		var enumMap = new EnumMap<Direction, SingleSpriteAnimation<Rectangle2D>>(Direction.class);
		var animationByDir = new SpriteAnimationMap<>(enumMap);
		for (var dir : Direction.values()) {
			int d = dirIndex(dir);
			var animation = new SingleSpriteAnimation<>(rhs(2 * d, 4 + ghostID), rhs(2 * d + 1, 4 + ghostID));
			animation.frameDuration(8);
			animation.repeatForever();
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	@Override
	public SingleSpriteAnimation<Rectangle2D> createGhostBlueAnimation() {
		var animation = new SingleSpriteAnimation<>(rhs(8, 4), rhs(9, 4));
		animation.frameDuration(8);
		animation.repeatForever();
		return animation;
	}

	@Override
	public SingleSpriteAnimation<Rectangle2D> createGhostFlashingAnimation() {
		var animation = new SingleSpriteAnimation<>(rhs(8, 4), rhs(9, 4), rhs(10, 4), rhs(11, 4));
		animation.frameDuration(4);
		return animation;
	}

	@Override
	public SpriteAnimationMap<Direction, Rectangle2D> createGhostEyesAnimationMap() {
		var enumMap = new EnumMap<Direction, SingleSpriteAnimation<Rectangle2D>>(Direction.class);
		var animationByDir = new SpriteAnimationMap<>(enumMap);
		for (var dir : Direction.values()) {
			int d = dirIndex(dir);
			animationByDir.put(dir, new SingleSpriteAnimation<>(rhs(8 + d, 5)));
		}
		return animationByDir;
	}

	@Override
	public SpriteArray<Rectangle2D> createGhostValueList() {
		return new SpriteArray<>(rhs(0, 8), rhs(1, 8), rhs(2, 8), rhs(3, 8));
	}

	// Ms. Pac-Man specific:

	public SpriteAnimationMap<Direction, Rectangle2D> createPacManMunchingAnimationMap() {
		var animationByDir = new SpriteAnimationMap<Direction, Rectangle2D>(4);
		for (var dir : Direction.values()) {
			int d = dirIndex(dir);
			var animation = new SingleSpriteAnimation<>(rhs(0, 9 + d), rhs(1, 9 + d), rhs(2, 9));
			animation.frameDuration(2);
			animation.repeatForever();
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	public SingleSpriteAnimation<Rectangle2D> createFlapAnimation() {
		// TODO this is not 100% accurate yet
		var animation = new SingleSpriteAnimation<>( //
				new Rectangle2D(456, 208, 32, 32), //
				new Rectangle2D(488, 208, 32, 32), //
				new Rectangle2D(520, 208, 32, 32), //
				new Rectangle2D(488, 208, 32, 32), //
				new Rectangle2D(456, 208, 32, 32)//
		);
		animation.frameDuration(4);
		return animation;
	}

	public SingleSpriteAnimation<Rectangle2D> createStorkFlyingAnimation() {
		var animation = new SingleSpriteAnimation<>( //
				new Rectangle2D(489, 176, 32, 16), //
				new Rectangle2D(521, 176, 32, 16) //
		);
		animation.repeatForever();
		animation.frameDuration(8);
		return animation;
	}
}