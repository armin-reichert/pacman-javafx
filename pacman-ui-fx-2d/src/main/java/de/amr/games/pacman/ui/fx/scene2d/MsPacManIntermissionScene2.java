/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.MsPacManIntermission2;
import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameRenderer;

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

	@Override
	protected MsPacManGameRenderer r() {
		return (MsPacManGameRenderer) super.r();
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		im = new MsPacManIntermission2(context.gameController());
		imc = im.context();

		im.changeState(MsPacManIntermission2.State.FLAP);

		imc.clapperboard.setAnimation(r().createClapperboardAnimation());
		imc.msPacMan.setAnimations(r().createPacAnimations(imc.msPacMan));
		imc.msPacMan.animations().ifPresent(AnimationMap::ensureRunning);
		imc.pacMan.setAnimations(r().createPacAnimations(imc.pacMan));
		imc.pacMan.animations().ifPresent(animations -> {
			var munching = r().createPacManMunchingAnimationMap(imc.pacMan);
			animations.put(GameModel.AK_PAC_MUNCHING, munching);
			animations.ensureRunning();
		});
	}

	@Override
	public void update() {
		im.update();
	}

	@Override
	public void drawSceneContent() {
		drawClap(imc.clapperboard);
		drawPacSprite(imc.msPacMan);
		drawPacSprite(imc.pacMan);
		drawLevelCounter(t(24), t(34), context.game().levelCounter());
	}
}