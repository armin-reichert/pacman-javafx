/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpritesheet;

/**
 * @author Armin Reichert
 */
public class PacManCreditScene extends GameScene2D {

	@Override
	protected PacManGameSpritesheet gss() {
		return (PacManGameSpritesheet) super.gss();
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.anyPressed(PacManGames2d.KEY_ADD_CREDIT, PacManGames2d.KEY_ADD_CREDIT_NUMPAD)) {
			context.ui().addCredit();
		} else if (Keyboard.anyPressed(PacManGames2d.KEY_START_GAME, PacManGames2d.KEY_START_GAME_NUMPAD)) {
			context.ui().startGame();
		}
	}

	@Override
	public void drawSceneContent() {
		var font6 = context.ui().theme().font("font.arcade", s(6));
		drawText("PUSH START BUTTON", ArcadeTheme.ORANGE, sceneFont(), t(6), t(17));
		drawText("1 PLAYER ONLY", ArcadeTheme.CYAN, sceneFont(), t(8), t(21));
		drawText("BONUS PAC-MAN FOR 10000", ArcadeTheme.ROSE, sceneFont(), t(1), t(25));
		drawText("PTS", ArcadeTheme.ROSE, font6, t(25), t(25));
		drawMidwayCopyright(t(4), t(29));
		drawLevelCounter(t(24), t(34), context.game().levelCounter());
	}
}