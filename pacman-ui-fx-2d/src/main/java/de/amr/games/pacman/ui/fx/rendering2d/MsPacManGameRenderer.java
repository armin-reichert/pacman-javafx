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
package de.amr.games.pacman.ui.fx.rendering2d;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

import de.amr.games.pacman.lib.anim.Animated;
import de.amr.games.pacman.lib.anim.AnimationByDirection;
import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.lib.anim.FrameSequence;
import de.amr.games.pacman.lib.anim.Pulse;
import de.amr.games.pacman.lib.anim.SimpleAnimation;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Clapperboard;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.PacManGames2dAssets;
import de.amr.games.pacman.ui.fx.util.Order;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class MsPacManGameRenderer extends GameRenderer {

	private static final Order<Direction> DIR_ORDER = new Order<>(//
			Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

	private static final int MAZE_IMAGE_WIDTH = 226;
	private static final int MAZE_IMAGE_HEIGHT = 248;
	private static final int SECOND_COLUMN = 228;
	private static final int THIRD_COLUMN = 456;

	public MsPacManGameRenderer(PacManGames2dAssets assets, Theme theme) {
		super(assets, theme, theme.spritesheet("mspacman.spritesheet"));
	}

	private Rectangle2D tileFromThirdColumn(int tileX, int tileY) {
		return spritesheet().tilesFrom(THIRD_COLUMN, 0, tileX, tileY, 1, 1);
	}

	@Override
	public Rectangle2D ghostValueSprite(int index) {
		return tileFromThirdColumn(index, 8);
	}

	@Override
	public Rectangle2D bonusSymbolSprite(int symbol) {
		return tileFromThirdColumn(3 + symbol, 0);
	}

	@Override
	public Rectangle2D bonusValueSprite(int symbol) {
		return tileFromThirdColumn(3 + symbol, 1);
	}

	@Override
	public void drawBonus(GraphicsContext g, Bonus bonus) {
		checkNotNull(bonus);
		var sprite = switch (bonus.state()) {
		case Bonus.STATE_INACTIVE -> null;
		case Bonus.STATE_EDIBLE -> bonusSymbolSprite(bonus.symbol());
		case Bonus.STATE_EATEN -> bonusValueSprite(bonus.symbol());
		default -> throw new IllegalArgumentException("Illegal bonus state: '%s'".formatted(bonus.state()));
		};
		if (bonus instanceof MovingBonus movingBonus) {
			g.save();
			g.translate(0, movingBonus.dy());
			drawEntitySprite(g, movingBonus.entity(), sprite);
			g.restore();
		} else {
			drawEntitySprite(g, bonus.entity(), sprite);
		}
	}

	private Rectangle2D highlightedMaze(int mazeNumber) {
		return new Rectangle2D(0, (mazeNumber - 1) * MAZE_IMAGE_HEIGHT, MAZE_IMAGE_WIDTH, MAZE_IMAGE_HEIGHT);
	}

	private Rectangle2D emptyMaze(int mazeNumber) {
		return spritesheet().region(SECOND_COLUMN, (mazeNumber - 1) * MAZE_IMAGE_HEIGHT, MAZE_IMAGE_WIDTH,
				MAZE_IMAGE_HEIGHT);
	}

	private Rectangle2D filledMaze(int mazeNumber) {
		return spritesheet().region(0, (mazeNumber - 1) * MAZE_IMAGE_HEIGHT, MAZE_IMAGE_WIDTH, MAZE_IMAGE_HEIGHT);
	}

	@Override
	public void drawMaze(GraphicsContext g, double x, double y, int mazeNumber, World world) {
		checkNotNull(world);

		// check if flashing animation is running
		var flashingAnimation = world.animation(GameModel.AK_MAZE_FLASHING);
		if (flashingAnimation.isPresent() && flashingAnimation.get().isRunning()) {
			var flashing = (boolean) flashingAnimation.get().frame();
			if (flashing) {
				GameRenderer.drawSprite(g, theme.image("mspacman.flashingMazes"), highlightedMaze(mazeNumber),
						x - 3 /* don't tell your mommy */, y);
			} else {
				drawSprite(g, emptyMaze(mazeNumber), x, y);
			}
		}

		else {
			// draw filled maze and hide eaten food (including energizers)
			drawSprite(g, filledMaze(mazeNumber), x, y);
			world.tiles().filter(world::containsEatenFood).forEach(tile -> GameRenderer.hideTileContent(g, tile));

			// energizer animation
			world.animation(GameModel.AK_MAZE_ENERGIZER_BLINKING).ifPresent(blinking -> {
				if (Boolean.FALSE.equals(blinking.frame())) {
					world.energizerTiles().forEach(tile -> GameRenderer.hideTileContent(g, tile));
				}
			});
		}
	}

	@Override
	public void drawLivesCounter(GraphicsContext g, int numLivesDisplayed) {
		drawLivesCounter(g, spritesheet(), numLivesDisplayed);
	}

	@Override
	public Rectangle2D livesCounterSprite() {
		return tileFromThirdColumn(1, 0);
	}

	public void drawCopyright(GraphicsContext g, double x, double y) {
		var font = theme.font("font.arcade", 8);
		g.drawImage(theme.image("mspacman.logo.midway"), x, y + 2, TS * 4 - 2, TS * 4);
		g.setFill(ArcadeTheme.RED);
		g.setFont(Font.font("Dialog", 11));
		g.fillText("\u00a9", x + TS * 5, y + TS * 2 + 2); // (c) symbol
		g.setFont(font);
		g.fillText("MIDWAY MFG CO", x + TS * 7, y + TS * 2);
		g.fillText("1980/1981", x + TS * 8, y + TS * 4);
	}

	// Animations

	@Override
	public AnimationMap createWorldAnimations(World world) {
		var map = new AnimationMap(GameModel.ANIMATION_MAP_CAPACITY);
		map.put(GameModel.AK_MAZE_ENERGIZER_BLINKING, new Pulse(10, true));
		map.put(GameModel.AK_MAZE_FLASHING, new Pulse(10, true));
		return map;
	}

	@Override
	public AnimationMap createPacAnimations(Pac pac) {
		var map = new AnimationMap(GameModel.ANIMATION_MAP_CAPACITY);
		map.put(GameModel.AK_PAC_DYING, createPacDyingAnimation());
		map.put(GameModel.AK_PAC_MUNCHING, createPacMunchingAnimation(pac));
		map.select(GameModel.AK_PAC_MUNCHING);
		return map;
	}

	private AnimationByDirection createPacMunchingAnimation(Pac pac) {
		var animationByDir = new AnimationByDirection(pac::moveDir);
		for (var dir : Direction.values()) {
			int d = DIR_ORDER.index(dir);
			var wide = tileFromThirdColumn(0, d);
			var middle = tileFromThirdColumn(1, d);
			var closed = tileFromThirdColumn(2, d);
			var munching = new SimpleAnimation<>(middle, middle, wide, wide, middle, middle, middle, closed, closed);
			munching.setFrameDuration(1);
			munching.repeatForever();
			animationByDir.put(dir, munching);
		}
		return animationByDir;
	}

	private SimpleAnimation<Rectangle2D> createPacDyingAnimation() {
		var right = tileFromThirdColumn(1, 0);
		var left = tileFromThirdColumn(1, 1);
		var up = tileFromThirdColumn(1, 2);
		var down = tileFromThirdColumn(1, 3);
		// TODO not yet 100% accurate
		var animation = new SimpleAnimation<>(down, left, up, right, down, left, up, right, down, left, up);
		animation.setFrameDuration(8);
		return animation;
	}

	@Override
	public AnimationMap createGhostAnimations(Ghost ghost) {
		var map = new AnimationMap(GameModel.ANIMATION_MAP_CAPACITY);
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
			var animation = new SimpleAnimation<>(tileFromThirdColumn(2 * d, 4 + ghost.id()),
					tileFromThirdColumn(2 * d + 1, 4 + ghost.id()));
			animation.setFrameDuration(8);
			animation.repeatForever();
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	private SimpleAnimation<Rectangle2D> createGhostBlueAnimation() {
		var animation = new SimpleAnimation<>(tileFromThirdColumn(8, 4), tileFromThirdColumn(9, 4));
		animation.setFrameDuration(8);
		animation.repeatForever();
		return animation;
	}

	private SimpleAnimation<Rectangle2D> createGhostFlashingAnimation() {
		var animation = new SimpleAnimation<>(tileFromThirdColumn(8, 4), tileFromThirdColumn(9, 4),
				tileFromThirdColumn(10, 4), tileFromThirdColumn(11, 4));
		animation.setFrameDuration(4);
		return animation;
	}

	private AnimationByDirection createGhostEyesAnimation(Ghost ghost) {
		var animationByDir = new AnimationByDirection(ghost::wishDir);
		for (var dir : Direction.values()) {
			int d = DIR_ORDER.index(dir);
			animationByDir.put(dir, new SimpleAnimation<>(tileFromThirdColumn(8 + d, 5)));
		}
		return animationByDir;
	}

	private Animated createGhostValueSpriteList() {
		return new FrameSequence<>(ghostValueSprite(0), ghostValueSprite(1), ghostValueSprite(2), ghostValueSprite(3));
	}

	// Ms. Pac-Man specific:

	public void drawClap(GraphicsContext g, Clapperboard clap) {
		if (clap.isVisible()) {
			clap.animation().map(Animated::animate).ifPresent(frame -> {
				var sprite = (Rectangle2D) frame;
				if (clap.isVisible()) {
					drawSpriteOverBoundingBox(g, sprite, clap.position().x(), clap.position().y());
				}
				var font = theme.font("font.arcade", 8);
				g.setFont(font);
				g.setFill(ArcadeTheme.PALE);
				g.fillText(clap.number(), clap.position().x() + sprite.getWidth() - 25, clap.position().y() + 18);
				g.fillText(clap.text(), clap.position().x() + sprite.getWidth(), clap.position().y());
			});
		}
	}

	public Rectangle2D heartSprite() {
		return tileFromThirdColumn(2, 10);
	}

	public Rectangle2D blueBagSprite() {
		return spritesheet().region(488, 199, 8, 8);
	}

	public Rectangle2D juniorPacSprite() {
		return spritesheet().region(509, 200, 8, 8);
	}

	public AnimationByDirection createPacManMunchingAnimationMap(Pac pac) {
		var animationByDir = new AnimationByDirection(pac::moveDir);
		for (var dir : Direction.values()) {
			int d = DIR_ORDER.index(dir);
			var animation = new SimpleAnimation<>(tileFromThirdColumn(0, 9 + d), tileFromThirdColumn(1, 9 + d),
					tileFromThirdColumn(2, 9));
			animation.setFrameDuration(2);
			animation.repeatForever();
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	public SimpleAnimation<Rectangle2D> createClapperboardAnimation() {
		// TODO this is not 100% accurate yet
		var animation = new SimpleAnimation<>( //
				spritesheet().region(456, 208, 32, 32), //
				spritesheet().region(488, 208, 32, 32), //
				spritesheet().region(520, 208, 32, 32), //
				spritesheet().region(488, 208, 32, 32), //
				spritesheet().region(456, 208, 32, 32)//
		);
		animation.setFrameDuration(4);
		return animation;
	}

	public SimpleAnimation<Rectangle2D> createStorkFlyingAnimation() {
		var animation = new SimpleAnimation<>( //
				spritesheet().region(489, 176, 32, 16), //
				spritesheet().region(521, 176, 32, 16) //
		);
		animation.repeatForever();
		animation.setFrameDuration(8);
		return animation;
	}
}