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
import de.amr.games.pacman.lib.animation.EntityAnimationSet;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.ArcadeRendererMsPacManGame;
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
		sceneController = new Intermission1Controller(context.gameController());
		icc = sceneController.context();
	}

	@Override
	public void init() {
		sceneController.restartInState(Intermission1Controller.State.FLAP);
		var flapAnimationSet = new EntityAnimationSet(1);
		flapAnimationSet.put("flap", ArcadeRendererMsPacManGame.get().createFlapAnimation());
		flapAnimationSet.select("flap");
		icc.flap.setAnimationSet(flapAnimationSet);
		icc.msPac.setAnimationSet(ctx.r2D().createPacAnimationSet(icc.msPac));
		icc.msPac.animationSet().ifPresent(EntityAnimationSet::ensureRunning);
		icc.pacMan.setAnimationSet(ctx.r2D().createPacAnimationSet(icc.pacMan));
		icc.pacMan.animationSet().ifPresent(animations -> {
			var munching = ArcadeRendererMsPacManGame.get().createPacManMunchingAnimationMap(icc.pacMan);
			animations.put(AnimKeys.PAC_MUNCHING, munching);
			animations.ensureRunning();
		});
		icc.inky.setAnimationSet(ctx.r2D().createGhostAnimationSet(icc.inky));
		icc.inky.animationSet().ifPresent(EntityAnimationSet::ensureRunning);
		icc.pinky.setAnimationSet(ctx.r2D().createGhostAnimationSet(icc.pinky));
		icc.pinky.animationSet().ifPresent(EntityAnimationSet::ensureRunning);
	}

	@Override
	public void update() {
		sceneController.update();
	}

	@Override
	public void drawSceneContent(GraphicsContext g) {
		var ss = (ArcadeRendererMsPacManGame) ctx.r2D();
		ss.drawFlap(g, icc.flap);
		ctx.r2D().drawPac(g, icc.msPac);
		ctx.r2D().drawPac(g, icc.pacMan);
		ctx.r2D().drawGhost(g, icc.inky);
		ctx.r2D().drawGhost(g, icc.pinky);
		ctx.r2D().drawEntity(g, icc.heart, ss.getHeart());
		ctx.r2D().drawLevelCounter(g, ctx.game().levelCounter);
	}
}