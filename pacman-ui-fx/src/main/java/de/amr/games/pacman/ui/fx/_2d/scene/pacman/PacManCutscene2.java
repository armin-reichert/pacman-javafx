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

import static de.amr.games.pacman.lib.math.Vector2i.v2i;
import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.anim.AnimationKey;
import de.amr.games.pacman.lib.anim.EntityAnimation;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpritesheetGameRenderer;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.PacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.app.Env;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class PacManCutscene2 extends GameScene2D {

	private int initialDelay;
	private int frame;
	private Pac pac;
	private Ghost blinky;
	private EntityAnimation stretchedDressAnimation;
	private EntityAnimation damagedAnimation;

	@Override
	public void init() {
		var renderer = (PacManGameRenderer) context.r2D();
		frame = -1;
		initialDelay = 120;

		pac = new Pac("Pac-Man");
		pac.placeAtTile(v2i(29, 20), 0, 0);
		pac.setMoveDir(Direction.LEFT);
		pac.setPixelSpeed(1.15f);
		pac.show();

		var pacAnimations = renderer.createPacAnimations(pac);
		pacAnimations.selectAndRestart(AnimationKey.PAC_MUNCHING);
		pac.setAnimations(pacAnimations);

		stretchedDressAnimation = renderer.createBlinkyStretchedAnimation();

		blinky = new Ghost(Ghost.ID_RED_GHOST, "Blinky");
		blinky.placeAtTile(v2i(28, 20), 0, 0);
		blinky.setMoveAndWishDir(Direction.LEFT);
		blinky.setPixelSpeed(0);
		blinky.hide();

		var blinkyAnimations = renderer.createGhostAnimations(blinky);
		damagedAnimation = renderer.createBlinkyDamagedAnimation();
		blinkyAnimations.put(AnimationKey.BLINKY_DAMAGED, damagedAnimation);
		blinkyAnimations.selectAndRestart(AnimationKey.GHOST_COLOR);
		blinky.setAnimations(blinkyAnimations);
	}

	@Override
	public void update() {
		if (initialDelay > 0) {
			--initialDelay;
			if (initialDelay == 0) {
				GameEvents.publishSoundEvent("start_intermission_2");
			}
			return;
		}

		if (context.state().timer().hasExpired()) {
			return;
		}

		switch (++frame) {
		case 110 -> {
			blinky.setPixelSpeed(1.25f);
			blinky.show();
		}
		case 196 -> {
			blinky.setPixelSpeed(0.17f);
			stretchedDressAnimation.setFrameIndex(1);
		}
		case 226 -> {
			stretchedDressAnimation.setFrameIndex(2);
		}
		case 248 -> {
			blinky.setPixelSpeed(0);
			blinky.animations().ifPresent(animations -> animations.selectedAnimation().get().stop());
			stretchedDressAnimation.setFrameIndex(3);
		}
		case 328 -> {
			stretchedDressAnimation.setFrameIndex(4);
		}
		case 329 -> {
			blinky.animations().ifPresent(animations -> animations.select(AnimationKey.BLINKY_DAMAGED));
			damagedAnimation.setFrameIndex(0);
		}
		case 389 -> {
			damagedAnimation.setFrameIndex(1);
		}
		case 508 -> {
			stretchedDressAnimation = null;
		}
		case 509 -> {
			context.state().timer().expire();
		}
		default -> {
			pac.move();
			pac.animate();
			blinky.move();
			blinky.animate();
		}
		}
	}

	@Override
	public void drawSceneContent() {
		// TODO make this work for all renderers
		if (context.r2D() instanceof SpritesheetGameRenderer r) {
			if (stretchedDressAnimation != null) {
				r.drawSprite(g, (Rectangle2D) stretchedDressAnimation.frame(), t(14), t(19) + 3.0);
			}
			r.drawGhost(g, blinky);
			r.drawPac(g, pac);
			r.drawLevelCounter(g, context.level().map(GameLevel::number), context.game().levelCounter());
		}
	}

	@Override
	protected void drawOverlayPaneContent() {
		if (Env.showDebugInfoPy.get()) {
			g.setFont(context.r2D().screenFont(TS));
			g.setFill(Color.WHITE);
			if (initialDelay > 0) {
				g.fillText("Wait %d".formatted(initialDelay), t(1), t(5));
			} else {
				g.fillText("Frame %d".formatted(frame), t(1), t(5));
			}
		}
	}
}