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

import de.amr.games.pacman.controller.mspacman.Intermission2Controller;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.ui.GameSounds;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.Flap2D;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.scene.ScenesMsPacMan;
import de.amr.games.pacman.ui.fx.shell.PacManGameUI_JavaFX;
import de.amr.games.pacman.ui.fx.sound.SoundManager;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene2 extends AbstractGameScene2D {

	private final Intermission2Controller sc = new Intermission2Controller();
	private final SoundManager sounds = ScenesMsPacMan.SOUNDS;
	private Player2D msPacMan2D;
	private Player2D pacMan2D;
	private Flap2D flap2D;

	public MsPacMan_IntermissionScene2(PacManGameUI_JavaFX ui) {
		super(ui, ScenesMsPacMan.RENDERING);
	}

	@Override
	public void init() {
		super.init();

		sc.playIntermissionSound = () -> sounds.play(GameSounds.INTERMISSION_2);
		sc.playFlapAnimation = () -> flap2D.animation.restart();
		sc.init(gameController);

		flap2D = new Flap2D(sc.flap, ScenesMsPacMan.RENDERING);

		msPacMan2D = new Player2D(sc.msPacMan, r2D);
		msPacMan2D.munchingAnimations.values().forEach(TimedSequence::restart);

		pacMan2D = new Player2D(sc.pacMan, r2D);
		pacMan2D.munchingAnimations = ScenesMsPacMan.RENDERING.createSpouseMunchingAnimations();
		pacMan2D.munchingAnimations.values().forEach(TimedSequence::restart);
	}

	@Override
	public void doUpdate() {
		sc.update();
	}

	@Override
	public void doRender() {
		drawLevelCounter();
		flap2D.render(gc);
		msPacMan2D.render(gc);
		pacMan2D.render(gc);
	}
}