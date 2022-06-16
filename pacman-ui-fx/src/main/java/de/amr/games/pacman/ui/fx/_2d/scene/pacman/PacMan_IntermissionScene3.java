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
package de.amr.games.pacman.ui.fx._2d.scene.pacman;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.pacman.Intermission3Controller;
import de.amr.games.pacman.lib.animation.SimpleAnimation;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Spritesheet_PacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * Third intermission scene: Blinky in shred dress chases Pac-Man, comes back half-naked drawing dress over the floor.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene3 extends GameScene2D {

	private Intermission3Controller sceneController;
	private Intermission3Controller.Context $;
	private SimpleAnimation<Rectangle2D> patchedAnimation, nakedAnimation;

	@Override
	public void setSceneContext(GameController gameController) {
		super.setSceneContext(gameController);
		sceneController = new Intermission3Controller(gameController);
		$ = sceneController.context();
	}

	@Override
	public void init() {
		sceneController.init();
		$.pac.setAnimations(new PacAnimations(r2D));
		$.pac.animations().get().ensureRunning();
		patchedAnimation = Spritesheet_PacMan.get().createBlinkyPatchedAnimation();
		patchedAnimation.ensureRunning();
		nakedAnimation = Spritesheet_PacMan.get().createBlinkyNakedAnimation();
		nakedAnimation.ensureRunning();
	}

	@Override
	public void doUpdate() {
		sceneController.update();
	}

	@Override
	public void doRender(GraphicsContext g) {
		r2D.drawLevelCounter(g, game.levelCounter);
		r2D.drawPac(g, $.pac);
		if (sceneController.state() == Intermission3Controller.State.CHASING) {
			r2D.drawSpriteCenteredOverBox(g, patchedAnimation.animate(), $.blinky.position.x, $.blinky.position.y);
		} else {
			r2D.drawSpriteCenteredOverBox(g, nakedAnimation.animate(), $.blinky.position.x, $.blinky.position.y);
		}
	}
}