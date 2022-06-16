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
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.animation.SimpleAnimation;
import de.amr.games.pacman.lib.animation.AnimationMap;
import de.amr.games.pacman.lib.animation.SpriteArray;
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

	private final Image mazeFull, mazeEmpty;
	private final Font font;

	private Spritesheet_PacMan() {
		super("/pacman/graphics/sprites.png", 16, Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);
		font = U.font("/common/emulogic.ttf", 8);
		mazeFull = U.image("/pacman/graphics/maze_full.png");
		mazeEmpty = U.image("/pacman/graphics/maze_empty.png");
	}

	@Override
	public Image source() {
		return source;
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
	public Rectangle2D getGhostSprite(int ghostID, Direction dir) {
		return tile(2 * dirIndex(dir), 4 + ghostID);
	}

	@Override
	public Rectangle2D getPacSprite(Direction dir, Mouth mouth) {
		return switch (mouth) {
		case CLOSED -> tile(2, 0);
		case OPEN -> tile(1, dirIndex(dir));
		case WIDE_OPEN -> tile(0, dirIndex(dir));
		};
	}

	@Override
	public Rectangle2D getBonusSymbolSprite(int symbol) {
		return tile(2 + symbol, 3);
	}

	@Override
	public Rectangle2D getBonusValueSprite(int symbol) {
		return symbol <= 3 ? tile(symbol, 9) : symbol == 4 ? tiles(4, 9, 2, 1) : tiles(3, symbol, 3, 1);
	}

	@Override
	public int mazeNumber(int levelNumber) {
		return 1;
	}

	@Override
	public SimpleAnimation<Image> createMazeFlashingAnimation(int mazeNumber) {
		var brightImage = U.colorsExchanged(mazeEmpty, Map.of(MAZE_WALL_COLOR, Color.WHITE));
		var animation = new SimpleAnimation<>(brightImage, mazeEmpty);
		animation.frameDuration(10);
		return animation;
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
	public void drawCopyright(GraphicsContext g, int tileY) {
		String text = "\u00A9 1980 MIDWAY MFG. CO.";
		g.setFont(getArcadeFont());
		g.setFill(getGhostColor(PINK_GHOST));
		g.fillText(text, t(4), t(tileY));
	}

	@Override
	public Color getFoodColor(int mazeNumber) {
		return FOOD_COLOR;
	}

	@Override
	public Rectangle2D getLifeSprite() {
		return tile(8, 1);
	}

	@Override
	public AnimationMap<Direction, Rectangle2D> createPacMunchingAnimationMap() {
		var enumMap = new EnumMap<Direction, SimpleAnimation<Rectangle2D>>(Direction.class);
		AnimationMap<Direction, Rectangle2D> map = new AnimationMap<>(enumMap);
		for (var dir : Direction.values()) {
			int d = dirIndex(dir);
			Rectangle2D wide_open = tile(0, d), open = tile(1, d), closed = tile(2, 0);
			var animation = new SimpleAnimation<>(closed, open, wide_open, open);
			animation.frameDuration(2);
			animation.repeatForever();
			map.put(dir, animation);
		}
		return map;
	}

	@Override
	public SimpleAnimation<Rectangle2D> createPacDyingAnimation() {
		var animation = new SimpleAnimation<>(tilesToRight(3, 0, 11));
		animation.frameDuration(8);
		return animation;
	}

	@Override
	public AnimationMap<Direction, Rectangle2D> createGhostColorAnimationMap(int ghostID) {
		var enumMap = new EnumMap<Direction, SimpleAnimation<Rectangle2D>>(Direction.class);
		AnimationMap<Direction, Rectangle2D> map = new AnimationMap<>(enumMap);
		for (var dir : Direction.values()) {
			int d = dirIndex(dir);
			var animation = new SimpleAnimation<>(tilesToRight(2 * d, 4 + ghostID, 2));
			animation.frameDuration(8);
			animation.repeatForever();
			map.put(dir, animation);
		}
		return map;
	}

	@Override
	public SimpleAnimation<Rectangle2D> createGhostBlueAnimation() {
		var animation = new SimpleAnimation<>(tile(8, 4), tile(9, 4));
		animation.frameDuration(8);
		animation.repeatForever();
		return animation;
	}

	@Override
	public SimpleAnimation<Rectangle2D> createGhostFlashingAnimation() {
		var animation = new SimpleAnimation<>(tilesToRight(8, 4, 4));
		animation.frameDuration(6);
		return animation;
	}

	@Override
	public AnimationMap<Direction, Rectangle2D> createGhostEyesAnimationMap() {
		var enumMap = new EnumMap<Direction, SimpleAnimation<Rectangle2D>>(Direction.class);
		AnimationMap<Direction, Rectangle2D> map = new AnimationMap<>(enumMap);
		for (var dir : Direction.values()) {
			int d = dirIndex(dir);
			map.put(dir, new SimpleAnimation<>(tile(8 + d, 5)));
		}
		return map;
	}

	@Override
	public SpriteArray<Rectangle2D> createGhostValueList() {
		return new SpriteArray<>(tilesToRight(0, 8, 4));
	}

	// Pac-Man specific:

	public Rectangle2D getNail() {
		return tile(8, 6);
	}

	public SimpleAnimation<Rectangle2D> createBigPacManMunchingAnimation() {
		var animation = new SimpleAnimation<>(tiles(2, 1, 2, 2), tiles(4, 1, 2, 2), tiles(6, 1, 2, 2));
		animation.frameDuration(3);
		animation.repeatForever();
		return animation;
	}

	public SpriteArray<Rectangle2D> createBlinkyStretchedAnimation() {
		return new SpriteArray<>(tilesToRight(8, 6, 5));
	}

	public SpriteArray<Rectangle2D> createBlinkyDamagedAnimation() {
		return new SpriteArray<>(tile(8, 7), tile(9, 7));
	}

	public SimpleAnimation<Rectangle2D> createBlinkyPatchedAnimation() {
		var animation = new SimpleAnimation<>(tile(10, 7), tile(11, 7));
		animation.frameDuration(4);
		animation.repeatForever();
		return animation;
	}

	public SimpleAnimation<Rectangle2D> createBlinkyNakedAnimation() {
		var animation = new SimpleAnimation<>(tiles(8, 8, 2, 1), tiles(10, 8, 2, 1));
		animation.frameDuration(4);
		animation.repeatForever();
		return animation;
	}
}