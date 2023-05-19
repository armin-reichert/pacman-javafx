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

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.MsPacManIntermission2;
import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameRenderer;
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

	private MsPacManIntermission2 im;
	private MsPacManIntermission2.Context imc;
	private MsPacManGameRenderer r;

	public MsPacManIntermissionScene2(GameController gameController) {
		super(gameController);
	}

	@Override
	public void init() {
		r = (MsPacManGameRenderer) context.rendering2D();

		context.setCreditVisible(true);
		context.setScoreVisible(true);

		im = new MsPacManIntermission2(context.gameController());
		imc = im.context();

		im.changeState(MsPacManIntermission2.State.FLAP);

		imc.clapperboard.setAnimation(r.createClapperboardAnimation());
		imc.msPacMan.setAnimations(r.createPacAnimations(imc.msPacMan));
		imc.msPacMan.animations().ifPresent(AnimationMap::ensureRunning);
		imc.pacMan.setAnimations(r.createPacAnimations(imc.pacMan));
		imc.pacMan.animations().ifPresent(animations -> {
			var munching = r.createPacManMunchingAnimationMap(imc.pacMan);
			animations.put(GameModel.AK_PAC_MUNCHING, munching);
			animations.ensureRunning();
		});
	}

	@Override
	public void update() {
		im.update();
	}

	@Override
	public void drawSceneContent(GraphicsContext g) {
		r.drawClap(g, imc.clapperboard);
		r.drawPac(g, imc.msPacMan);
		r.drawPac(g, imc.pacMan);
		r.drawLevelCounter(g, t(24), t(34), context.game().levelCounter());
	}
}