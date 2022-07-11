/*
MIT License

Copyright (c) 2022 Armin Reichert

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

package de.amr.games.pacman.ui.fx._2d.scene.pacman;

import static de.amr.games.pacman.lib.V2i.v;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.animation.EntityAnimation;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.SpritesheetPacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.app.Env;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class PacManCutscene2 extends GameScene2D {

	private static final String DAMAGED = "damaged";

	private int initialDelay;
	private int frame;
	private Pac pac;
	private Ghost blinky;
	private EntityAnimation stretchedDressAnimation;
	private EntityAnimation damagedAnimation;

	@Override
	public void init() {
		frame = -1;
		initialDelay = 120;

		pac = new Pac("Pac-Man");
		pac.placeAtTile(v(29, 20), 0, 0);
		pac.setMoveDir(Direction.LEFT);
		pac.setAbsSpeed(1.15);
		pac.show();

		var pacAnimations = new PacAnimations(pac, ctx.r2D);
		pacAnimations.select(AnimKeys.PAC_MUNCHING);
		pacAnimations.byName(AnimKeys.PAC_MUNCHING).restart();
		pac.setAnimationSet(pacAnimations);

		stretchedDressAnimation = ((SpritesheetPacMan) ctx.r2D).createBlinkyStretchedAnimation();

		blinky = new Ghost(Ghost.RED_GHOST, "Blinky");
		blinky.placeAtTile(v(28, 20), 0, 0);
		blinky.setBothDirs(Direction.LEFT);
		blinky.setAbsSpeed(0);
		blinky.hide();

		var blinkyAnimations = new GhostAnimations(blinky, ctx.r2D);
		damagedAnimation = ((SpritesheetPacMan) ctx.r2D).createBlinkyDamagedAnimation();
		blinkyAnimations.put(DAMAGED, damagedAnimation);
		blinkyAnimations.select(AnimKeys.GHOST_COLOR);
		blinkyAnimations.byName(AnimKeys.GHOST_COLOR).restart();
		blinky.setAnimationSet(blinkyAnimations);

	}

	@Override
	protected void doUpdate() {
		if (initialDelay > 0) {
			--initialDelay;
			return;
		}
		++frame;
		if (frame == 0) {
			ctx.gameController.sounds().play(GameSound.INTERMISSION_1);
		} else if (frame == 110) {
			blinky.setAbsSpeed(1.25);
			blinky.show();
		} else if (frame == 196) {
			blinky.setAbsSpeed(0.17);
			stretchedDressAnimation.setFrameIndex(1);
		} else if (frame == 226) {
			stretchedDressAnimation.setFrameIndex(2);
		} else if (frame == 248) {
			blinky.setAbsSpeed(0);
			blinky.animationSet().ifPresent(animations -> animations.selectedAnimation().stop());
			stretchedDressAnimation.setFrameIndex(3);
		} else if (frame == 328) {
			stretchedDressAnimation.setFrameIndex(4);
		} else if (frame == 329) {
			blinky.animationSet().ifPresent(animations -> animations.select(DAMAGED));
			damagedAnimation.setFrameIndex(0);
		} else if (frame == 389) {
			damagedAnimation.setFrameIndex(1);
		} else if (frame == 508) {
			stretchedDressAnimation = null;
		} else if (frame == 509) {
			ctx.state().timer().expire();
			return;
		}
		pac.move();
		pac.advanceAnimation();
		blinky.move();
		blinky.advanceAnimation();
	}

	@Override
	public void drawSceneContent() {
		if (Env.showDebugInfoPy.get()) {
			g.setFont(ctx.r2D.getArcadeFont());
			g.setFill(Color.WHITE);
			if (initialDelay > 0) {
				g.fillText("Wait %d".formatted(initialDelay), t(3), t(3));
			} else {
				g.fillText("Frame %d".formatted(frame), t(3), t(3));
			}
		}
		if (stretchedDressAnimation != null) {
			ctx.r2D.drawSprite(g, (Rectangle2D) stretchedDressAnimation.frame(), t(14), t(19) + 3.0);
		}
		ctx.r2D.drawGhost(g, blinky);
		ctx.r2D.drawPac(g, pac);
	}
}