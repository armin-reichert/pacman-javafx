/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;

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
		drawText("PUSH START BUTTON", ArcadeTheme.PALETTE_ORANGE, font8, t(6), t(17));
		drawText("1 PLAYER ONLY", ArcadeTheme.PALETTE_CYAN, font8, t(8), t(21));
		drawText("BONUS PAC-MAN FOR 10000", ArcadeTheme.PALETTE_ROSE, font8, t(1), t(25));
		drawText("PTS", ArcadeTheme.PALETTE_ROSE, font6, t(25), t(25));
		drawMidwayCopyright(t(4), t(29));
		drawLevelCounter();
	}
}