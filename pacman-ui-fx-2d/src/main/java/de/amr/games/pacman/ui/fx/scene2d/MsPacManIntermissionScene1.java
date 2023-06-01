/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.MsPacManIntermission1;
import de.amr.games.pacman.ui.fx.rendering2d.GhostSpriteAnimationsMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.PacSpriteAnimationsMsPacManGame;
import de.amr.games.pacman.ui.fx.util.SpriteAnimation;
import javafx.util.Duration;

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
	private SpriteAnimation clapAnimation;

	private MsPacManIntermission1.Context imc() {
		return intermission.context();
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		intermission = new MsPacManIntermission1(context.gameController());
		intermission.changeState(MsPacManIntermission1.State.INIT);

		imc().msPac.setAnimations(new PacSpriteAnimationsMsPacManGame(imc().msPac, context.spritesheetMsPacMan()));
		imc().pacMan.setAnimations(new PacSpriteAnimationsMsPacManGame(imc().pacMan, context.spritesheetMsPacMan()));
		imc().inky.setAnimations(new GhostSpriteAnimationsMsPacManGame(imc().inky, context.spritesheetMsPacMan()));
		imc().pinky.setAnimations(new GhostSpriteAnimationsMsPacManGame(imc().pinky, context.spritesheetMsPacMan()));

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
		drawGhostSprite(imc().inky);
		drawGhostSprite(imc().pinky);
		drawEntitySprite(imc().heart, context.spritesheetMsPacMan().heartSprite());
		drawLevelCounter(t(24), t(34), context.game().levelCounter());
	}
}