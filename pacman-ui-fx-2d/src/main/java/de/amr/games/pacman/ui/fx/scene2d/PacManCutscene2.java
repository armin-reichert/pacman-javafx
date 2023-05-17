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

package de.amr.games.pacman.ui.fx.scene2d;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.v2i;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.event.GameEvents;
import de.amr.games.pacman.lib.anim.Animated;
import de.amr.games.pacman.lib.steering.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui.fx.app.Game2d;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameRenderer;
import de.amr.games.pacman.ui.fx.rendering2d.SpritesheetRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class PacManCutscene2 extends GameScene2D {

	private int initialDelay;
	private int frame;
	private Pac pac;
	private Ghost blinky;
	private Animated stretchedDressAnimation;
	private Animated damagedAnimation;

	public PacManCutscene2(GameController gameController) {
		super(gameController);
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		var renderer = (PacManGameRenderer) context.rendering2D();
		frame = -1;
		initialDelay = 120;

		pac = new Pac("Pac-Man");
		pac.placeAtTile(v2i(29, 20), 0, 0);
		pac.setMoveDir(Direction.LEFT);
		pac.setPixelSpeed(1.15f);
		pac.show();

		var pacAnimations = renderer.createPacAnimations(pac);
		pacAnimations.selectAndRestart(GameModel.AK_PAC_MUNCHING);
		pac.setAnimations(pacAnimations);

		stretchedDressAnimation = renderer.createBlinkyStretchedAnimation();

		blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
		blinky.placeAtTile(v2i(28, 20), 0, 0);
		blinky.setMoveAndWishDir(Direction.LEFT);
		blinky.setPixelSpeed(0);
		blinky.hide();

		var blinkyAnimations = renderer.createGhostAnimations(blinky);
		damagedAnimation = renderer.createBlinkyDamagedAnimation();
		blinkyAnimations.put(GameModel.AK_BLINKY_DAMAGED, damagedAnimation);
		blinkyAnimations.selectAndRestart(GameModel.AK_GHOST_COLOR);
		blinky.setAnimations(blinkyAnimations);
	}

	@Override
	public void update() {
		if (initialDelay > 0) {
			--initialDelay;
			if (initialDelay == 0) {
				GameEvents.publishSoundEvent(GameModel.SE_START_INTERMISSION_2);
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
			blinky.animations().ifPresent(animations -> animations.select(GameModel.AK_BLINKY_DAMAGED));
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
	public void drawScene(GraphicsContext g) {
		// TODO make this work for all renderers
		if (context.rendering2D() instanceof SpritesheetRenderer r) {
			if (stretchedDressAnimation != null) {
				r.drawSprite(g, (Rectangle2D) stretchedDressAnimation.frame(), TS * (14), TS * (19) + 3.0);
			}
			r.drawGhost(g, blinky);
			r.drawPac(g, pac);
			r.drawLevelCounter(g, context.game().levelCounter());
		}
	}

	@Override
	protected void drawInfo(GraphicsContext g) {
		if (Game2d.PY_SHOW_DEBUG_INFO.get()) {
			g.setFont(Game2d.assets.arcadeFont);
			g.setFill(Color.WHITE);
			if (initialDelay > 0) {
				g.fillText("Wait %d".formatted(initialDelay), TS * (1), TS * (5));
			} else {
				g.fillText("Frame %d".formatted(frame), TS * (1), TS * (5));
			}
		}
	}
}