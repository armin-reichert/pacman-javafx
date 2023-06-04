/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.MsPacManIntermission2;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.PacAnimationsMsPacManGame;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import javafx.util.Duration;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class MsPacManIntermissionScene2 extends GameScene2D {

	private MsPacManIntermission2 intermission;
	private SpriteAnimation clapAnimation;

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);
		intermission = new MsPacManIntermission2(context.gameController());
		var ss = context.ui().spritesheetMsPacManGame();
		intermission.msPac.setAnimations(new PacAnimationsMsPacManGame(intermission.msPac, ss));
		intermission.pacMan.setAnimations(new PacAnimationsMsPacManGame(intermission.pacMan, ss));
		clapAnimation = ss.createClapperboardAnimation();
		clapAnimation.setDelay(Duration.seconds(1));
		clapAnimation.start();
		intermission.changeState(MsPacManIntermission2.STATE_FLAP, 2 * 60);
	}

	@Override
	public void update() {
		intermission.tick();
	}

	@Override
	public void drawSceneContent() {
		if (intermission.clapVisible) {
			drawClap("2", "THE CHASE", t(3), t(10), clapAnimation);
		}
		drawPacSprite(intermission.msPac);
		drawPacSprite(intermission.pacMan);
		drawLevelCounter(t(24), t(34), context.game().levelCounter());
	}
}