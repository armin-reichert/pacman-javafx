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
import de.amr.games.pacman.model.mspacman.Clapperboard;
import de.amr.games.pacman.ui.fx._2d.rendering.RendererMsPacManGame;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
import javafx.scene.canvas.GraphicsContext;

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
	private Intermission2Controller.Context intermissionData;

	@Override
	public void setContext(SceneContext sceneContext) {
		super.setContext(sceneContext);
		intermission = new Intermission2Controller(sceneContext.gameController());
		intermissionData = intermission.context();
	}

	@Override
	public void init() {
		var renderer = (RendererMsPacManGame) ctx.r2D();
		intermission.restartInState(Intermission2Controller.State.FLAP);
		var clapperboardAnimationSet = new EntityAnimationSet<Integer>(1);
		clapperboardAnimationSet.put(Clapperboard.ACTION, renderer.createClapperboardAnimation());
		clapperboardAnimationSet.select(Clapperboard.ACTION);
		intermissionData.flap.setAnimationSet(clapperboardAnimationSet);
		intermissionData.msPacMan.setAnimationSet(renderer.createPacAnimationSet(intermissionData.msPacMan));
		intermissionData.msPacMan.animationSet().ifPresent(EntityAnimationSet::ensureRunning);
		intermissionData.pacMan.setAnimationSet(renderer.createPacAnimationSet(intermissionData.pacMan));
		intermissionData.pacMan.animationSet().ifPresent(animations -> {
			var munching = renderer.createPacManMunchingAnimationMap(intermissionData.pacMan);
			animations.put(AnimKeys.PAC_MUNCHING, munching);
			animations.ensureRunning();
		});
	}

	@Override
	public void update() {
		intermission.update();
	}

	@Override
	public void draw(GraphicsContext g) {
		var renderer = (RendererMsPacManGame) ctx.r2D();
		renderer.drawClapperboard(g, intermissionData.flap);
		renderer.drawPac(g, intermissionData.msPacMan);
		renderer.drawPac(g, intermissionData.pacMan);
		renderer.drawLevelCounter(g, ctx.game().levelCounter);
	}
}