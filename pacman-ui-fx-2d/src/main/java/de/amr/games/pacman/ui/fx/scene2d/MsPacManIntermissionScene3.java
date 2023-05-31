/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.MsPacManIntermission3;
import de.amr.games.pacman.lib.anim.SimpleAnimation;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameSpritesheet;
import de.amr.games.pacman.ui.fx.rendering2d.PacSpriteAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.SpriteAnimation;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;

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
	private SpriteAnimation clapAnimation;
	private SimpleAnimation<Rectangle2D> storkAnimation;

	@Override
	protected MsPacManGameSpritesheet gss() {
		return (MsPacManGameSpritesheet) super.gss();
	}

	private MsPacManIntermission3.Context imc() {
		return intermission.context();
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		intermission = new MsPacManIntermission3(context.gameController());
		intermission.changeState(MsPacManIntermission3.State.INIT);

		imc().msPacMan.setAnimations(new PacSpriteAnimations(imc().msPacMan, gss()));
		imc().pacMan.setAnimations(new PacSpriteAnimations(imc().pacMan, gss()));

		storkAnimation = gss().createStorkFlyingAnimation();
		storkAnimation.start();

		clapAnimation = gss().createClapperboardAnimation();
		clapAnimation.setDelay(Duration.seconds(1));
		clapAnimation.start();
	}

	@Override
	public void update() {
		intermission.update();
		storkAnimation.animate(); // TODO use SpriteAnimation
	}

	@Override
	public void drawSceneContent() {
		drawClap(imc().clapperboard, clapAnimation);
		drawPacSprite(imc().msPacMan);
		drawPacSprite(imc().pacMan);
		drawEntitySprite(imc().stork, storkAnimation.frame());
		drawEntitySprite(imc().bag, imc().bagOpen ? gss().juniorPacSprite() : gss().blueBagSprite());
		drawLevelCounter(t(24), t(34), context.game().levelCounter());
	}
}