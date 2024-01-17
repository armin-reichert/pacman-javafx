/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadePalette;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.SpritesheetMsPacManGame;

import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.ui.fx.PacManGames2dApp.KEYS_ADD_CREDIT;
import static de.amr.games.pacman.ui.fx.PacManGames2dApp.KEYS_START_GAME;

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
		if (Keyboard.pressed(KEYS_ADD_CREDIT)) {
			context.actionHandler().addCredit();
		} else if (Keyboard.pressed(KEYS_START_GAME)) {
			context.actionHandler().startGame();
		}
	}

	@Override
	public void drawSceneContent() {
		var ss = (SpritesheetMsPacManGame) context.spritesheet();
		var font6 = sceneFont(6); // TODO looks bad
		var font8 = sceneFont(8);
		var color = ArcadePalette.ORANGE;
		drawText("PUSH START BUTTON", color, font8, t(6), t(16));
		drawText("1 PLAYER ONLY", color, font8, t(8), t(18));
		drawText("ADDITIONAL    AT 10000", color, font8, t(2), t(25));
		drawSprite(ss.livesCounterSprite(), t(13), t(23) + 1);
		drawText("PTS", color, font6, t(25), t(25));
		drawMsPacManCopyright(t(6), t(28));
		drawLevelCounter();
	}
}