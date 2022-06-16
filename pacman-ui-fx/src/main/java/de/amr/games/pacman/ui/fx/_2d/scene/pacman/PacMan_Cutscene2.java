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
import de.amr.games.pacman.lib.animation.SimpleThingAnimation;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.Pac;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Spritesheet_PacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.app.Env;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class PacMan_Cutscene2 extends GameScene2D {

	private int initialDelay;
	private int frame;
	private Pac pac;
	private Ghost blinky;
	private Rectangle2D nail;
	private SimpleThingAnimation<Rectangle2D> stretchedDress;
	private SimpleThingAnimation<Rectangle2D> damagedBlinky;

	@Override
	public void init() {
		frame = -1;
		initialDelay = 120;

		pac = new Pac("Pac-Man");
		pac.setAnimations(new PacAnimations(r2D));
		pac.animations().get().select("ANIM_MUNCHING");
		pac.animation("ANIM_MUNCHING").get().restart();
		pac.placeAt(v(29, 20), 0, 0);
		pac.setMoveDir(Direction.LEFT);
		pac.setAbsSpeed(1.15);
		pac.show();

		blinky = new Ghost(Ghost.RED_GHOST, "Blinky");
		blinky.setAnimations(new GhostAnimations(Ghost.RED_GHOST, r2D));
		blinky.animations().get().select("ANIM_COLOR");
		blinky.animation("ANIM_COLOR").get().restart();
		blinky.placeAt(v(28, 20), 0, 0);
		blinky.setBothDirs(Direction.LEFT);
		blinky.hide();

		stretchedDress = null;
		damagedBlinky = null;
		nail = ((Spritesheet_PacMan) r2D).getNail();
	}

	@Override
	protected void doUpdate() {
		if (initialDelay > 0) {
			--initialDelay;
			return;
		}
		++frame;
		if (frame == 0) {
			game.sounds().ifPresent(snd -> snd.play(GameSound.INTERMISSION_1));
		} else if (frame == 110) {
			blinky.setAbsSpeed(1.25);
			blinky.show();
		} else if (frame == 196) {
			blinky.setAbsSpeed(0.15);
			stretchedDress = ((Spritesheet_PacMan) r2D).createBlinkyStretchedAnimation();
			stretchedDress.setFrameIndex(0);
		} else if (frame == 226) {
			stretchedDress.setFrameIndex(1);
		} else if (frame == 248) {
			stretchedDress.setFrameIndex(2);
			blinky.setAbsSpeed(0);
			blinky.animations().get().selectedAnimation().stop();
		} else if (frame == 328) {
			stretchedDress.setFrameIndex(3);
		} else if (frame == 329) {
			damagedBlinky = ((Spritesheet_PacMan) r2D).createBlinkyDamagedAnimation();
			damagedBlinky.setFrameIndex(0);
		} else if (frame == 389) {
			damagedBlinky.setFrameIndex(1);
		} else if (frame == 509) {
			gameController.state().timer().expire();
			return;
		}
		pac.move();
		blinky.move();
	}

	@Override
	protected void doRender(GraphicsContext g) {
		if (Env.$debugUI.get()) {
			g.setFont(r2D.getArcadeFont());
			g.setFill(Color.WHITE);
			if (initialDelay > 0) {
				g.fillText("Wait %d".formatted(initialDelay), t(3), t(3));
			} else {
				g.fillText("Frame %d".formatted(frame), t(3), t(3));
			}
		}
		r2D.drawSprite(g, nail, t(14), t(19) + 3);
		if (stretchedDress != null) {
			r2D.drawSprite(g, stretchedDress.frame(), t(14), t(19) + 3);
		}
		if (damagedBlinky != null) {
			r2D.drawSpriteCenteredOverBox(g, damagedBlinky.frame(), blinky.position.x, blinky.position.y);
		} else {
			r2D.drawGhost(g, blinky);
		}
		r2D.drawPac(g, pac);
	}
}