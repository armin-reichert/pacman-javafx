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

import de.amr.games.pacman.controller.mspacman.Intermission2Controller;
import de.amr.games.pacman.lib.animation.EntityAnimationSet;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.ui.fx._2d.rendering.RendererMsPacManGame;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.scene.SceneContext;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class MsPacManIntermissionScene2 extends GameScene2D {

	private Intermission2Controller intermission;

	@Override
	public void setContext(SceneContext sceneContext) {
		super.setContext(sceneContext);
		intermission = new Intermission2Controller(sceneContext.gameController());
	}

	@Override
	public void init() {
		var renderer = (RendererMsPacManGame) ctx.r2D();
		intermission.restartInState(Intermission2Controller.State.FLAP);
		intermission.context().clapperboard.setAnimationSet(renderer.createClapperboardAnimationSet());
		intermission.context().msPacMan.setAnimationSet(renderer.createPacAnimationSet(intermission.context().msPacMan));
		intermission.context().msPacMan.animationSet().ifPresent(EntityAnimationSet::ensureRunning);
		intermission.context().pacMan.setAnimationSet(renderer.createPacAnimationSet(intermission.context().pacMan));
		intermission.context().pacMan.animationSet().ifPresent(animations -> {
			var munching = renderer.createPacManMunchingAnimationMap(intermission.context().pacMan);
			animations.put(AnimKeys.PAC_MUNCHING, munching);
			animations.ensureRunning();
		});
	}

	@Override
	public void update() {
		intermission.update();
	}

	@Override
	public void draw() {
		var renderer = (RendererMsPacManGame) ctx.r2D();
		renderer.drawClapperboard(g, intermission.context().clapperboard);
		renderer.drawPac(g, intermission.context().msPacMan);
		renderer.drawPac(g, intermission.context().pacMan);
		renderer.drawLevelCounter(g, ctx.game().levelCounter);
	}
}