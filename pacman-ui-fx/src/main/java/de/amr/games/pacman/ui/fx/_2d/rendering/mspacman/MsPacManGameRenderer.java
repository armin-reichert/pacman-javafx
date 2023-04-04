/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
import java.util.stream.IntStream;

import de.amr.games.pacman.lib.anim.Animated;
import de.amr.games.pacman.lib.anim.AnimationByDirection;
import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.lib.anim.FrameSequence;
import de.amr.games.pacman.lib.anim.Pulse;
import de.amr.games.pacman.lib.anim.SimpleAnimation;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Bonus;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.model.mspacman.Clapperboard;
import de.amr.games.pacman.model.mspacman.MovingBonus;
import de.amr.games.pacman.ui.fx._2d.rendering.common.ArcadeTheme;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostColoring;
import de.amr.games.pacman.ui.fx._2d.rendering.common.MazeColoring;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Reordering;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Spritesheet;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpritesheetRenderer;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class MsPacManGameRenderer extends SpritesheetRenderer {

	private static final Spritesheet SHEET = new Spritesheet(ResourceMgr.image("graphics/mspacman/sprites.png"), 16);

	// Order of direction-related images inside spritesheet
	private static final Reordering<Direction> DIR_ORDER = new Reordering<>(Direction.RIGHT, Direction.LEFT, Direction.UP,
			Direction.DOWN);

	//@formatter:off
	private static final MazeColoring[] MAZE_COLORS = {
		new MazeColoring(Color.rgb(222, 222, 255), Color.rgb(255, 183, 174),  Color.rgb(255,   0,   0), Color.rgb(255, 183, 255)),
		new MazeColoring(Color.rgb(255, 255, 0),   Color.rgb( 71, 183, 255),  Color.rgb(222, 222, 255), Color.rgb(255, 183, 255)),
		new MazeColoring(Color.rgb(255,   0, 0),   Color.rgb(222, 151,  81),  Color.rgb(222, 222, 255), Color.rgb(255, 183, 255)),
		new MazeColoring(Color.rgb(222, 222, 255), Color.rgb( 33,  33, 255),  Color.rgb(255, 183,  81), Color.rgb(255, 183, 255)),
		new MazeColoring(Color.rgb(0,   255, 255), Color.rgb(255, 183, 255),  Color.rgb(255, 255,   0), Color.rgb(255, 183, 255)),
		new MazeColoring(Color.rgb(222, 222, 255), Color.rgb(255, 183, 174),  Color.rgb(255, 255,   0), Color.rgb(255, 183, 255)),
	};
	//@formatter:on

	private static final int MAZE_WIDTH = 226;
	private static final int MAZE_HEIGHT = 248;
	private static final int SECOND_COLUMN = 228;
	private static final int THIRD_COLUMN = 456;

	private static final Image MIDWAY_LOGO = ResourceMgr.image("graphics/mspacman/midway.png");

	private static final Image[] MAZES_EMPTY_FLASHING = IntStream.range(0, 6)
			.mapToObj(MsPacManGameRenderer::emptyMazeFlashing).toArray(Image[]::new);

	private static Image emptyMaze(int i) {
		return SHEET.subImage(SECOND_COLUMN, MAZE_HEIGHT * i, MAZE_WIDTH, MAZE_HEIGHT);
	}

	private static Image emptyMazeFlashing(int i) {
		return Ufx.colorsExchanged(emptyMaze(i),
				Map.of(MAZE_COLORS[i].wallBaseColor(), Color.WHITE, MAZE_COLORS[i].wallTopColor(), Color.BLACK));
	}

	// tile from third column
	private static Rectangle2D col3(int col, int row) {
		return SHEET.tilesFrom(THIRD_COLUMN, 0, col, row, 1, 1);
	}

	public MsPacManGameRenderer() {
		super(SHEET);
	}

	@Override
	public GhostColoring ghostColoring(int ghostID) {
		return ArcadeTheme.GHOST_COLORS[ghostID];
	}

	@Override
	public MazeColoring mazeColoring(int mazeNumber) {
		return MAZE_COLORS[mazeNumber - 1];
	}

	@Override
	public Rectangle2D ghostValueRegion(int index) {
		return col3(index, 8);
	}

	@Override
	public Rectangle2D bonusSymbolRegion(int symbol) {
		return col3(3 + symbol, 0);
	}

	@Override
	public Rectangle2D bonusValueRegion(int symbol) {
		return col3(3 + symbol, 1);
	}

	@Override
	public void drawBonus(GraphicsContext g, Bonus bonus) {
		var sprite = switch (bonus.state()) {
		case Bonus.STATE_INACTIVE -> null;
		case Bonus.STATE_EDIBLE -> bonusSymbolRegion(bonus.symbol());
		case Bonus.STATE_EATEN -> bonusValueRegion(bonus.symbol());
		default -> throw new IllegalArgumentException();
		};
		if (bonus instanceof MovingBonus movingBonus) {
			g.save();
			g.translate(0, movingBonus.dy());
			drawEntitySprite(g, movingBonus.entity(), sprite);
			g.restore();
		} else {
			super.drawBonus(g, bonus);
		}
	}

	@Override
	public void drawGhostFacingRight(GraphicsContext g, int ghostID, int x, int y) {
		var region = col3(2 * DIR_ORDER.index(Direction.RIGHT) + 1, 4 + ghostID);
		drawSpriteCenteredOverBox(g, region, x, y);
	}

	@Override
	public void drawMaze(GraphicsContext g, int x, int y, int mazeNumber, World world) {
		var w = MAZE_WIDTH;
		var h = MAZE_HEIGHT;
		var flashingAnimation = world.animation(GameModel.AK_MAZE_FLASHING);
		if (flashingAnimation.isPresent() && flashingAnimation.get().isRunning()) {
			var flashing = (boolean) flashingAnimation.get().frame();
			if (flashing) {
				g.drawImage(MAZES_EMPTY_FLASHING[mazeNumber - 1], x, y);
			} else {
				drawSprite(g, spritesheet.region(SECOND_COLUMN, h * (mazeNumber - 1), w, h), x, y);
			}
		} else {
			drawSprite(g, spritesheet.region(0, h * (mazeNumber - 1), w, h), x, y);
			world.tiles().filter(world::containsEatenFood).forEach(tile -> hideTileContent(g, tile));
			var energizerBlinking = world.animation(GameModel.AK_MAZE_ENERGIZER_BLINKING);
			boolean energizerVisible = energizerBlinking.isPresent() && (boolean) energizerBlinking.get().frame();
			if (!energizerVisible) {
				world.energizerTiles().forEach(tile -> hideTileContent(g, tile));
			}
		}
	}

	@Override
	public Rectangle2D lifeSymbolRegion() {
		return col3(1, 0);
	}

	public void drawCopyright(GraphicsContext g, int tileY) {
		int x = t(6);
		int y = t(tileY - 1);
		g.drawImage(MIDWAY_LOGO, x, y + 2, t(4) - 2, t(4));
		g.setFill(ArcadeTheme.RED);
		g.setFont(Font.font("Dialog", 11));
		g.fillText("\u00a9", x + t(5), y + t(2) + 2); // (c) symbol
		g.setFont(ArcadeTheme.SCREEN_FONT);
		g.fillText("MIDWAY MFG CO", x + t(7), y + t(2));
		g.fillText("1980/1981", x + t(8), y + t(4));
	}

	// Animations

	@Override
	public AnimationMap createWorldAnimations(World world) {
		var map = new AnimationMap();
		map.put(GameModel.AK_MAZE_ENERGIZER_BLINKING, new Pulse(10, true));
		map.put(GameModel.AK_MAZE_FLASHING, new Pulse(10, true));
		return map;
	}

	@Override
	public AnimationMap createPacAnimations(Pac pac) {
		var map = new AnimationMap();
		map.put(GameModel.AK_PAC_DYING, createPacDyingAnimation());
		map.put(GameModel.AK_PAC_MUNCHING, createPacMunchingAnimation(pac));
		map.select(GameModel.AK_PAC_MUNCHING);
		return map;
	}

	private AnimationByDirection createPacMunchingAnimation(Pac pac) {
		var animationByDir = new AnimationByDirection(pac::moveDir);
		for (var dir : Direction.values()) {
			int d = DIR_ORDER.index(dir);
			var wide = col3(0, d);
			var middle = col3(1, d);
			var closed = col3(2, d);
			var munching = new SimpleAnimation<>(middle, middle, wide, wide, middle, middle, middle, closed, closed);
			munching.setFrameDuration(1);
			munching.repeatForever();
			animationByDir.put(dir, munching);
		}
		return animationByDir;
	}

	private SimpleAnimation<Rectangle2D> createPacDyingAnimation() {
		var right = col3(1, 0);
		var left = col3(1, 1);
		var up = col3(1, 2);
		var down = col3(1, 3);
		// TODO not yet 100% accurate
		var animation = new SimpleAnimation<>(down, left, up, right, down, left, up, right, down, left, up);
		animation.setFrameDuration(8);
		return animation;
	}

	@Override
	public AnimationMap createGhostAnimations(Ghost ghost) {
		var map = new AnimationMap();
		map.put(GameModel.AK_GHOST_COLOR, createGhostColorAnimation(ghost));
		map.put(GameModel.AK_GHOST_BLUE, createGhostBlueAnimation());
		map.put(GameModel.AK_GHOST_EYES, createGhostEyesAnimation(ghost));
		map.put(GameModel.AK_GHOST_FLASHING, createGhostFlashingAnimation());
		map.put(GameModel.AK_GHOST_VALUE, createGhostValueSpriteList());
		map.select(GameModel.AK_GHOST_COLOR);
		return map;
	}

	private AnimationByDirection createGhostColorAnimation(Ghost ghost) {
		var animationByDir = new AnimationByDirection(ghost::wishDir);
		for (var dir : Direction.values()) {
			int d = DIR_ORDER.index(dir);
			var animation = new SimpleAnimation<>(col3(2 * d, 4 + ghost.id()), col3(2 * d + 1, 4 + ghost.id()));
			animation.setFrameDuration(8);
			animation.repeatForever();
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	private SimpleAnimation<Rectangle2D> createGhostBlueAnimation() {
		var animation = new SimpleAnimation<>(col3(8, 4), col3(9, 4));
		animation.setFrameDuration(8);
		animation.repeatForever();
		return animation;
	}

	private SimpleAnimation<Rectangle2D> createGhostFlashingAnimation() {
		var animation = new SimpleAnimation<>(col3(8, 4), col3(9, 4), col3(10, 4), col3(11, 4));
		animation.setFrameDuration(4);
		return animation;
	}

	private AnimationByDirection createGhostEyesAnimation(Ghost ghost) {
		var animationByDir = new AnimationByDirection(ghost::wishDir);
		for (var dir : Direction.values()) {
			int d = DIR_ORDER.index(dir);
			animationByDir.put(dir, new SimpleAnimation<>(col3(8 + d, 5)));
		}
		return animationByDir;
	}

	private Animated createGhostValueSpriteList() {
		return new FrameSequence<>(ghostValueRegion(0), ghostValueRegion(1), ghostValueRegion(2), ghostValueRegion(3));
	}

	// Ms. Pac-Man specific:

	public void drawClap(GraphicsContext g, Clapperboard clap) {
		if (clap.isVisible()) {
			clap.animation().map(Animated::animate).ifPresent(frame -> {
				var sprite = (Rectangle2D) frame;
				if (clap.isVisible()) {
					drawSpriteCenteredOverBox(g, sprite, clap.position().x(), clap.position().y());
				}
				g.setFont(ArcadeTheme.SCREEN_FONT);
				g.setFill(ArcadeTheme.PALE);
				g.fillText(String.valueOf(clap.sceneNumber()), clap.position().x() + sprite.getWidth() - 25,
						clap.position().y() + 18);
				g.fillText(clap.sceneTitle(), clap.position().x() + sprite.getWidth(), clap.position().y());
			});
		}
	}

	public Rectangle2D heartSprite() {
		return col3(2, 10);
	}

	public Rectangle2D blueBagSprite() {
		return spritesheet.region(488, 199, 8, 8);
	}

	public Rectangle2D juniorPacSprite() {
		return spritesheet.region(509, 200, 8, 8);
	}

	public AnimationByDirection createPacManMunchingAnimationMap(Pac pac) {
		var animationByDir = new AnimationByDirection(pac::moveDir);
		for (var dir : Direction.values()) {
			int d = DIR_ORDER.index(dir);
			var animation = new SimpleAnimation<>(col3(0, 9 + d), col3(1, 9 + d), col3(2, 9));
			animation.setFrameDuration(2);
			animation.repeatForever();
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	public SimpleAnimation<Rectangle2D> createClapperboardAnimation() {
		// TODO this is not 100% accurate yet
		var animation = new SimpleAnimation<>( //
				spritesheet.region(456, 208, 32, 32), //
				spritesheet.region(488, 208, 32, 32), //
				spritesheet.region(520, 208, 32, 32), //
				spritesheet.region(488, 208, 32, 32), //
				spritesheet.region(456, 208, 32, 32)//
		);
		animation.setFrameDuration(4);
		return animation;
	}

	public SimpleAnimation<Rectangle2D> createStorkFlyingAnimation() {
		var animation = new SimpleAnimation<>( //
				spritesheet.region(489, 176, 32, 16), //
				spritesheet.region(521, 176, 32, 16) //
		);
		animation.repeatForever();
		animation.setFrameDuration(8);
		return animation;
	}
}