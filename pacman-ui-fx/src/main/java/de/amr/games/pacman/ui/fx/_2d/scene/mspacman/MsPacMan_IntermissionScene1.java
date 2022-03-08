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

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.mspacman.Intermission1Controller;
import de.amr.games.pacman.controller.mspacman.Intermission1Controller.IntermissonState;
import de.amr.games.pacman.lib.TimedSeq;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.GameSounds;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.LevelCounter2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.Flap2D;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.Heart2D;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Rendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import javafx.scene.canvas.Canvas;

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

	private final Intermission1Controller sc;
	private LevelCounter2D levelCounter2D;
	private Player2D msPacMan2D;
	private Player2D pacMan2D;
	private Ghost2D inky2D;
	private Ghost2D pinky2D;
	private Flap2D flap2D;
	private Heart2D heart2D;

	public MsPacMan_IntermissionScene1(GameController gameController, Canvas canvas, V2i unscaledSize) {
		super(gameController, canvas, unscaledSize);
		sc = new Intermission1Controller(gameController);
		sc.playIntermissionSound = () -> sounds.loop(GameSounds.INTERMISSION_1, 1);
		sc.playFlapAnimation = () -> flap2D.animation.restart();
	}

	@Override
	public void init() {
		super.init();
		sc.init();

		levelCounter2D = new LevelCounter2D(game, r2D);
		levelCounter2D.rightPosition = unscaledSize.minus(t(3), t(2));

		flap2D = new Flap2D(sc.flap, game, (Rendering2D_MsPacMan) r2D);
		msPacMan2D = new Player2D(sc.msPac, game, r2D);
		// overwrite by Pac-Man instead of Ms. Pac-Man sprites:
		pacMan2D = new Player2D(sc.pacMan, game, r2D);
		pacMan2D.munchings = ((Rendering2D_MsPacMan) r2D).createHusbandMunchingAnimations();
		inky2D = new Ghost2D(sc.inky, game, r2D);
		pinky2D = new Ghost2D(sc.pinky, game, r2D);
		heart2D = new Heart2D(sc.heart, game, (Rendering2D_MsPacMan) r2D);

		// start animations
		msPacMan2D.munchings.values().forEach(TimedSeq::restart);
		pacMan2D.munchings.values().forEach(TimedSeq::restart);
		inky2D.animKicking.values().forEach(TimedSeq::restart);
		pinky2D.animKicking.values().forEach(TimedSeq::restart);
	}

	@Override
	public void doUpdate() {
		sc.updateState();
		// stop ghost animation when Pac-Man and Ms. Pac-Man are in heaven
		if (sc.state == IntermissonState.IN_HEAVEN && sc.pacMan.velocity.equals(V2d.NULL)) {
			inky2D.animKicking.values().forEach(TimedSeq::stop);
			pinky2D.animKicking.values().forEach(TimedSeq::stop);
		}
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