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
package de.amr.games.pacman.ui.fx._2d.scene.mspacman;

import static de.amr.games.pacman.model.world.World.t;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.mspacman.Intermission1Controller;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.ui.GameSounds;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.LevelCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.Flap2D;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.Heart2D;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Rendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.app.Env;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene1 extends AbstractGameScene2D {

	private final Intermission1Controller sc = new Intermission1Controller();

	private LevelCounter2D levelCounter2D;
	private Player2D msPacMan2D;
	private Player2D pacMan2D;
	private Ghost2D inky2D;
	private Ghost2D pinky2D;
	private Flap2D flap2D;
	private Heart2D heart2D;

	public MsPacMan_IntermissionScene1(GameController gameController) {
		super(gameController);
	}

	@Override
	public void init() {
		super.init();

		sc.playIntermissionSound = () -> Env.sounds.loop(GameSounds.INTERMISSION_1, 1);
		sc.playFlapAnimation = () -> flap2D.animation.restart();
		sc.init(gameController);

		levelCounter2D = new LevelCounter2D(game, Env.r2D);
		levelCounter2D.rightPosition = unscaledSize.minus(t(3), t(2));

		flap2D = new Flap2D(sc.flap, (Rendering2D_MsPacMan) Env.r2D);
		msPacMan2D = new Player2D(sc.msPac, Env.r2D);
		// overwrite by Pac-Man instead of Ms. Pac-Man sprites:
		pacMan2D = new Player2D(sc.pacMan, Env.r2D);
		pacMan2D.munchingAnimations = ((Rendering2D_MsPacMan) Env.r2D).createHusbandMunchingAnimations();
		inky2D = new Ghost2D(sc.inky, Env.r2D);
		pinky2D = new Ghost2D(sc.pinky, Env.r2D);
		heart2D = new Heart2D(sc.heart, (Rendering2D_MsPacMan) Env.r2D);

		// start animations
		msPacMan2D.munchingAnimations.values().forEach(TimedSequence::restart);
		pacMan2D.munchingAnimations.values().forEach(TimedSequence::restart);
		inky2D.kickingAnimations.values().forEach(TimedSequence::restart);
		pinky2D.kickingAnimations.values().forEach(TimedSequence::restart);
	}

	@Override
	public void doUpdate() {
		sc.updateState();
	}

	@Override
	public void doRender() {
		levelCounter2D.render(gc);
		flap2D.render(gc);
		msPacMan2D.render(gc);
		pacMan2D.render(gc);
		inky2D.render(gc);
		pinky2D.render(gc);
		heart2D.render(gc);
	}
}