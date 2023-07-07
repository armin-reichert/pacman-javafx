/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.ui.fx.app.ActionHandler;
import de.amr.games.pacman.ui.fx.app.PacManGames2dApp;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.SpritesheetMsPacManGame;

/**
 * @author Armin Reichert
 */
public class MsPacManCreditScene extends GameScene2D {

	@Override
	public void init() {
		setCreditVisible(true);
		setScoreVisible(true);
	}

	@Override
	public void update() {
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.anyPressed(PacManGames2dApp.KEY_ADD_CREDIT, PacManGames2dApp.KEY_ADD_CREDIT_NUMPAD)) {
			actionHandler().ifPresent(ActionHandler::addCredit);
		} else if (Keyboard.anyPressed(PacManGames2dApp.KEY_START_GAME, PacManGames2dApp.KEY_START_GAME_NUMPAD)) {
			actionHandler().ifPresent(ActionHandler::startGame);
		}
	}

	@Override
	public void drawSceneContent() {
		var ss = (SpritesheetMsPacManGame) spritesheet;
		var font6 = theme.font("font.arcade", s(6)); // TODO looks bad
		var font8 = sceneFont();
		var color = theme.color("palette.orange");
		drawText("PUSH START BUTTON", color, font8, t(6), t(16));
		drawText("1 PLAYER ONLY", color, font8, t(8), t(18));
		drawText("ADDITIONAL    AT 10000", color, font8, t(2), t(25));
		drawSprite(ss.livesCounterSprite(), t(13), t(23) + 1);
		drawText("PTS", color, font6, t(25), t(25));
		drawMsPacManCopyright(t(6), t(28));
	}
}