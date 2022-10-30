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
package de.amr.games.pacman.ui.fx._2d.rendering;

import static de.amr.games.pacman.model.common.actors.Ghost.PINK_GHOST;
import static de.amr.games.pacman.model.common.world.World.t;

import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.animation.EntityAnimationByDirection;
import de.amr.games.pacman.lib.animation.FixedEntityAnimation;
import de.amr.games.pacman.lib.animation.SingleEntityAnimation;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class RendererPacManGame extends RendererCommon {

	private static final Color MAZE_WALL_COLOR = Color.rgb(33, 33, 255);
	private static final Color FOOD_COLOR = Color.rgb(254, 189, 180);

	private static final Spritesheet SPRITESHEET = new Spritesheet("graphics/pacman/sprites.png", 16, Direction.RIGHT,
			Direction.LEFT, Direction.UP, Direction.DOWN);

	private static final Image MAZE_FULL = Ufx.image("graphics/pacman/maze_full.png");
	private static final Image MAZE_EMPTY = Ufx.image("graphics/pacman/maze_empty.png");
	private static final Image MAZE_EMPTY_BW = Ufx.colorsExchanged(MAZE_EMPTY, Map.of(MAZE_WALL_COLOR, Color.WHITE));

	@Override
	public Spritesheet getSpritesheet() {
		return SPRITESHEET;
	}

	@Override
	public Rectangle2D getGhostSprite(int ghostID, Direction dir) {
		return getSpritesheet().tile(2 * getSpritesheet().dirIndex(dir), 4 + ghostID);
	}

	@Override
	public Rectangle2D getBonusSymbolSprite(int symbol) {
		return getSpritesheet().tile(2 + symbol, 3);
	}

	@Override
	public Rectangle2D getBonusValueSprite(int symbol) {
		if (symbol <= 3) {
			return getSpritesheet().tile(symbol, 9);
		}
		if (symbol == 4) {
			var region = getSpritesheet().tiles(4, 9, 3, 1);
			return new Rectangle2D(region.getMinX(), region.getMinY(), region.getWidth() - 13, region.getHeight()); // WTF
		}
		return getSpritesheet().tiles(3, 5 + symbol, 3, 1);
	}

	@Override
	public SingleEntityAnimation<Image> createMazeFlashingAnimation(int mazeNumber) {
		var animation = new SingleEntityAnimation<>(MAZE_EMPTY_BW, MAZE_EMPTY);
		animation.setFrameDuration(10);
		return animation;
	}

	@Override
	public Image getMazeFullImage(int mazeNumber) {
		return MAZE_FULL;
	}

	@Override
	public void drawCopyright(GraphicsContext g, int tileY) {
		String text = "\u00A9 1980 MIDWAY MFG.CO.";
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
		return getSpritesheet().tile(8, 1);
	}

	@Override
	public EntityAnimationByDirection createPacMunchingAnimationMap(Pac pac) {
		var animationByDir = new EntityAnimationByDirection(pac::moveDir);
		for (var dir : Direction.values()) {
			int d = getSpritesheet().dirIndex(dir);
			var wide = getSpritesheet().tile(0, d);
			var middle = getSpritesheet().tile(1, d);
			var closed = getSpritesheet().tile(2, 0);
			var animation = new SingleEntityAnimation<>(closed, closed, middle, middle, wide, wide, middle, middle, closed);
			animation.setFrameDuration(1);
			animation.repeatForever();
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	@Override
	public SingleEntityAnimation<Rectangle2D> createPacDyingAnimation() {
		var animation = new SingleEntityAnimation<>(getSpritesheet().tilesToRight(3, 0, 11));
		animation.setFrameDuration(8);
		return animation;
	}

	@Override
	public EntityAnimationByDirection createGhostColorAnimationMap(Ghost ghost) {
		var animationByDir = new EntityAnimationByDirection(ghost::wishDir);
		for (var dir : Direction.values()) {
			int d = getSpritesheet().dirIndex(dir);
			var animation = new SingleEntityAnimation<>(getSpritesheet().tilesToRight(2 * d, 4 + ghost.id, 2));
			animation.setFrameDuration(8);
			animation.repeatForever();
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	@Override
	public SingleEntityAnimation<Rectangle2D> createGhostBlueAnimation() {
		var animation = new SingleEntityAnimation<>(getSpritesheet().tile(8, 4), getSpritesheet().tile(9, 4));
		animation.setFrameDuration(8);
		animation.repeatForever();
		return animation;
	}

	@Override
	public SingleEntityAnimation<Rectangle2D> createGhostFlashingAnimation() {
		var animation = new SingleEntityAnimation<>(getSpritesheet().tilesToRight(8, 4, 4));
		animation.setFrameDuration(6);
		return animation;
	}

	@Override
	public EntityAnimationByDirection createGhostEyesAnimationMap(Ghost ghost) {
		var animationByDir = new EntityAnimationByDirection(ghost::wishDir);
		for (var dir : Direction.values()) {
			int d = getSpritesheet().dirIndex(dir);
			animationByDir.put(dir, new SingleEntityAnimation<>(getSpritesheet().tile(8 + d, 5)));
		}
		return animationByDir;
	}

	@Override
	public FixedEntityAnimation<Rectangle2D> createGhostValueList() {
		return new FixedEntityAnimation<>(getSpritesheet().tilesToRight(0, 8, 4));
	}

	// Pac-Man specific:

	public SingleEntityAnimation<Rectangle2D> createBigPacManMunchingAnimation() {
		var animation = new SingleEntityAnimation<>(getSpritesheet().tiles(2, 1, 2, 2), getSpritesheet().tiles(4, 1, 2, 2),
				getSpritesheet().tiles(6, 1, 2, 2));
		animation.setFrameDuration(3);
		animation.repeatForever();
		return animation;
	}

	public FixedEntityAnimation<Rectangle2D> createBlinkyStretchedAnimation() {
		return new FixedEntityAnimation<>(getSpritesheet().tilesToRight(8, 6, 5));
	}

	public FixedEntityAnimation<Rectangle2D> createBlinkyDamagedAnimation() {
		return new FixedEntityAnimation<>(getSpritesheet().tile(8, 7), getSpritesheet().tile(9, 7));
	}

	public SingleEntityAnimation<Rectangle2D> createBlinkyPatchedAnimation() {
		var animation = new SingleEntityAnimation<>(getSpritesheet().tile(10, 7), getSpritesheet().tile(11, 7));
		animation.setFrameDuration(4);
		animation.repeatForever();
		return animation;
	}

	public SingleEntityAnimation<Rectangle2D> createBlinkyNakedAnimation() {
		var animation = new SingleEntityAnimation<>(getSpritesheet().tiles(8, 8, 2, 1),
				getSpritesheet().tiles(10, 8, 2, 1));
		animation.setFrameDuration(4);
		animation.repeatForever();
		return animation;
	}
}