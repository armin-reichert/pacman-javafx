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

import static de.amr.games.pacman.model.common.world.World.t;

import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.animation.EntityAnimation;
import de.amr.games.pacman.lib.animation.EntityAnimationByDirection;
import de.amr.games.pacman.lib.animation.FixedEntityAnimation;
import de.amr.games.pacman.lib.animation.SingleEntityAnimation;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.model.mspacman.Clapperboard;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class RendererMsPacManGame extends RendererCommon {

	private static final int MAZE_WIDTH = 226;
	private static final int MAZE_HEIGHT = 248;
	private static final int SECOND_COLUMN = 228;
	private static final int THIRD_COLUMN = 456;

	//@formatter:off
	private static final Color[] MAZE_TOP_COLORS = { 
		Color.rgb(255, 183, 174), 
		Color.rgb(71, 183, 255), 
		Color.rgb(222, 151, 81), 
		Color.rgb(33, 33, 255), 
		Color.rgb(255, 183, 255), 
		Color.rgb(255, 183, 174), 
	};

	private static final Color[] MAZE_SIDE_COLORS = { 
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

	private static final Spritesheet SPRITESHEET;
	private static final Image MIDWAY_LOGO;
	private static final Image[] MAZES_EMPTY_INV;

	static {
		SPRITESHEET = new Spritesheet("graphics/mspacman/sprites.png", 16, Direction.RIGHT, Direction.LEFT, Direction.UP,
				Direction.DOWN);
		MIDWAY_LOGO = Ufx.image("graphics/mspacman/midway.png");
		int numMazes = 6;
		MAZES_EMPTY_INV = new Image[numMazes];
		for (int i = 0; i < numMazes; ++i) {
			var mazeEmpty = SPRITESHEET.subImage(SECOND_COLUMN, MAZE_HEIGHT * i, MAZE_WIDTH, MAZE_HEIGHT);
			MAZES_EMPTY_INV[i] = Ufx.colorsExchanged(mazeEmpty, //
					Map.of(MAZE_SIDE_COLORS[i], Color.WHITE, MAZE_TOP_COLORS[i], Color.BLACK));
		}
	}

	// tile from third column
	private Rectangle2D t3c(int col, int row) {
		return spritesheet().tiles(THIRD_COLUMN, 0, col, row, 1, 1);
	}

	@Override
	public Spritesheet spritesheet() {
		return SPRITESHEET;
	}

	@Override
	public Color getMazeFoodColor(int mazeNumber) {
		return FOOD_COLORS[mazeNumber - 1];
	}

	@Override
	public Color getMazeTopColor(int mazeNumber) {
		return MAZE_TOP_COLORS[mazeNumber - 1];
	}

	@Override
	public Color getMazeSideColor(int mazeNumber) {
		return MAZE_SIDE_COLORS[mazeNumber - 1];
	}

	@Override
	public Color getGhostHouseDoorColor() {
		return Color.rgb(255, 183, 255);
	}

	@Override
	public Rectangle2D ghostSprite(int ghostID, Direction dir) {
		return t3c(2 * spritesheet().dirIndex(dir) + 1, 4 + ghostID);
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
		g.drawImage(spritesheet().sourceImage(), 0, MAZE_HEIGHT * (mazeNumber - 1), MAZE_WIDTH, MAZE_HEIGHT, x, y,
				MAZE_WIDTH, MAZE_HEIGHT);
		world.tiles().filter(world::containsEatenFood).forEach(tile -> clearTileContent(g, tile));
		if (energizersHidden) {
			world.energizerTiles().forEach(tile -> clearTileContent(g, tile));
		}
	}

	@Override
	public void drawEmptyMaze(GraphicsContext g, int x, int y, int mazeNumber, boolean flash) {
		if (flash) {
			g.drawImage(MAZES_EMPTY_INV[mazeNumber - 1], x, y);
		} else {
			g.drawImage(spritesheet().sourceImage(), SECOND_COLUMN, MAZE_HEIGHT * (mazeNumber - 1), MAZE_WIDTH, MAZE_HEIGHT,
					x, y, MAZE_WIDTH, MAZE_HEIGHT);
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
		g.drawImage(MIDWAY_LOGO, x, y + 2, t(4) - 2, t(4));
		g.setFill(Color.RED);
		g.setFont(Font.font("Dialog", 11));
		g.fillText("\u00a9", x + t(5), y + t(2) + 2); // (c) symbol
		g.setFont(arcadeFont());
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
	public EntityAnimationByDirection createPacMunchingAnimationMap(Pac pac) {
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
	public EntityAnimationByDirection createGhostColorAnimationMap(Ghost ghost) {
		var animationByDir = new EntityAnimationByDirection(ghost::wishDir);
		for (var dir : Direction.values()) {
			int d = spritesheet().dirIndex(dir);
			var animation = new SingleEntityAnimation<>(t3c(2 * d, 4 + ghost.id), t3c(2 * d + 1, 4 + ghost.id));
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
	public EntityAnimationByDirection createGhostEyesAnimationMap(Ghost ghost) {
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
				drawEntity(g, clapper, sprite);
				g.setFont(arcadeFont());
				g.setFill(Color.rgb(222, 222, 255));
				g.fillText(String.valueOf(clapper.sceneNumber), clapper.getPosition().x() + sprite.getWidth() - 25,
						clapper.getPosition().y() + 18);
				g.fillText(clapper.sceneTitle, clapper.getPosition().x() + sprite.getWidth(), clapper.getPosition().y());
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