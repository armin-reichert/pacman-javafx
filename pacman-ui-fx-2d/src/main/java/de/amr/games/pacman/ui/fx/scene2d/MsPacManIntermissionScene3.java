/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.MsPacManIntermission3;
import de.amr.games.pacman.ui.fx.rendering2d.PacSpriteAnimationsMsPacMan;
import de.amr.games.pacman.ui.fx.rendering2d.SpriteAnimation;
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
	private SpriteAnimation storkAnimation;

	private MsPacManIntermission3.Context imc() {
		return intermission.context();
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		intermission = new MsPacManIntermission3(context.gameController());
		intermission.changeState(MsPacManIntermission3.State.INIT);

		imc().msPacMan.setAnimations(new PacSpriteAnimationsMsPacMan(imc().msPacMan, context.spritesheetMsPacMan()));
		imc().pacMan.setAnimations(new PacSpriteAnimationsMsPacMan(imc().pacMan, context.spritesheetMsPacMan()));

		storkAnimation = context.spritesheetMsPacMan().createStorkFlyingAnimation();
		storkAnimation.start();

		clapAnimation = context.spritesheetMsPacMan().createClapperboardAnimation();
		clapAnimation.setDelay(Duration.seconds(1));
		clapAnimation.start();
	}

	@Override
	public void update() {
		intermission.update();
	}

	@Override
	public void drawSceneContent() {
		drawClap(imc().clapperboard, clapAnimation);
		drawPacSprite(imc().msPacMan);
		drawPacSprite(imc().pacMan);
		drawEntitySprite(imc().stork, storkAnimation.frame());
		drawEntitySprite(imc().bag, imc().bagOpen ? context.spritesheetMsPacMan().juniorPacSprite()
				: context.spritesheetMsPacMan().blueBagSprite());
		drawLevelCounter(t(24), t(34), context.game().levelCounter());
	}
}