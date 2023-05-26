/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameRenderer;

/**
 * @author Armin Reichert
 */
public class PacManCreditScene extends GameScene2D {

	@Override
	protected PacManGameRenderer r() {
		return (PacManGameRenderer) super.r();
	}

	@Override
	public void init() {
		setSceneCanvasScaled(true);
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
		var font6 = r().theme().font("font.arcade", 6);
		drawText("PUSH START BUTTON", ArcadeTheme.ORANGE, sceneFont(), t(6), t(17));
		drawText("1 PLAYER ONLY", ArcadeTheme.CYAN, sceneFont(), t(8), t(21));
		drawText("BONUS PAC-MAN FOR 10000", ArcadeTheme.ROSE, sceneFont(), t(1), t(25));
		drawText("PTS", ArcadeTheme.ROSE, font6, t(25), t(25));
		drawMidwayCopyright(t(4), t(29));
		drawLevelCounter(t(24), t(34), context.game().levelCounter());
	}
}