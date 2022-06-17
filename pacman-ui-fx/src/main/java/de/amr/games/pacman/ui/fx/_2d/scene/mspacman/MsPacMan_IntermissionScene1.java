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
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Spritesheet_MsPacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
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

	@Override
	public void setSceneContext(GameController gameController) {
		super.setSceneContext(gameController);
		sceneController = new Intermission1Controller(gameController);
		$ = sceneController.context();
	}

	@Override
	public void init() {
		sceneController.restartInInitialState(Intermission1Controller.State.FLAP);
		$.flap.animation = Spritesheet_MsPacMan.get().createFlapAnimation();
		$.msPac.setAnimations(new PacAnimations(r2D));
		$.msPac.animations().get().ensureRunning();
		$.pacMan.setAnimations(new PacAnimations(r2D));
		$.pacMan.animations().get().put("munching", Spritesheet_MsPacMan.get().createPac_Man_MunchingAnimationMap());
		$.pacMan.animations().get().ensureRunning();
		$.inky.setAnimations(new GhostAnimations($.inky.id, r2D));
		$.inky.animations().get().ensureRunning();
		$.pinky.setAnimations(new GhostAnimations($.pinky.id, r2D));
		$.pinky.animations().get().ensureRunning();
	}

	@Override
	public void doUpdate() {
		sceneController.update();
	}

	@Override
	public void doRender(GraphicsContext g) {
		var ss = (Spritesheet_MsPacMan) r2D;
		ss.drawFlap(g, $.flap);
		r2D.drawPac(g, $.msPac);
		r2D.drawPac(g, $.pacMan);
		r2D.drawGhost(g, $.inky);
		r2D.drawGhost(g, $.pinky);
		r2D.drawEntity(g, $.heart, ss.getHeart());
		r2D.drawLevelCounter(g, game.levelCounter);
	}
}