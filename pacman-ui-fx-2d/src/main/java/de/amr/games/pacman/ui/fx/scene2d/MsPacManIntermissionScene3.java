/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.MsPacManIntermission3;
import de.amr.games.pacman.lib.anim.SimpleAnimation;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameSpritesheet;
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
	protected MsPacManGameSpritesheet gss() {
		return (MsPacManGameSpritesheet) super.gss();
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		im = new MsPacManIntermission3(context.gameController());
		imc = im.context();

		im.changeState(MsPacManIntermission3.State.FLAP);

		imc.clapperboard.setAnimation(gss().createClapperboardAnimation());
		imc.msPacMan.setAnimations(gss().createPacAnimations(imc.msPacMan));
		imc.pacMan.setAnimations(gss().createPacAnimations(imc.pacMan));
		imc.pacMan.animations()
				.ifPresent(anims -> anims.put(GameModel.AK_PAC_MUNCHING, gss().createPacManMunchingAnimationMap(imc.pacMan)));

		storkAnimation = gss().createStorkFlyingAnimation();
		storkAnimation.start();
	}

	@Override
	public void update() {
		im.update();
		storkAnimation.animate();
	}

	@Override
	public void drawSceneContent() {
		drawClap(imc.clapperboard);
		drawPacSprite(imc.msPacMan);
		drawPacSprite(imc.pacMan);
		drawEntitySprite(imc.stork, storkAnimation.frame());
		drawEntitySprite(imc.bag, imc.bagOpen ? gss().juniorPacSprite() : gss().blueBagSprite());
		drawLevelCounter(t(24), t(34), context.game().levelCounter());
	}
}