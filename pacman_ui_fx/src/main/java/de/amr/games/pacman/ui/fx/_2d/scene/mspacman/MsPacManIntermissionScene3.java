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

import de.amr.games.pacman.controller.mspacman.MsPacManIntermission3;
import de.amr.games.pacman.lib.anim.SingleEntityAnimation;
import de.amr.games.pacman.model.common.actors.AnimKeys;
import de.amr.games.pacman.ui.fx._2d.rendering.RendererMsPacManGame;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import javafx.geometry.Rectangle2D;

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

	private MsPacManIntermission3 intermission;
	private SingleEntityAnimation<Rectangle2D> storkAnim;

	@Override
	public void init() {
		var renderer = (RendererMsPacManGame) ctx.r2D();
		intermission = new MsPacManIntermission3(ctx.gameController());
		intermission.restart(MsPacManIntermission3.IntermissionState.FLAP);
		intermission.context().clapperboard.setAnimationSet(renderer.createClapperboardAnimationSet());
		intermission.context().msPacMan.setAnimationSet(renderer.createPacAnimationSet(intermission.context().msPacMan));
		intermission.context().pacMan.setAnimationSet(renderer.createPacAnimationSet(intermission.context().pacMan));
		var munching = renderer.createPacManMunchingAnimationMap(intermission.context().pacMan);
		intermission.context().pacMan.animationSet().ifPresent(anims -> anims.put(AnimKeys.PAC_MUNCHING, munching));
		storkAnim = renderer.createStorkFlyingAnimation();
		storkAnim.ensureRunning();
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
		renderer.drawEntitySprite(g, intermission.context().stork, storkAnim.animate());
		renderer.drawEntitySprite(g, intermission.context().bag,
				intermission.context().bagOpen ? renderer.juniorPacSprite() : renderer.blueBagSprite());
		renderer.drawLevelCounter(g, ctx.game().levelCounter());
	}
}