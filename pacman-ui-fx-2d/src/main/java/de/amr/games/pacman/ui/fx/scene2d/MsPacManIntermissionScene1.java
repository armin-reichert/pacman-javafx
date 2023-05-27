/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.MsPacManIntermission1;
import de.amr.games.pacman.lib.anim.AnimationMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameRenderer;

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

	private MsPacManIntermission1 im;
	private MsPacManIntermission1.Context imc;

	@Override
	protected MsPacManGameRenderer r() {
		return (MsPacManGameRenderer) super.r();
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		im = new MsPacManIntermission1(context.gameController());
		imc = im.context();
		im.changeState(MsPacManIntermission1.State.FLAP);

		imc.clapperboard.setAnimation(r().createClapperboardAnimation());
		imc.msPac.setAnimations(r().createPacAnimations(imc.msPac));
		imc.msPac.animations().ifPresent(AnimationMap::ensureRunning);
		imc.pacMan.setAnimations(r().createPacAnimations(imc.pacMan));
		imc.pacMan.animations().ifPresent(animations -> {
			var munching = r().createPacManMunchingAnimationMap(imc.pacMan);
			animations.put(GameModel.AK_PAC_MUNCHING, munching);
			animations.ensureRunning();
		});
		imc.inky.setAnimations(r().createGhostAnimations(imc.inky));
		imc.inky.animations().ifPresent(AnimationMap::ensureRunning);
		imc.pinky.setAnimations(r().createGhostAnimations(imc.pinky));
		imc.pinky.animations().ifPresent(AnimationMap::ensureRunning);
	}

	@Override
	public void update() {
		im.update();
	}

	@Override
	public void drawSceneContent() {
		drawClap(imc.clapperboard);
		drawPacSprite(imc.msPac);
		drawPacSprite(imc.pacMan);
		drawGhostSprite(imc.inky);
		drawGhostSprite(imc.pinky);
		drawEntitySprite(imc.heart, r().heartSprite());
		drawLevelCounter(t(24), t(34), context.game().levelCounter());
	}
}