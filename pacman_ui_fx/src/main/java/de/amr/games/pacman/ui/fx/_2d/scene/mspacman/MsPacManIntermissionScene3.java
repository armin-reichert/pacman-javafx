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

import de.amr.games.pacman.controller.mspacman.Intermission3Controller;
import de.amr.games.pacman.lib.animation.EntityAnimationSet;
import de.amr.games.pacman.lib.animation.SingleEntityAnimation;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.model.mspacman.Clapperboard;
import de.amr.games.pacman.ui.fx._2d.rendering.RendererMsPacManGame;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

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
public class MsPacManIntermissionScene3 extends GameScene2D {

	private Intermission3Controller intermission;
	private Intermission3Controller.Context intermissionData;
	private SingleEntityAnimation<Rectangle2D> storkAnim;

	@Override
	public void setContext(SceneContext sceneContext) {
		super.setContext(sceneContext);
		intermission = new Intermission3Controller(sceneContext.gameController());
		intermissionData = intermission.context();
	}

	@Override
	public void init() {
		var renderer = (RendererMsPacManGame) ctx.r2D();
		intermission.restartInState(Intermission3Controller.State.FLAP);
		var flapAnimationSet = new EntityAnimationSet<Integer>(1);
		flapAnimationSet.put(Clapperboard.ACTION, renderer.createClapperboardAnimation());
		flapAnimationSet.select(Clapperboard.ACTION);
		intermissionData.flap.setAnimationSet(flapAnimationSet);
		intermissionData.msPacMan.setAnimationSet(ctx.r2D().createPacAnimationSet(intermissionData.msPacMan));
		intermissionData.pacMan.setAnimationSet(ctx.r2D().createPacAnimationSet(intermissionData.pacMan));
		var munching = renderer.createPacManMunchingAnimationMap(intermissionData.pacMan);
		intermissionData.pacMan.animationSet().ifPresent(anims -> anims.put(AnimKeys.PAC_MUNCHING, munching));
		storkAnim = renderer.createStorkFlyingAnimation();
		storkAnim.ensureRunning();
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
		renderer.drawEntity(g, intermissionData.stork, storkAnim.animate());
		renderer.drawEntity(g, intermissionData.bag, intermissionData.bagOpen ? renderer.juniorPacSprite() : renderer.blueBagSprite());
		renderer.drawLevelCounter(g, ctx.game().levelCounter);
	}
}