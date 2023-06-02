/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.MsPacManIntermission3;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.PacAnimationsMsPacManGame;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
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

		var ss = context.ui().spritesheetMsPacManGame();
		imc().msPacMan.setAnimations(new PacAnimationsMsPacManGame(imc().msPacMan, ss));
		imc().pacMan.setAnimations(new PacAnimationsMsPacManGame(imc().pacMan, ss));

		storkAnimation = ss.createStorkFlyingAnimation();
		storkAnimation.start();

		clapAnimation = ss.createClapperboardAnimation();
		clapAnimation.setDelay(Duration.seconds(1));
		clapAnimation.start();
	}

	@Override
	public void update() {
		intermission.update();
	}

	@Override
	public void drawSceneContent() {
		var ss = context.ui().spritesheetMsPacManGame();
		drawClap(imc().clapperboard, clapAnimation);
		drawPacSprite(imc().msPacMan);
		drawPacSprite(imc().pacMan);
		drawEntitySprite(imc().stork, storkAnimation.currentSprite());
		drawEntitySprite(imc().bag, imc().bagOpen ? ss.juniorPacSprite() : ss.blueBagSprite());
		drawLevelCounter(t(24), t(34), context.game().levelCounter());
	}
}