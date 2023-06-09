/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.app.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.SpritesheetMsPacManGame;

/**
 * @author Armin Reichert
 */
public class MsPacManCreditScene extends GameScene2D {

	public MsPacManCreditScene(PacManGames2dUI ui) {
		super(ui);
	}

	@Override
	public void init() {
		setCreditVisible(true);
		setScoreVisible(true);
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.anyPressed(PacManGames2d.KEY_ADD_CREDIT, PacManGames2d.KEY_ADD_CREDIT_NUMPAD)) {
			ui().addCredit();
		} else if (Keyboard.anyPressed(PacManGames2d.KEY_START_GAME, PacManGames2d.KEY_START_GAME_NUMPAD)) {
			ui().startGame();
		}
	}

	@Override
	public void drawSceneContent() {
		var ss = (SpritesheetMsPacManGame) ui().spritesheet();
		var font6 = ui().theme().font("font.arcade", s(6));
		drawText("PUSH START BUTTON", ArcadeTheme.ORANGE, sceneFont(), t(6), t(16));
		drawText("1 PLAYER ONLY", ArcadeTheme.ORANGE, sceneFont(), t(8), t(18));
		drawText("ADDITIONAL    AT 10000", ArcadeTheme.ORANGE, sceneFont(), t(2), t(25));
		drawSprite(ss.livesCounterSprite(), t(13), t(23) + 1);
		drawText("PTS", ArcadeTheme.ORANGE, font6, t(25), t(25));
		drawMsPacManCopyright(t(6), t(28));
	}
}