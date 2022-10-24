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
import de.amr.games.pacman.lib.animation.EntityAnimation;
import de.amr.games.pacman.lib.animation.EntityAnimationByDirection;
import de.amr.games.pacman.lib.animation.FixedEntityAnimation;
import de.amr.games.pacman.lib.animation.SingleEntityAnimation;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.mspacman.Clapperboard;
import de.amr.games.pacman.ui.fx._2d.rendering.common.ArcadeRendererBase;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Spritesheet;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class ArcadeRendererMsPacManGame extends ArcadeRendererBase {

	//@formatter:off
	private static final Color[] GHOST_COLORS = {
		Color.RED,
		Color.rgb(252, 181, 255),
		Color.CYAN,
		Color.rgb(253, 192, 90)
	};
	
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
	private static final Image[] MAZES_FULL;
	private static final Image[] MAZES_EMPTY;
	private static final Image[] MAZES_EMPTY_BW;

	static {
		SPRITESHEET = new Spritesheet("graphics/mspacman/sprites.png", 16, Direction.RIGHT, Direction.LEFT, Direction.UP,
				Direction.DOWN);
		MIDWAY_LOGO = Ufx.image("graphics/mspacman/midway.png");
		int numMazes = 6;
		MAZES_FULL = new Image[numMazes];
		MAZES_EMPTY = new Image[numMazes];
		MAZES_EMPTY_BW = new Image[numMazes];
		for (int i = 0; i < numMazes; ++i) {
			MAZES_FULL[i] = SPRITESHEET.subImage(0, 248 * i, 226, 248);
			MAZES_EMPTY[i] = SPRITESHEET.subImage(228, 248 * i, 226, 248);
			MAZES_EMPTY_BW[i] = Ufx.colorsExchanged(MAZES_EMPTY[i], //
					Map.of(MAZE_SIDE_COLORS[i], Color.WHITE, MAZE_TOP_COLORS[i], Color.BLACK));
		}
	}

	public ArcadeRendererMsPacManGame() {
		super(SPRITESHEET);
	}

	/**
	 * @param col column
	 * @param row row
	 * @return Sprite at given row and column from the right-hand-side of the spritesheet
	 */
	public Rectangle2D rhs(int col, int row) {
		return sheet.tilesAtOrigin(456, 0, col, row, 1, 1);
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
		return rhs(2 * sheet.dirIndex(dir) + 1, 4 + ghostID);
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
		return MAZES_FULL[mazeNumber - 1];
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
		g.drawImage(MIDWAY_LOGO, x, y + 2, t(4) - 2, t(4));
		g.setFill(Color.RED);
		g.setFont(Font.font("Dialog", 11));
		g.fillText("\u00a9", x + t(5), y + t(2) + 2); // (c) symbol
		g.setFont(getArcadeFont());
		g.fillText("MIDWAY MFG CO", x + t(7), y + t(2));
		g.fillText("1980/1981", x + t(8), y + t(4));
	}

	public void drawFlap(GraphicsContext g, Clapperboard flap) {
		if (flap.isVisible()) {
			flap.animation().map(EntityAnimation::animate).ifPresent(spriteObj -> {
				var sprite = (Rectangle2D) spriteObj;
				drawEntity(g, flap, sprite);
				g.setFont(getArcadeFont());
				g.setFill(Color.rgb(222, 222, 255));
				g.fillText(String.valueOf(flap.sceneNumber), flap.getPosition().x() + sprite.getWidth() - 25,
						flap.getPosition().y() + 18);
				g.fillText(flap.sceneTitle, flap.getPosition().x() + sprite.getWidth(), flap.getPosition().y());
			});
		}
	}

	@Override
	public SingleEntityAnimation<Image> createMazeFlashingAnimation(int mazeNumber) {
		int mazeIndex = mazeNumber - 1;
		var mazeEmpty = sheet.subImage(228, 248 * mazeIndex, 226, 248);
		var brightImage = Ufx.colorsExchanged(mazeEmpty, Map.of( //
				MAZE_SIDE_COLORS[mazeIndex], Color.WHITE, //
				MAZE_TOP_COLORS[mazeIndex], Color.BLACK) //
		);
		var animation = new SingleEntityAnimation<>(brightImage, mazeEmpty);
		animation.setFrameDuration(10);
		return animation;
	}

	@Override
	public EntityAnimationByDirection createPacMunchingAnimationMap(Pac pac) {
		var animationByDir = new EntityAnimationByDirection(pac::moveDir);
		for (var dir : Direction.values()) {
			int d = sheet.dirIndex(dir);
			var wide = rhs(0, d);
			var middle = rhs(1, d);
			var closed = rhs(2, d);
			var munching = new SingleEntityAnimation<>(middle, middle, wide, wide, middle, middle, middle, closed, closed);
			munching.setFrameDuration(1);
			munching.repeatForever();
			animationByDir.put(dir, munching);
		}
		return animationByDir;
	}

	@Override
	public SingleEntityAnimation<Rectangle2D> createPacDyingAnimation() {
		var right = rhs(1, 0);
		var left = rhs(1, 1);
		var up = rhs(1, 2);
		var down = rhs(1, 3);
		// TODO not yet 100% accurate
		var animation = new SingleEntityAnimation<>(down, left, up, right, down, left, up, right, down, left, up);
		animation.setFrameDuration(8);
		return animation;
	}

	@Override
	public EntityAnimationByDirection createGhostColorAnimationMap(Ghost ghost) {
		var animationByDir = new EntityAnimationByDirection(ghost::wishDir);
		for (var dir : Direction.values()) {
			int d = sheet.dirIndex(dir);
			var animation = new SingleEntityAnimation<>(rhs(2 * d, 4 + ghost.id), rhs(2 * d + 1, 4 + ghost.id));
			animation.setFrameDuration(8);
			animation.repeatForever();
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	@Override
	public SingleEntityAnimation<Rectangle2D> createGhostBlueAnimation() {
		var animation = new SingleEntityAnimation<>(rhs(8, 4), rhs(9, 4));
		animation.setFrameDuration(8);
		animation.repeatForever();
		return animation;
	}

	@Override
	public SingleEntityAnimation<Rectangle2D> createGhostFlashingAnimation() {
		var animation = new SingleEntityAnimation<>(rhs(8, 4), rhs(9, 4), rhs(10, 4), rhs(11, 4));
		animation.setFrameDuration(4);
		return animation;
	}

	@Override
	public EntityAnimationByDirection createGhostEyesAnimationMap(Ghost ghost) {
		var animationByDir = new EntityAnimationByDirection(ghost::wishDir);
		for (var dir : Direction.values()) {
			int d = sheet.dirIndex(dir);
			animationByDir.put(dir, new SingleEntityAnimation<>(rhs(8 + d, 5)));
		}
		return animationByDir;
	}

	@Override
	public FixedEntityAnimation<Rectangle2D> createGhostValueList() {
		return new FixedEntityAnimation<>(rhs(0, 8), rhs(1, 8), rhs(2, 8), rhs(3, 8));
	}

	// Ms. Pac-Man specific:

	public EntityAnimationByDirection createPacManMunchingAnimationMap(Pac pac) {
		var animationByDir = new EntityAnimationByDirection(pac::moveDir);
		for (var dir : Direction.values()) {
			int d = sheet.dirIndex(dir);
			var animation = new SingleEntityAnimation<>(rhs(0, 9 + d), rhs(1, 9 + d), rhs(2, 9));
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