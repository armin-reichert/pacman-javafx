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

import de.amr.games.pacman.controller.mspacman.Intermission3Controller;
import de.amr.games.pacman.ui.GameSounds;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.Flap2D;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.JuniorBag2D;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.Stork2D;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.scene.ScenesMsPacMan;
import de.amr.games.pacman.ui.fx.shell.PacManGameUI_JavaFX;
import javafx.scene.Scene;

/**
 * Intermission scene 3: "Junior".
 * 
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle. The stork drops the
 * bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and finally opens up to reveal a tiny Pac-Man.
 * (Played after rounds 9, 13, and 17)
 * 
 * @author Armin Reichert
 */
public class MsPacMan_IntermissionScene3 extends AbstractGameScene2D {

	private final Intermission3Controller sceneController = new Intermission3Controller();

	private Player2D msPacMan2D;
	private Player2D pacMan2D;
	private Flap2D flap2D;
	private Stork2D stork2D;
	private JuniorBag2D bag2D;

	public MsPacMan_IntermissionScene3(PacManGameUI_JavaFX ui) {
		super(ui, ScenesMsPacMan.RENDERING, ScenesMsPacMan.SOUNDS);
	}

	@Override
	public void init(Scene parentScene) {
		super.init(parentScene);
		sceneController.playIntermissionSound = () -> sounds.play(GameSounds.INTERMISSION_3);
		sceneController.playFlapAnimation = () -> flap2D.animation.restart();
		sceneController.init(gameController);

		msPacMan2D = new Player2D(sceneController.msPacMan, rendering);
		pacMan2D = new Player2D(sceneController.pacMan, rendering);
		pacMan2D.munchingAnimations = ScenesMsPacMan.RENDERING.createSpouseMunchingAnimations();

		flap2D = new Flap2D(sceneController.flap, ScenesMsPacMan.RENDERING);

		stork2D = new Stork2D(sceneController.stork, ScenesMsPacMan.RENDERING);
		stork2D.animation.restart();

		bag2D = new JuniorBag2D(sceneController.bag, ScenesMsPacMan.RENDERING);
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
		stork2D.render(gc);
		bag2D.render(gc);
	}
}