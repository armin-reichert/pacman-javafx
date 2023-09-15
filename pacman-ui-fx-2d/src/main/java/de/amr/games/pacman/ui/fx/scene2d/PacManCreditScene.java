/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.ui.fx.app.ActionHandler;
import de.amr.games.pacman.ui.fx.input.Keyboard;

import static de.amr.games.pacman.ui.fx.app.PacManGames2dApp.*;

/**
 * @author Armin Reichert
 */
public class PacManCreditScene extends GameScene2D {

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
		if (Keyboard.anyPressed(KEY_ADD_CREDIT, KEY_ADD_CREDIT_NUMPAD)) {
			context.actionHandler().addCredit();
		} else if (Keyboard.anyPressed(KEY_START_GAME, KEY_START_GAME_NUMPAD)) {
			context.actionHandler().startGame();
		}
	}

	@Override
	public void drawSceneContent() {
		var theme = context.theme();
		var font8 = sceneFont(8);
		var font6 = sceneFont(6);
		drawText("PUSH START BUTTON", theme.color("palette.orange"), font8, t(6), t(17));
		drawText("1 PLAYER ONLY", theme.color("palette.cyan"), font8, t(8), t(21));
		drawText("BONUS PAC-MAN FOR 10000", theme.color("palette.rose"), font8, t(1), t(25));
		drawText("PTS", theme.color("palette.rose"), font6, t(25), t(25));
		drawMidwayCopyright(t(4), t(29));
		drawLevelCounter();
	}
}