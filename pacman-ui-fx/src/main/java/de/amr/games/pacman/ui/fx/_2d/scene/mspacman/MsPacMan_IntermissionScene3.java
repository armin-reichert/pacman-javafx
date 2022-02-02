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

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.mspacman.Intermission3Controller;
import de.amr.games.pacman.ui.GameSounds;
import de.amr.games.pacman.ui.fx._2d.entity.common.Player2D;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.Flap2D;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.JuniorBag2D;
import de.amr.games.pacman.ui.fx._2d.entity.mspacman.Stork2D;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.ScenesMsPacMan;
import javafx.scene.canvas.Canvas;

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

	private final Intermission3Controller sc = new Intermission3Controller();
	private Player2D msPacMan2D;
	private Player2D pacMan2D;
	private Flap2D flap2D;
	private Stork2D stork2D;
	private JuniorBag2D bag2D;

	public MsPacMan_IntermissionScene3(GameController gameController, Canvas canvas) {
		super(gameController, canvas, ScenesMsPacMan.RENDERING);
	}

	@Override
	public void init() {
		super.init();

		sc.playIntermissionSound = () -> Env.sounds.play(GameSounds.INTERMISSION_3);
		sc.playFlapAnimation = () -> flap2D.animation.restart();
		sc.init(gameController);

		msPacMan2D = new Player2D(sc.msPacMan, r2D);
		pacMan2D = new Player2D(sc.pacMan, r2D);
		pacMan2D.munchingAnimations = ScenesMsPacMan.RENDERING.createSpouseMunchingAnimations();

		flap2D = new Flap2D(sc.flap, ScenesMsPacMan.RENDERING);

		stork2D = new Stork2D(sc.stork, ScenesMsPacMan.RENDERING);
		stork2D.animation.restart();

		bag2D = new JuniorBag2D(sc.bag, ScenesMsPacMan.RENDERING);
	}

	@Override
	public void doUpdate() {
		sc.updateState();
	}

	@Override
	public void doRender() {
		drawLevelCounter();
		flap2D.render(gc);
		msPacMan2D.render(gc);
		pacMan2D.render(gc);
		stork2D.render(gc);
		bag2D.render(gc);
	}
}