/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.MsPacManIntermission2;
import de.amr.games.pacman.ui.fx.rendering2d.PacSpriteAnimationsMsPacMan;
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

	private MsPacManIntermission2.Context imc() {
		return intermission.context();
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		intermission = new MsPacManIntermission2(context.gameController());
		intermission.changeState(MsPacManIntermission2.State.INIT);

		imc().msPac.setAnimations(new PacSpriteAnimationsMsPacMan(imc().msPac, context.spritesheetMsPacMan()));
		imc().pacMan.setAnimations(new PacSpriteAnimationsMsPacMan(imc().pacMan, context.spritesheetMsPacMan()));

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
		drawPacSprite(imc().msPac);
		drawPacSprite(imc().pacMan);
		drawLevelCounter(t(24), t(34), context.game().levelCounter());
	}
}