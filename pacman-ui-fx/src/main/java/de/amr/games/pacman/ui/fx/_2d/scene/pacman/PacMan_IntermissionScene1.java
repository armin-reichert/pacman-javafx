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

import static de.amr.games.pacman.model.world.World.t;

import de.amr.games.pacman.controller.pacman.Intermission1Controller;
import de.amr.games.pacman.controller.pacman.Intermission1Controller.IntermissionState;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.ui.GameSounds;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.LevelCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.entity.pacman.BigPacMan2D;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Rendering2D_PacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.app.Env;

/**
 * First intermission scene: Blinky chases Pac-Man and is then chased by a huge Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene1 extends AbstractGameScene2D {

	private final Intermission1Controller sc = new Intermission1Controller();

	private LevelCounter2D levelCounter2D;
	private Player2D pacMan2D;
	private Ghost2D blinky2D;
	private BigPacMan2D bigPacMan2D;

	@Override
	public void init() {
		super.init();

		sc.playIntermissionSound = () -> Env.sounds.loop(GameSounds.INTERMISSION_1, 2);
		sc.init(gameController);

		levelCounter2D = new LevelCounter2D(game, Env.r2D);
		levelCounter2D.rightPosition = unscaledSize.minus(t(3), t(2));

		pacMan2D = new Player2D(sc.pac, Env.r2D);
		blinky2D = new Ghost2D(sc.blinky, Env.r2D);
		bigPacMan2D = new BigPacMan2D(sc.pac, (Rendering2D_PacMan) Env.r2D);
		pacMan2D.munchingAnimations.values().forEach(TimedSequence::restart);
		blinky2D.kickingAnimations.values().forEach(TimedSequence::restart);
		blinky2D.frightenedAnimation.restart();
		bigPacMan2D.munchingAnimation.restart();
	}

	@Override
	public void doUpdate() {
		sc.updateState();
	}

	@Override
	public void doRender() {
		levelCounter2D.render(gc);
		blinky2D.render(gc);
		if (sc.state == IntermissionState.CHASING_PACMAN) {
			pacMan2D.render(gc);
		} else {
			bigPacMan2D.render(gc);
		}
	}
}