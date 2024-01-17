/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.MsPacManIntermission3;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.ClapperboardAnimation;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.PacAnimationsMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.SpritesheetMsPacManGame;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;

import static de.amr.games.pacman.lib.Globals.t;

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
public class MsPacManCutscene3 extends GameScene2D {

	private MsPacManIntermission3 intermission;
	private ClapperboardAnimation clapAnimation;
	private SpriteAnimation storkAnimation;

	@Override
	public void init() {
		var ss = (SpritesheetMsPacManGame) context.spritesheet();

		setCreditVisible(!GameController.it().hasCredit());
		setScoreVisible(true);

		intermission = new MsPacManIntermission3();
		intermission.msPacMan.setAnimations(new PacAnimationsMsPacManGame(intermission.msPacMan, ss));
		intermission.pacMan.setAnimations(new PacAnimationsMsPacManGame(intermission.pacMan, ss));
		storkAnimation = ss.createStorkFlyingAnimation();
		storkAnimation.start();
		clapAnimation = new ClapperboardAnimation("3", "JUNIOR");
		clapAnimation.start();
		intermission.changeState(MsPacManIntermission3.STATE_FLAP, TickTimer.INDEFINITE);
	}

	@Override
	public void update() {
		intermission.tick();
		clapAnimation.tick();
	}

	@Override
	public void drawSceneContent() {
		var ss = (SpritesheetMsPacManGame) context.spritesheet();
		drawClapperBoard(clapAnimation, t(3), t(10));
		drawPac(intermission.msPacMan);
		drawPac(intermission.pacMan);
		drawEntitySprite(intermission.stork, storkAnimation.currentSprite());
		drawEntitySprite(intermission.bag, intermission.bagOpen ? ss.juniorPacSprite() : ss.blueBagSprite());
		drawLevelCounter();
	}
}