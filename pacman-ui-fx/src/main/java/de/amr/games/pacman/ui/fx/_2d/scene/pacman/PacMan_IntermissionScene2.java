/*
MIT License

Copyright (c) 2021 Armin Reichert

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

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.pacman.Intermission2Controller;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.GameSounds;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.entity.pacman.Nail2D;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Rendering2D_PacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.app.Env;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;

/**
 * Second intermission scene: Blinky pursues Pac but kicks a nail that tears his dress apart.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene2 extends AbstractGameScene2D {

	private final Intermission2Controller sc = new Intermission2Controller();
	private Player2D pacMan2D;
	private Ghost2D blinky2D;
	private Nail2D nail2D;
	private TimedSequence<Rectangle2D> blinkyStretchedAnimation;
	private TimedSequence<Rectangle2D> blinkyDamagedAnimation;

	public PacMan_IntermissionScene2(GameController gameController, Canvas canvas, Rendering2D_PacMan r2D) {
		super(gameController, canvas, r2D);
	}

	@Override
	public void init() {
		super.init();

		sc.playIntermissionSound = () -> Env.sounds.play(GameSounds.INTERMISSION_2);
		sc.init(gameController);

		pacMan2D = new Player2D(sc.pac, r2D);
		blinky2D = new Ghost2D(sc.blinky, r2D);
		nail2D = new Nail2D(sc.nail, (Rendering2D_PacMan) r2D);
		pacMan2D.munchingAnimations.values().forEach(TimedSequence::restart);
		blinky2D.kickingAnimations.values().forEach(TimedSequence::restart);
		blinkyStretchedAnimation = ((Rendering2D_PacMan) r2D).createBlinkyStretchedAnimation();
		blinkyDamagedAnimation = ((Rendering2D_PacMan) r2D).createBlinkyDamagedAnimation();
	}

	@Override
	public void doUpdate() {
		sc.updateState();
	}

	@Override
	public void doRender() {
		drawLevelCounter();
		pacMan2D.render(gc);
		nail2D.render(gc);
		if (sc.nailDistance() < 0) {
			blinky2D.render(gc);
		} else {
			drawBlinkyStretched(sc.blinky, sc.nail.position, sc.nailDistance() / 4);
		}
	}

	private void drawBlinkyStretched(Ghost blinky, V2d nailPosition, int stretching) {
		Rectangle2D stretchedDress = blinkyStretchedAnimation.frame(stretching);
		r2D.renderSprite(gc, stretchedDress, (int) (nailPosition.x - 4), (int) (nailPosition.y - 4));
		if (stretching < 3) {
			blinky2D.render(gc);
		} else {
			Rectangle2D damagedDress = blinkyDamagedAnimation.frame(blinky.dir() == Direction.UP ? 0 : 1);
			r2D.renderSprite(gc, damagedDress, (int) (blinky.position.x - 4), (int) (blinky.position.y - 4));
		}
	}
}