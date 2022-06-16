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
import de.amr.games.pacman.controller.pacman.Intermission1Controller;
import de.amr.games.pacman.controller.pacman.Intermission1Controller.State;
import de.amr.games.pacman.lib.animation.SingleSpriteAnimation;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Spritesheet_PacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * First intermission scene: Blinky chases Pac-Man and is then chased by a huge Pac-Man.
 * 
 * @author Armin Reichert
 */
public class PacMan_IntermissionScene1 extends GameScene2D {

	private Intermission1Controller sceneController;
	private Intermission1Controller.Context $;
	private SingleSpriteAnimation<Rectangle2D> bigPacMunchingAnimation;

	@Override
	public void setSceneContext(GameController gameController) {
		super.setSceneContext(gameController);
		sceneController = new Intermission1Controller(gameController);
		$ = sceneController.context();
	}

	@Override
	public void init() {
		sceneController.init();
		$.pac.setAnimations(new PacAnimations(r2D));
		$.pac.animations().get().ensureRunning();
		$.blinky.setAnimations(new GhostAnimations($.blinky.id, r2D));
		$.blinky.animations().get().ensureRunning();
		bigPacMunchingAnimation = Spritesheet_PacMan.get().createBigPacManMunchingAnimation();
		bigPacMunchingAnimation.ensureRunning();
	}

	@Override
	public void doUpdate() {
		sceneController.update();
	}

	@Override
	public void doRender(GraphicsContext g) {
		r2D.drawGhost(g, $.blinky);
		drawPac(g);
		r2D.drawLevelCounter(g, game.levelCounter);
	}

	private void drawPac(GraphicsContext g) {
		if (sceneController.state() == State.CHASING_PACMAN) {
			r2D.drawPac(g, $.pac);
		} else {
			var sprite = bigPacMunchingAnimation.animate();
			r2D.drawSpriteCenteredOverBox(g, sprite, $.pac.position.x, $.pac.position.y - sprite.getHeight() / 2 + 8);
		}
	}
}