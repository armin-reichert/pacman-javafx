/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.mspacman.Intermission1Controller;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.Flap2D;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.Heart2D;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.MsPacMansHusbandAnimations;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.sound.GameSound;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.scene.canvas.GraphicsContext;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene1 extends GameScene2D {

	private Intermission1Controller sceneController;
	private Intermission1Controller.Context $;
	private Flap2D flap2D;
	private Heart2D heart2D;

	@Override
	public void setSceneContext(GameController gameController) {
		super.setSceneContext(gameController);
		sceneController = new Intermission1Controller(gameController);
		$ = sceneController.context();
		sceneController.playIntermissionSound = () -> SoundManager.get().loop(GameSound.INTERMISSION_1, 1);
		sceneController.playFlapAnimation = () -> flap2D.playAnimation();
		sceneController.addStateChangeListener(this::onSceneStateChange);
	}

	@Override
	public void init() {
		sceneController.restartInInitialState(Intermission1Controller.State.FLAP);
		flap2D = new Flap2D($.flap);
		$.msPac.setAnimations(new PacAnimations(r2D));
		$.msPac.animations().get().ensureRunning();
		$.pacMan.setAnimations(new MsPacMansHusbandAnimations());
		$.pacMan.animations().get().ensureRunning();
		$.inky.setAnimations(new GhostAnimations($.inky.id, r2D));
		$.inky.animations().get().ensureRunning();
		$.pinky.setAnimations(new GhostAnimations($.pinky.id, r2D));
		$.pinky.animations().get().ensureRunning();
		heart2D = new Heart2D($.heart);
	}

	@Override
	public void doUpdate() {
		sceneController.update();
	}

	private void onSceneStateChange(Intermission1Controller.State fromState, Intermission1Controller.State toState) {
		if (toState == Intermission1Controller.State.IN_HEAVEN) {
			$.inky.animations().get().selectedAnimation().stop();
			$.pinky.animations().get().selectedAnimation().stop();
		}
	}

	@Override
	public void doRender(GraphicsContext g) {
		r2D.drawLevelCounter(g, game.levelCounter);
		flap2D.render(g, r2D);
		r2D.drawPac(g, $.msPac);
		r2D.drawPac(g, $.pacMan);
		r2D.drawGhost(g, $.inky);
		r2D.drawGhost(g, $.pinky);
		heart2D.render(g, r2D);
	}
}