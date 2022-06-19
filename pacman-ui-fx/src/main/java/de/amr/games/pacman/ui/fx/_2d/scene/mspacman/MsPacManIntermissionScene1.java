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

import de.amr.games.pacman.controller.mspacman.Intermission1Controller;
import de.amr.games.pacman.ui.fx._2d.rendering.common.GhostAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.common.PacAnimations;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Spritesheet_MsPacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
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
public class MsPacManIntermissionScene1 extends GameScene2D {

	private Intermission1Controller sceneController;
	private Intermission1Controller.Context icc;

	@Override
	public void setSceneContext(SceneContext context) {
		super.setSceneContext(context);
		sceneController = new Intermission1Controller(context.gameController);
		icc = sceneController.context();
	}

	@Override
	public void init() {
		sceneController.restartInInitialState(Intermission1Controller.State.FLAP);
		icc.flap.animation = Spritesheet_MsPacMan.get().createFlapAnimation();
		icc.msPac.setAnimations(new PacAnimations($.r2D));
		icc.msPac.animations().get().ensureRunning();
		icc.pacMan.setAnimations(new PacAnimations($.r2D));
		icc.pacMan.animations().get().put("munching", Spritesheet_MsPacMan.get().createPac_Man_MunchingAnimationMap());
		icc.pacMan.animations().get().ensureRunning();
		icc.inky.setAnimations(new GhostAnimations(icc.inky.id, $.r2D));
		icc.inky.animations().get().ensureRunning();
		icc.pinky.setAnimations(new GhostAnimations(icc.pinky.id, $.r2D));
		icc.pinky.animations().get().ensureRunning();
	}

	@Override
	public void doUpdate() {
		sceneController.update();
	}

	@Override
	public void doRender(GraphicsContext g) {
		var ss = (Spritesheet_MsPacMan) $.r2D;
		ss.drawFlap(g, icc.flap);
		$.r2D.drawPac(g, icc.msPac);
		$.r2D.drawPac(g, icc.pacMan);
		$.r2D.drawGhost(g, icc.inky);
		$.r2D.drawGhost(g, icc.pinky);
		$.r2D.drawEntity(g, icc.heart, ss.getHeart());
		$.r2D.drawLevelCounter(g, $.game.levelCounter);
	}
}