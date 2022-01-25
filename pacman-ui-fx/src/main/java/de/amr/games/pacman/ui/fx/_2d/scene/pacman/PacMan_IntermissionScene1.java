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

import de.amr.games.pacman.controller.pacman.Intermission1Controller;
import de.amr.games.pacman.controller.pacman.Intermission1Controller.IntermissionState;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.ui.GameSounds;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.entity.pacman.BigPacMan2D;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.scene.ScenesPacMan;
import de.amr.games.pacman.ui.fx.shell.PacManGameUI_JavaFX;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.scene.Scene;

/**
 * First intermission scene: Blinky chases Pac-Man and is then chased by a huge Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene1 extends AbstractGameScene2D {

	private final Intermission1Controller sceneController = new Intermission1Controller();
	private final SoundManager sounds = ScenesPacMan.SOUNDS;
	private Player2D pacMan2D;
	private Ghost2D blinky2D;
	private BigPacMan2D bigPacMan2D;

	public PacMan_IntermissionScene1(PacManGameUI_JavaFX ui) {
		super(ui, ScenesPacMan.RENDERING);
	}

	@Override
	public void init(Scene parentScene) {
		super.init(parentScene);

		sceneController.playIntermissionSound = () -> sounds.loop(GameSounds.INTERMISSION_1, 2);
		sceneController.init(gameController);

		pacMan2D = new Player2D(sceneController.pac, r2D);
		blinky2D = new Ghost2D(sceneController.blinky, r2D);
		bigPacMan2D = new BigPacMan2D(sceneController.pac, ScenesPacMan.RENDERING);
		pacMan2D.munchingAnimations.values().forEach(TimedSequence::restart);
		blinky2D.kickingAnimations.values().forEach(TimedSequence::restart);
		blinky2D.frightenedAnimation.restart();
		bigPacMan2D.munchingAnimation.restart();
	}

	@Override
	public void doUpdate() {
		sceneController.updateState();
	}

	@Override
	public void doRender() {
		drawLevelCounter();
		blinky2D.render(gc);
		if (sceneController.currentStateID == IntermissionState.BLINKY_CHASING_PACMAN) {
			pacMan2D.render(gc);
		} else {
			bigPacMan2D.render(gc);
		}
	}
}