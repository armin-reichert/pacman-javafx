/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.MsPacManIntermission3;
import de.amr.games.pacman.lib.anim.SimpleAnimation;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameRenderer;
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

	private MsPacManIntermission3 im;
	private MsPacManIntermission3.Context imc;
	private SimpleAnimation<Rectangle2D> storkAnimation;

	@Override
	protected MsPacManGameRenderer r() {
		return (MsPacManGameRenderer) super.r();
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		im = new MsPacManIntermission3(context.gameController());
		imc = im.context();

		im.changeState(MsPacManIntermission3.State.FLAP);

		imc.clapperboard.setAnimation(r().createClapperboardAnimation());
		imc.msPacMan.setAnimations(r().createPacAnimations(imc.msPacMan));
		imc.pacMan.setAnimations(r().createPacAnimations(imc.pacMan));
		imc.pacMan.animations()
				.ifPresent(anims -> anims.put(GameModel.AK_PAC_MUNCHING, r().createPacManMunchingAnimationMap(imc.pacMan)));

		storkAnimation = r().createStorkFlyingAnimation();
		storkAnimation.start();
	}

	@Override
	public void update() {
		im.update();
		storkAnimation.animate();
	}

	@Override
	public void drawSceneContent() {
		r().drawClap(g, imc.clapperboard);
		r().drawPac(g, imc.msPacMan);
		r().drawPac(g, imc.pacMan);
		r().drawEntitySprite(g, imc.stork, storkAnimation.frame());
		r().drawEntitySprite(g, imc.bag, imc.bagOpen ? r().juniorPacSprite() : r().blueBagSprite());
		r().drawLevelCounter(g, t(24), t(34), context.game().levelCounter());
	}
}