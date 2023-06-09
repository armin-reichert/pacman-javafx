/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.MsPacManIntermission2;
import de.amr.games.pacman.ui.fx.app.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.ClapperBoardAnimation;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.PacAnimationsMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.SpritesheetMsPacManGame;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 * 
 * @author Armin Reichert
 */
public class MsPacManCutscene2 extends GameScene2D {

	private MsPacManIntermission2 intermission;
	private ClapperBoardAnimation clapAnimation;

	public MsPacManCutscene2(PacManGames2dUI ui) {
		super(ui);
	}

	@Override
	public void init() {
		var ss = (SpritesheetMsPacManGame) ui().spritesheet();
		setCreditVisible(true);
		setScoreVisible(true);
		intermission = new MsPacManIntermission2();
		intermission.msPac.setAnimations(new PacAnimationsMsPacManGame(intermission.msPac, ss));
		intermission.pacMan.setAnimations(new PacAnimationsMsPacManGame(intermission.pacMan, ss));
		clapAnimation = new ClapperBoardAnimation("2", "THE CHASE");
		clapAnimation.start();
		intermission.changeState(MsPacManIntermission2.STATE_FLAP, 2 * 60);
	}

	@Override
	public void update() {
		intermission.tick();
		clapAnimation.tick();
	}

	@Override
	public void drawSceneContent() {
		drawClapperBoard(clapAnimation, t(3), t(10));
		drawPacSprite(intermission.msPac);
		drawPacSprite(intermission.pacMan);
		drawLevelCounter(t(24), t(34), game().levelCounter());
	}
}