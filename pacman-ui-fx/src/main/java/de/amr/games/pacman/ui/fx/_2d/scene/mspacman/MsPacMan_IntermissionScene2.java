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

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.mspacman.Intermission2Controller;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.Flap2D;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.scene.ScenesMsPacMan;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene2 extends AbstractGameScene2D {

	private Intermission2Controller sceneController;
	private Player2D msPacMan2D;
	private Player2D pacMan2D;
	private Flap2D flap2D;

	public MsPacMan_IntermissionScene2() {
		super(ScenesMsPacMan.RENDERING, ScenesMsPacMan.SOUNDS);
	}

	@Override
	public void init(PacManGameController gameController) {
		super.init(gameController);

		sceneController = new Intermission2Controller(gameController);
		sceneController.playIntermissionSound = () -> sounds.play(PacManGameSound.INTERMISSION_2);
		sceneController.playFlapAnimation = () -> flap2D.animation.restart();
		sceneController.init();

		flap2D = new Flap2D(sceneController.flap, ScenesMsPacMan.RENDERING);

		msPacMan2D = new Player2D(sceneController.msPacMan, rendering);
		msPacMan2D.munchingAnimations.values().forEach(TimedSequence::restart);

		pacMan2D = new Player2D(sceneController.pacMan, rendering);
		pacMan2D.munchingAnimations = ScenesMsPacMan.RENDERING.createSpouseMunchingAnimations();
		pacMan2D.munchingAnimations.values().forEach(TimedSequence::restart);
	}

	@Override
	public void doUpdate() {
		sceneController.update();
	}

	@Override
	public void doRender() {
		renderLevelCounter();
		flap2D.render(gc);
		msPacMan2D.render(gc);
		pacMan2D.render(gc);
	}
}