/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.MsPacManIntermission1;
import de.amr.games.pacman.ui.fx.rendering2d.GhostSpriteAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameSpritesheet;
import de.amr.games.pacman.ui.fx.rendering2d.PacSpriteAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.SpriteAnimation;
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

	@Override
	protected MsPacManGameSpritesheet gss() {
		return (MsPacManGameSpritesheet) super.gss();
	}

	private MsPacManIntermission1.Context imc() {
		return intermission.context();
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		intermission = new MsPacManIntermission1(context.gameController());
		intermission.changeState(MsPacManIntermission1.State.INIT);

		imc().msPac.setAnimations(new PacSpriteAnimations(imc().msPac, gss()));
		imc().pacMan.setAnimations(new PacSpriteAnimations(imc().pacMan, gss()));
		imc().inky.setAnimations(new GhostSpriteAnimations(imc().inky, gss()));
		imc().pinky.setAnimations(new GhostSpriteAnimations(imc().pinky, gss()));

		clapAnimation = gss().createClapperboardAnimation();
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
		drawEntitySprite(imc().heart, gss().heartSprite());
		drawLevelCounter(t(24), t(34), context.game().levelCounter());
	}
}