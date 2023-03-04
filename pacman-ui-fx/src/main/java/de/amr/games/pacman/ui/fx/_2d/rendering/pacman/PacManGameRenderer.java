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
import de.amr.games.pacman.ui.fx._2d.rendering.common.ArcadeTheme;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostColoring;
import de.amr.games.pacman.ui.fx._2d.rendering.common.MazeColoring;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpritesheetGameRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * @author Armin Reichert
 */
public class PacManGameRenderer extends SpritesheetGameRenderer {

	public PacManGameRenderer() {
		super(PacManGameAssets.SPRITESHEET);
	}

	@Override
	public MazeColoring mazeColors(int mazeNumber) {
		return PacManGameAssets.MAZE_COLORS;
	}

	@Override
	public GhostColoring ghostColoring(int ghostID) {
		return ArcadeTheme.GHOST_COLORS[ghostID];
	}

	@Override
	public Rectangle2D ghostValueRegion(int index) {
		return spritesheet.tile(index, 8);
	}

	@Override
	public Rectangle2D bonusSymbolRegion(int symbol) {
		return spritesheet.tile(2 + symbol, 3);
	}

	@Override
	public Rectangle2D bonusValueRegion(int symbol) {
		if (symbol <= 3) {
			return spritesheet.tile(symbol, 9);
		}
		if (symbol == 4) {
			var region = spritesheet.region(4, 9, 2, 1);
			return new Rectangle2D(region.getMinX(), region.getMinY(), region.getWidth() - 13, region.getHeight()); // WTF
		}
		return spritesheet.region(3, 5 + symbol, 3, 1);
	}

	@Override
	public EntityAnimationMap createWorldAnimations(World world) {
		var map = new EntityAnimationMap();
		map.put(AnimationKey.MAZE_ENERGIZER_BLINKING, new Pulse(10, true));
		map.put(AnimationKey.MAZE_FLASHING, new Pulse(10, true));
		return map;
	}

	@Override
	public void drawGhostFacingRight(GraphicsContext g, int ghostID, int x, int y) {
		var region = spritesheet.tile(2 * spritesheet.dirIndex(Direction.RIGHT), 4 + ghostID);
		drawSpriteCenteredOverBox(g, region, x, y);
	}

	@Override
	public void drawFlashingMaze(GraphicsContext g, int x, int y, int mazeNumber, World world) {
		boolean flash = false;
		var flashingAnimation = world.animation(AnimationKey.MAZE_FLASHING);
		if (flashingAnimation.isPresent()) {
			flash = (boolean) flashingAnimation.get().frame();
		}
		g.drawImage(flash ? PacManGameAssets.MAZE_EMPTY_INV : PacManGameAssets.MAZE_EMPTY, x, y);
	}

	@Override
	public void drawMaze(GraphicsContext g, int x, int y, int mazeNumber, World world, boolean energizerDark) {
		g.drawImage(PacManGameAssets.MAZE_FULL, x, y);
		world.tiles().filter(world::containsEatenFood).forEach(tile -> hideTileContent(g, tile));
		if (energizerDark) {
			world.energizerTiles().forEach(tile -> hideTileContent(g, tile));
		}
	}

	@Override
	public Rectangle2D lifeSymbolRegion() {
		return spritesheet.tile(8, 1);
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
			var wide = spritesheet.tile(0, d);
			var middle = spritesheet.tile(1, d);
			var closed = spritesheet.tile(2, 0);
			var animation = new SingleEntityAnimation<>(closed, closed, middle, middle, wide, wide, middle, middle);
			animation.setFrameDuration(1);
			animation.repeatForever();
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	private SingleEntityAnimation<Rectangle2D> createPacDyingAnimation() {
		var animation = new SingleEntityAnimation<>(spritesheet.tilesRightOf(3, 0, 11));
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
			var animation = new SingleEntityAnimation<>(spritesheet.tilesRightOf(2 * d, 4 + ghost.id(), 2));
			animation.setFrameDuration(8);
			animation.repeatForever();
			animationByDir.put(dir, animation);
		}
		return animationByDir;
	}

	private SingleEntityAnimation<Rectangle2D> createGhostBlueAnimation() {
		var animation = new SingleEntityAnimation<>(spritesheet.tile(8, 4), spritesheet.tile(9, 4));
		animation.setFrameDuration(8);
		animation.repeatForever();
		return animation;
	}

	private SingleEntityAnimation<Rectangle2D> createGhostFlashingAnimation() {
		var animation = new SingleEntityAnimation<>(spritesheet.tilesRightOf(8, 4, 4));
		animation.setFrameDuration(6);
		return animation;
	}

	private EntityAnimationByDirection createGhostEyesAnimation(Ghost ghost) {
		var animationByDir = new EntityAnimationByDirection(ghost::wishDir);
		for (var dir : Direction.values()) {
			int d = spritesheet.dirIndex(dir);
			animationByDir.put(dir, new SingleEntityAnimation<>(spritesheet.tile(8 + d, 5)));
		}
		return animationByDir;
	}

	private EntityAnimation createGhostValueSpriteList() {
		return new FixedEntityAnimation<>(ghostValueRegion(0), ghostValueRegion(1), ghostValueRegion(2),
				ghostValueRegion(3));
	}

	// Pac-Man specific:

	public SingleEntityAnimation<Rectangle2D> createBigPacManMunchingAnimation() {
		var animation = new SingleEntityAnimation<>(spritesheet.region(2, 1, 2, 2), spritesheet.region(4, 1, 2, 2),
				spritesheet.region(6, 1, 2, 2));
		animation.setFrameDuration(3);
		animation.repeatForever();
		return animation;
	}

	public FixedEntityAnimation<Rectangle2D> createBlinkyStretchedAnimation() {
		return new FixedEntityAnimation<>(spritesheet.tilesRightOf(8, 6, 5));
	}

	public FixedEntityAnimation<Rectangle2D> createBlinkyDamagedAnimation() {
		return new FixedEntityAnimation<>(spritesheet.tile(8, 7), spritesheet.tile(9, 7));
	}

	public SingleEntityAnimation<Rectangle2D> createBlinkyPatchedAnimation() {
		var animation = new SingleEntityAnimation<>(spritesheet.tile(10, 7), spritesheet.tile(11, 7));
		animation.setFrameDuration(4);
		animation.repeatForever();
		return animation;
	}

	public SingleEntityAnimation<Rectangle2D> createBlinkyNakedAnimation() {
		var animation = new SingleEntityAnimation<>(spritesheet.region(8, 8, 2, 1), spritesheet.region(10, 8, 2, 1));
		animation.setFrameDuration(4);
		animation.repeatForever();
		return animation;
	}
}