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

import de.amr.games.pacman.controller.mspacman.Intermission1Controller;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.ui.GameSounds;
import de.amr.games.pacman.ui.fx._2d.entity.common.Ghost2D;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.Flap2D;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.Heart2D;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Rendering2D_MsPacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.scene.ScenesMsPacMan;
import de.amr.games.pacman.ui.fx.shell.PacManGameUI_JavaFX;
import javafx.scene.Scene;

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

	private final Intermission1Controller sceneController = new Intermission1Controller();

	private Player2D msPacMan2D;
	private Player2D pacMan2D;
	private Ghost2D inky2D;
	private Ghost2D pinky2D;
	private Flap2D flap2D;
	private Heart2D heart2D;

	public MsPacMan_IntermissionScene1(PacManGameUI_JavaFX ui) {
		super(ui, ScenesMsPacMan.RENDERING, ScenesMsPacMan.SOUNDS);
	}

	@Override
	public void init(Scene parentScene) {
		super.init(parentScene);

		sceneController.playIntermissionSound = () -> sounds.loop(GameSounds.INTERMISSION_1, 1);
		sceneController.playFlapAnimation = () -> flap2D.animation.restart();
		sceneController.init(gameController);

		flap2D = new Flap2D(sceneController.flap, ScenesMsPacMan.RENDERING);
		msPacMan2D = new Player2D(sceneController.msPac, rendering);
		// overwrite by Pac-Man instead of Ms. Pac-Man sprites:
		pacMan2D = new Player2D(sceneController.pacMan, rendering);
		pacMan2D.munchingAnimations = ScenesMsPacMan.RENDERING.createSpouseMunchingAnimations();
		inky2D = new Ghost2D(sceneController.inky, rendering);
		pinky2D = new Ghost2D(sceneController.pinky, rendering);
		heart2D = new Heart2D(sceneController.heart, (Rendering2D_MsPacMan) rendering);

		// start animations
		msPacMan2D.munchingAnimations.values().forEach(TimedSequence::restart);
		pacMan2D.munchingAnimations.values().forEach(TimedSequence::restart);
		inky2D.kickingAnimations.values().forEach(TimedSequence::restart);
		pinky2D.kickingAnimations.values().forEach(TimedSequence::restart);
	}

	@Override
	public void doUpdate() {
		sceneController.updateState();
	}

	@Override
	public void doRender() {
		renderLevelCounter();
		flap2D.render(gc);
		msPacMan2D.render(gc);
		pacMan2D.render(gc);
		inky2D.render(gc);
		pinky2D.render(gc);
		heart2D.render(gc);
	}
}