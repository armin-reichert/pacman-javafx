/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.MsPacManIntermission1;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.ClapperBoardAnimation;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.GhostAnimationsMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.PacAnimationsMsPacManGame;

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

	private MsPacManIntermission1 intermission;
	private ClapperBoardAnimation clapAnimation;

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		intermission = new MsPacManIntermission1(context.gameController());

		var ss = context.ui().spritesheetMsPacManGame();
		intermission.msPac.setAnimations(new PacAnimationsMsPacManGame(intermission.msPac, ss));
		intermission.pacMan.setAnimations(new PacAnimationsMsPacManGame(intermission.pacMan, ss));
		intermission.inky.setAnimations(new GhostAnimationsMsPacManGame(intermission.inky, ss));
		intermission.pinky.setAnimations(new GhostAnimationsMsPacManGame(intermission.pinky, ss));

		clapAnimation = new ClapperBoardAnimation(ss.createClapperboardSprites(), "1", "THEY MEET");
		clapAnimation.start();
		intermission.changeState(MsPacManIntermission1.STATE_FLAP, 2 * 60);
	}

	@Override
	public void update() {
		intermission.tick();
		clapAnimation.tick();
	}

	@Override
	public void drawSceneContent() {
		if (intermission.clapVisible) {
			drawClapperBoard(clapAnimation, t(3), t(10));
		}
		drawPacSprite(intermission.msPac);
		drawPacSprite(intermission.pacMan);
		drawGhostSprite(intermission.inky);
		drawGhostSprite(intermission.pinky);
		drawEntitySprite(intermission.heart, context.ui().spritesheetMsPacManGame().heartSprite());
		drawLevelCounter(t(24), t(34), context.game().levelCounter());
	}

}