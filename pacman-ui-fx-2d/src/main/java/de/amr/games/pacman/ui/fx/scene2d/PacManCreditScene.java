/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadePalette;

import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.ui.fx.PacManGames2dUI.KEYS_ADD_CREDIT;
import static de.amr.games.pacman.ui.fx.PacManGames2dUI.KEYS_START_GAME;

/**
 * @author Armin Reichert
 */
public class PacManCreditScene extends GameScene2D {

	@Override
	public boolean isCreditVisible() {
		return true;
	}

	@Override
	public void init() {
		setScoreVisible(true);
	}

	@Override
	public void update() {
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.pressed(KEYS_ADD_CREDIT)) {
			context.actionHandler().addCredit();
		} else if (Keyboard.pressed(KEYS_START_GAME)) {
			context.actionHandler().startGame();
		}
	}

	@Override
	public void drawSceneContent() {
		var font8 = sceneFont(8);
		var font6 = sceneFont(6);
		drawText("PUSH START BUTTON", ArcadePalette.ORANGE, font8, t(6), t(17));
		drawText("1 PLAYER ONLY", ArcadePalette.CYAN, font8, t(8), t(21));
		drawText("BONUS PAC-MAN FOR 10000", ArcadePalette.ROSE, font8, t(1), t(25));
		drawText("PTS", ArcadePalette.ROSE, font6, t(25), t(25));
		drawMidwayCopyright(t(4), t(29));
		drawLevelCounter();
	}
}