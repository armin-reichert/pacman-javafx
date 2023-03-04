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
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.PacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.app.Env;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class PacManCutscene3 extends GameScene2D {

	private int initialDelay;
	private int frame;
	private Pac pac;
	private Ghost blinky;

	@Override
	public void init() {
		var renderer = (PacManGameRenderer) context.r2D();
		frame = -1;
		initialDelay = 120;

		pac = new Pac("Pac-Man");
		pac.placeAtTile(v2i(29, 20), 0, 0);
		pac.setMoveDir(Direction.LEFT);
		pac.setPixelSpeed(1.25f);
		pac.show();

		pac.setAnimations(renderer.createPacAnimations(pac));
		pac.selectAndRunAnimation(AnimationKey.PAC_MUNCHING);

		blinky = new Ghost(Ghost.ID_RED_GHOST, "Blinky");
		blinky.placeAtTile(v2i(35, 20), 0, 0);
		blinky.setMoveAndWishDir(Direction.LEFT);
		blinky.setPixelSpeed(1.25f);
		blinky.show();

		var blinkyAnimations = renderer.createGhostAnimations(blinky);
		blinkyAnimations.put(AnimationKey.BLINKY_PATCHED, renderer.createBlinkyPatchedAnimation());
		blinkyAnimations.put(AnimationKey.BLINKY_NAKED, renderer.createBlinkyNakedAnimation());
		blinky.setAnimations(blinkyAnimations);
		blinky.selectAndRunAnimation(AnimationKey.BLINKY_PATCHED);
	}

	@Override
	public void update() {
		if (initialDelay > 0) {
			--initialDelay;
			if (initialDelay == 0) {
				GameEvents.publishSoundEvent("start_intermission_3");
			}
			return;
		}
		if (context.state().timer().hasExpired()) {
			return;
		}
		switch (++frame) {
		case 400 -> {
			blinky.placeAtTile(v2i(-1, 20), 0, 0);
			blinky.setMoveAndWishDir(Direction.RIGHT);
			blinky.selectAndRunAnimation(AnimationKey.BLINKY_NAKED);
		}
		case 700 -> {
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
		context.r2D().drawPac(g, pac);
		context.r2D().drawGhost(g, blinky);
		context.r2D().drawLevelCounter(g, context.level().map(GameLevel::number), context.game().levelCounter());
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