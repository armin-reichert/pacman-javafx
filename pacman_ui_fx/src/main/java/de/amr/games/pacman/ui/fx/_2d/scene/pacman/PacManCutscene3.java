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
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.SpritesheetPacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.app.Env;
import javafx.scene.canvas.GraphicsContext;
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
		frame = -1;
		initialDelay = 120;
		pac = new Pac("Pac-Man");
		pac.setAnimationSet(new PacAnimations(pac, ctx.r2D));
		blinky = new Ghost(Ghost.RED_GHOST, "Blinky");
		blinky.setAnimationSet(new GhostAnimations(blinky, ctx.r2D));
		blinky.animationSet().ifPresent(animations -> {
			animations.put("patched", SpritesheetPacMan.get().createBlinkyPatchedAnimation());
			animations.put("naked", SpritesheetPacMan.get().createBlinkyNakedAnimation());
		});
	}

	@Override
	protected void doUpdate() {
		if (initialDelay > 0) {
			--initialDelay;
			return;
		}
		++frame;
		if (frame == 0) {
			ctx.gameController.sounds().ifPresent(snd -> snd.loop(GameSound.INTERMISSION_1, 2));
			pac.placeAtTile(v(29, 20), 0, 0);
			pac.setMoveDir(Direction.LEFT);
			pac.setAbsSpeed(1.25);
			pac.show();
			pac.setAnimation(AnimKeys.PAC_MUNCHING);
			blinky.placeAtTile(v(35, 20), 0, 0);
			blinky.setBothDirs(Direction.LEFT);
			blinky.setAbsSpeed(1.25);
			blinky.show();
			blinky.setAnimation("patched");
		} else if (frame == 296) {
			blinky.placeAtTile(v(-1, 20), 0, 0);
			blinky.setBothDirs(Direction.RIGHT);
			blinky.setAnimation("naked");
		} else if (frame == 516) {
			ctx.state().timer().expire();
			return;
		}
		pac.move();
		pac.advance();
		blinky.move();
		blinky.advance();
	}

	@Override
	public void doRender(GraphicsContext g) {
		if (Env.debugUI.get()) {
			g.setFont(ctx.r2D.getArcadeFont());
			g.setFill(Color.WHITE);
			if (initialDelay > 0) {
				g.fillText("Wait %d".formatted(initialDelay), t(3), t(3));
			} else {
				g.fillText("Frame %d".formatted(frame), t(3), t(3));
			}
		}
		ctx.r2D.drawPac(g, pac);
		ctx.r2D.drawGhost(g, blinky);
	}
}