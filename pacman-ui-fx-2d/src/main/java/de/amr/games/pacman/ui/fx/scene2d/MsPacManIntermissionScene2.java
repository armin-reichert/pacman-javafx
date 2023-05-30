/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.MsPacManIntermission2;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameSpritesheet;
import de.amr.games.pacman.ui.fx.rendering2d.PacSpriteAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.SpriteAnimation;
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

	private MsPacManIntermission2 im;
	private MsPacManIntermission2.Context imc;
	private SpriteAnimation clapAnimation;

	@Override
	protected MsPacManGameSpritesheet gss() {
		return (MsPacManGameSpritesheet) super.gss();
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);

		im = new MsPacManIntermission2(context.gameController());
		imc = im.context();

		im.changeState(MsPacManIntermission2.State.FLAP);

		imc.msPacMan.setAnimations(new PacSpriteAnimations(imc.msPacMan, gss()));

		imc.pacMan.setAnimations(new PacSpriteAnimations(imc.pacMan, gss()));
		// TODO take Pac-Man animations from Ms. Pac-Man spritesheet
//		imc.pacMan.animations().ifPresent(animations -> {
//			var munching = gss().createPacManMunchingAnimationMap(imc.pacMan);
//			animations.put(GameModel.AK_PAC_MUNCHING, munching);
//			animations.ensureRunning();
//		});
		clapAnimation = gss().createClapperboardAnimation();
		clapAnimation.setDelay(Duration.seconds(1));
		clapAnimation.start();
	}

	@Override
	public void update() {
		im.update();
	}

	@Override
	public void drawSceneContent() {
		drawClap(imc.clapperboard, clapAnimation);
		drawPacSprite(imc.msPacMan);
		drawPacSprite(imc.pacMan);
		drawLevelCounter(t(24), t(34), context.game().levelCounter());
	}
}