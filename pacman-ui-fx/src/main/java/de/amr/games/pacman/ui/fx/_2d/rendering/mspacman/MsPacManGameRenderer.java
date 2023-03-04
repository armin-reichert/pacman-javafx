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
import static de.amr.games.pacman.ui.fx._2d.rendering.mspacman.MsPacManGameAssets.col3;

import de.amr.games.pacman.lib.anim.AnimationKey;
import de.amr.games.pacman.lib.anim.EntityAnimation;
import de.amr.games.pacman.lib.anim.EntityAnimationByDirection;
import de.amr.games.pacman.lib.anim.EntityAnimationMap;
import de.amr.games.pacman.lib.anim.FixedEntityAnimation;
import de.amr.games.pacman.lib.anim.Pulse;
import de.amr.games.pacman.lib.anim.SingleEntityAnimation;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.model.mspacman.Clapperboard;
import de.amr.games.pacman.ui.fx._2d.rendering.common.ArcadeTheme;
import de.amr.games.pacman.ui.fx._2d.rendering.common.ArcadeTheme.Palette;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostColoring;
import de.amr.games.pacman.ui.fx._2d.rendering.common.MazeColoring;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpritesheetGameRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class MsPacManGameRenderer extends SpritesheetGameRenderer {

	public MsPacManGameRenderer() {
		super(MsPacManGameAssets.SPRITESHEET);
	}

	@Override
	public GhostColoring ghostColoring(int ghostID) {
		return ArcadeTheme.GHOST_COLORS[ghostID];
	}

	@Override
	public MazeColoring mazeColors(int mazeNumber) {
		return MsPacManGameAssets.COLORS[mazeNumber - 1];
	}

	@Override
	public Color ghostHouseDoorColor() {
		return MsPacManGameAssets.GHOSTHOUSE_DOOR_COLOR;
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
	public void drawGhostFacingRight(GraphicsContext g, int ghostID, int x, int y) {
		var region = col3(2 * spritesheet.dirIndex(Direction.RIGHT) + 1, 4 + ghostID);
		drawSpriteCenteredOverBox(g, region, x, y);
	}

	@Override
	public void drawMaze(GraphicsContext g, int x, int y, int mazeNumber, World world, boolean energizersHidden) {
		var w = MsPacManGameAssets.MAZE_WIDTH;
		var h = MsPacManGameAssets.MAZE_HEIGHT;
		g.drawImage(spritesheet.source(), 0, h * (mazeNumber - 1), w, h, x, y, w, h);
		world.tiles().filter(world::containsEatenFood).forEach(tile -> hideTileContent(g, tile));
		if (energizersHidden) {
			world.energizerTiles().forEach(tile -> hideTileContent(g, tile));
		}
	}

	@Override
	public void drawFlashingMaze(GraphicsContext g, int x, int y, int mazeNumber, World world, boolean flash) {
		var w = MsPacManGameAssets.MAZE_WIDTH;
		var h = MsPacManGameAssets.MAZE_HEIGHT;
		if (flash) {
			g.drawImage(MsPacManGameAssets.MAZES_EMPTY_FLASHING[mazeNumber - 1], x, y);
		} else {
			g.drawImage(spritesheet.source(), MsPacManGameAssets.SECOND_COLUMN, h * (mazeNumber - 1), w, h, x, y, w, h);
		}
	}

	@Override
	public Rectangle2D lifeSymbolRegion() {
		return col3(1, 0);
	}

	public void drawCopyright(GraphicsContext g, int tileY) {
		int x = t(6);
		int y = t(tileY - 1);
		g.drawImage(MsPacManGameAssets.MIDWAY_LOGO, x, y + 2, t(4) - 2, t(4));
		g.setFill(Palette.RED);
		g.setFont(Font.font("Dialog", 11));
		g.fillText("\u00a9", x + t(5), y + t(2) + 2); // (c) symbol
		g.setFont(ArcadeTheme.SCREEN_FONT);
		g.fillText("MIDWAY MFG CO", x + t(7), y + t(2));
		g.fillText("1980/1981", x + t(8), y + t(4));
	}

	// Animations

	@Override
	public EntityAnimationMap createWorldAnimations(World world) {
		var map = new EntityAnimationMap();
		map.put(AnimationKey.MAZE_ENERGIZER_BLINKING, new Pulse(10, true));
		map.put(AnimationKey.MAZE_FLASHING, new Pulse(10, true));
		return map;
	}

	@Override
	public EntityAnimationMap createPacAnimations(Pac pac) {
		var map = new EntityAnimationMap();
		map.put(AnimationKey.PAC_DYING, createPacDyingAnimation());
		map.put(AnimationKey.PAC_MUNCHING, createPacMunchingAnimation(pac));
		map.select(AnimationKey.PAC_MUNCHING);
		return map;
	}

	private EntityAnimationByDirection createPacMunchingAnimation(Pac pac) {
		var animationByDir = new EntityAnimationByDirection(pac::moveDir);
		for (var dir : Direction.values()) {
			int d = spritesheet.dirIndex(dir);
			var wide = col3(0, d);
			var middle = col3(1, d);
			var closed = col3(2, d);
			var munching = new SingleEntityAnimation<>(middle, middle, wide, wide, middle, middle, middle, closed, closed);
			munching.setFrameDuration(1);
			munching.repeatForever();
			animationByDir.put(dir, munching);
		}
		return animationByDir;
	}

	private SingleEntityAnimation<Rectangle2D> createPacDyingAnimation() {
		var right = col3(1, 0);
		var left = col3(1, 1);
		var up = col3(1, 2);
		var down = col3(1, 3);
		// TODO not yet 100% accurate
		var animation = new SingleEntityAnimation<>(down, left, up, right, down, left, up, right, down, left, up);
		animation.setFrameDuration(8);
		return animation;
	}

	@Override
	public EntityAnimationMap createGhostAnimations(Ghost ghost) {
		var map = new EntityAnimationMap();
		map.put(AnimationKey.GHOST_COLOR, createGhostColorAnimation(ghost));
		map.put(AnimationKey.GHOST_BLUE, createGhostBlueAnimation());
		map.put(AnimationKey.GHOST_EYES, createGhostEyesAnimation(ghost));
		map.put(AnimationKey.GHOST_FLASHING, createGhostFlashingAnimation());
		map.put(AnimationKey.GHOST_VALUE, createGhostValueSpriteList());
		map.select(AnimationKey.GHOST_COLOR);
		return map;
	}

	private EntityAnimationByDirection createGhostColorAnimation(Ghost ghost) {
		var animationByDir = new EntityAnimationByDirection(ghost::wishDir);
		for (var dir : Direction.values()) {
			int d = spritesheet.dirIndex(dir);
			var animation = new SingleEntityAnimation<>(col3(2 * d, 4 + ghost.id()), col3(2 * d + 1, 4 + ghost.id()));
			animation.setFrameDuration(8);
			animation.repeatForever();
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	private SingleEntityAnimation<Rectangle2D> createGhostBlueAnimation() {
		var animation = new SingleEntityAnimation<>(col3(8, 4), col3(9, 4));
		animation.setFrameDuration(8);
		animation.repeatForever();
		return animation;
	}

	private SingleEntityAnimation<Rectangle2D> createGhostFlashingAnimation() {
		var animation = new SingleEntityAnimation<>(col3(8, 4), col3(9, 4), col3(10, 4), col3(11, 4));
		animation.setFrameDuration(4);
		return animation;
	}

	private EntityAnimationByDirection createGhostEyesAnimation(Ghost ghost) {
		var animationByDir = new EntityAnimationByDirection(ghost::wishDir);
		for (var dir : Direction.values()) {
			int d = spritesheet.dirIndex(dir);
			animationByDir.put(dir, new SingleEntityAnimation<>(col3(8 + d, 5)));
		}
		return animationByDir;
	}

	private EntityAnimation createGhostValueSpriteList() {
		return new FixedEntityAnimation<>(ghostValueRegion(0), ghostValueRegion(1), ghostValueRegion(2),
				ghostValueRegion(3));
	}

	// Ms. Pac-Man specific:

	public void drawClap(GraphicsContext g, Clapperboard clap) {
		if (clap.isVisible()) {
			clap.animation().map(EntityAnimation::animate).ifPresent(frame -> {
				var sprite = (Rectangle2D) frame;
				drawEntitySprite(g, clap, sprite);
				g.setFont(ArcadeTheme.SCREEN_FONT);
				g.setFill(Palette.PALE);
				g.fillText(String.valueOf(clap.sceneNumber), clap.position().x() + sprite.getWidth() - 25,
						clap.position().y() + 18);
				g.fillText(clap.sceneTitle, clap.position().x() + sprite.getWidth(), clap.position().y());
			});
		}
	}

	public Rectangle2D heartSprite() {
		return col3(2, 10);
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
			int d = spritesheet.dirIndex(dir);
			var animation = new SingleEntityAnimation<>(col3(0, 9 + d), col3(1, 9 + d), col3(2, 9));
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