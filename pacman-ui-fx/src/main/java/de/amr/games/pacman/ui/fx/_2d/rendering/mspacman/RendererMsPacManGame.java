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

import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;
import static de.amr.games.pacman.ui.fx._2d.rendering.mspacman.MsPacManGameAssets.t3c;

import de.amr.games.pacman.lib.anim.EntityAnimation;
import de.amr.games.pacman.lib.anim.EntityAnimationByDirection;
import de.amr.games.pacman.lib.anim.EntityAnimationMap;
import de.amr.games.pacman.lib.anim.FixedEntityAnimation;
import de.amr.games.pacman.lib.anim.SingleEntityAnimation;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.model.mspacman.Clapperboard;
import de.amr.games.pacman.ui.fx._2d.rendering.RendererCommon;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class RendererMsPacManGame extends RendererCommon {

	public static final RendererMsPacManGame THE_ONE_AND_ONLY = new RendererMsPacManGame();

	private RendererMsPacManGame() {
	}

	@Override
	public Spritesheet spritesheet() {
		return MsPacManGameAssets.SPRITESHEET;
	}

	@Override
	public Color mazeFoodColor(int mazeNumber) {
		return MsPacManGameAssets.FOOD_COLORS[mazeNumber - 1];
	}

	@Override
	public Color mazeTopColor(int mazeNumber) {
		return MsPacManGameAssets.MAZE_TOP_COLORS[mazeNumber - 1];
	}

	@Override
	public Color mazeSideColor(int mazeNumber) {
		return MsPacManGameAssets.MAZE_SIDE_COLORS[mazeNumber - 1];
	}

	@Override
	public Color ghostHouseDoorColor() {
		return MsPacManGameAssets.GHOSTHOUSE_DOOR_COLOR;
	}

	@Override
	public Rectangle2D ghostSprite(int ghostID, Direction dir) {
		return MsPacManGameAssets.t3c(2 * spritesheet().dirIndex(dir) + 1, 4 + ghostID);
	}

	@Override
	public Rectangle2D bonusSymbolSprite(int symbol) {
		return t3c(3 + symbol, 0);
	}

	@Override
	public Rectangle2D bonusValueSprite(int symbol) {
		return t3c(3 + symbol, 1);
	}

	@Override
	public void drawFilledMaze(GraphicsContext g, int x, int y, int mazeNumber, World world, boolean energizersHidden) {
		var w = MsPacManGameAssets.MAZE_WIDTH;
		var h = MsPacManGameAssets.MAZE_HEIGHT;
		g.drawImage(spritesheet().source(), 0, h * (mazeNumber - 1), w, h, x, y, w, h);
		world.tiles().filter(world::containsEatenFood).forEach(tile -> hideTileContent(g, tile));
		if (energizersHidden) {
			world.energizerTiles().forEach(tile -> hideTileContent(g, tile));
		}
	}

	@Override
	public void drawEmptyMaze(GraphicsContext g, int x, int y, int mazeNumber, boolean flash) {
		var w = MsPacManGameAssets.MAZE_WIDTH;
		var h = MsPacManGameAssets.MAZE_HEIGHT;
		if (flash) {
			g.drawImage(MsPacManGameAssets.MAZES_EMPTY_INV[mazeNumber - 1], x, y);
		} else {
			g.drawImage(spritesheet().source(), MsPacManGameAssets.SECOND_COLUMN, h * (mazeNumber - 1), w, h, x, y, w, h);
		}
	}

	@Override
	public Rectangle2D lifeSprite() {
		return t3c(1, 0);
	}

	@Override
	public void drawCopyright(GraphicsContext g, int tileY) {
		int x = t(6);
		int y = t(tileY - 1);
		g.drawImage(MsPacManGameAssets.MIDWAY_LOGO, x, y + 2, t(4) - 2, t(4));
		g.setFill(Color.RED);
		g.setFont(Font.font("Dialog", 11));
		g.fillText("\u00a9", x + t(5), y + t(2) + 2); // (c) symbol
		g.setFont(arcadeFont(TS));
		g.fillText("MIDWAY MFG CO", x + t(7), y + t(2));
		g.fillText("1980/1981", x + t(8), y + t(4));
	}

	// Animations

	@Override
	public SingleEntityAnimation<Boolean> createMazeFlashingAnimation() {
		var animation = new SingleEntityAnimation<>(true, false);
		animation.setFrameDuration(10);
		return animation;
	}

	@Override
	public EntityAnimationByDirection createPacMunchingAnimation(Pac pac) {
		var animationByDir = new EntityAnimationByDirection(pac::moveDir);
		for (var dir : Direction.values()) {
			int d = spritesheet().dirIndex(dir);
			var wide = t3c(0, d);
			var middle = t3c(1, d);
			var closed = t3c(2, d);
			var munching = new SingleEntityAnimation<>(middle, middle, wide, wide, middle, middle, middle, closed, closed);
			munching.setFrameDuration(1);
			munching.repeatForever();
			animationByDir.put(dir, munching);
		}
		return animationByDir;
	}

	@Override
	public SingleEntityAnimation<Rectangle2D> createPacDyingAnimation() {
		var right = t3c(1, 0);
		var left = t3c(1, 1);
		var up = t3c(1, 2);
		var down = t3c(1, 3);
		// TODO not yet 100% accurate
		var animation = new SingleEntityAnimation<>(down, left, up, right, down, left, up, right, down, left, up);
		animation.setFrameDuration(8);
		return animation;
	}

	@Override
	public EntityAnimationByDirection createGhostColorAnimation(Ghost ghost) {
		var animationByDir = new EntityAnimationByDirection(ghost::wishDir);
		for (var dir : Direction.values()) {
			int d = spritesheet().dirIndex(dir);
			var animation = new SingleEntityAnimation<>(t3c(2 * d, 4 + ghost.id()), t3c(2 * d + 1, 4 + ghost.id()));
			animation.setFrameDuration(8);
			animation.repeatForever();
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	@Override
	public SingleEntityAnimation<Rectangle2D> createGhostBlueAnimation() {
		var animation = new SingleEntityAnimation<>(t3c(8, 4), t3c(9, 4));
		animation.setFrameDuration(8);
		animation.repeatForever();
		return animation;
	}

	@Override
	public SingleEntityAnimation<Rectangle2D> createGhostFlashingAnimation() {
		var animation = new SingleEntityAnimation<>(t3c(8, 4), t3c(9, 4), t3c(10, 4), t3c(11, 4));
		animation.setFrameDuration(4);
		return animation;
	}

	@Override
	public EntityAnimationByDirection createGhostEyesAnimation(Ghost ghost) {
		var animationByDir = new EntityAnimationByDirection(ghost::wishDir);
		for (var dir : Direction.values()) {
			int d = spritesheet().dirIndex(dir);
			animationByDir.put(dir, new SingleEntityAnimation<>(t3c(8 + d, 5)));
		}
		return animationByDir;
	}

	@Override
	public FixedEntityAnimation<Rectangle2D> createGhostValueList() {
		return new FixedEntityAnimation<>(t3c(0, 8), t3c(1, 8), t3c(2, 8), t3c(3, 8));
	}

	// Ms. Pac-Man specific:

	public void drawClapperboard(GraphicsContext g, Clapperboard clapper) {
		if (clapper.isVisible()) {
			clapper.animation().map(EntityAnimation::animate).ifPresent(frame -> {
				var sprite = (Rectangle2D) frame;
				drawEntitySprite(g, clapper, sprite);
				g.setFont(arcadeFont(TS));
				g.setFill(Color.rgb(222, 222, 255));
				g.fillText(String.valueOf(clapper.sceneNumber), clapper.position().x() + sprite.getWidth() - 25,
						clapper.position().y() + 18);
				g.fillText(clapper.sceneTitle, clapper.position().x() + sprite.getWidth(), clapper.position().y());
			});
		}
	}

	public Rectangle2D heartSprite() {
		return t3c(2, 10);
	}

	public Rectangle2D blueBagSprite() {
		return new Rectangle2D(488, 199, 8, 8);
	}

	public Rectangle2D juniorPacSprite() {
		return new Rectangle2D(509, 200, 8, 8);
	}

	public EntityAnimationByDirection createPacManMunchingAnimationMap(Pac pac) {
		var animationByDir = new EntityAnimationByDirection(pac::moveDir);
		for (var dir : Direction.values()) {
			int d = spritesheet().dirIndex(dir);
			var animation = new SingleEntityAnimation<>(t3c(0, 9 + d), t3c(1, 9 + d), t3c(2, 9));
			animation.setFrameDuration(2);
			animation.repeatForever();
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	public SingleEntityAnimation<Rectangle2D> createClapperboardAnimation() {
		// TODO this is not 100% accurate yet
		var animation = new SingleEntityAnimation<>( //
				new Rectangle2D(456, 208, 32, 32), //
				new Rectangle2D(488, 208, 32, 32), //
				new Rectangle2D(520, 208, 32, 32), //
				new Rectangle2D(488, 208, 32, 32), //
				new Rectangle2D(456, 208, 32, 32)//
		);
		animation.setFrameDuration(4);
		return animation;
	}

	public EntityAnimationMap<Integer> createClapperboardAnimations() {
		var animationSet = new EntityAnimationMap<Integer>(1);
		animationSet.put(0, createClapperboardAnimation());
		animationSet.select(0);
		return animationSet;
	}

	public SingleEntityAnimation<Rectangle2D> createStorkFlyingAnimation() {
		var animation = new SingleEntityAnimation<>( //
				new Rectangle2D(489, 176, 32, 16), //
				new Rectangle2D(521, 176, 32, 16) //
		);
		animation.repeatForever();
		animation.setFrameDuration(8);
		return animation;
	}
}