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

import static de.amr.games.pacman.lib.Globals.TS;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.ui.fx.app.Game2d;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameRenderer;
import de.amr.games.pacman.ui.fx.rendering2d.Rendering2D;
import javafx.scene.canvas.GraphicsContext;

/**
 * @author Armin Reichert
 */
public class PacManCreditScene extends GameScene2D {

	public PacManCreditScene(GameController gameController) {
		super(gameController);
	}

	@Override
	public void init() {
		context.setCreditVisible(true);
		context.setScoreVisible(true);
	}

	@Override
	public void update() {
		// Nothing to do
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.pressed(Game2d.KEY_ADD_CREDIT)) {
			Game2d.app.addCredit();
		} else if (Keyboard.pressed(Game2d.KEY_START_GAME)) {
			Game2d.app.startGame();
		}
	}

	@Override
	public void drawSceneContent(GraphicsContext g) {
		var r = context.rendering2D();
		Rendering2D.drawText(g, "PUSH START BUTTON", ArcadeTheme.ORANGE, Game2d.assets.arcadeFont, TS * (6), TS * (17));
		Rendering2D.drawText(g, "1 PLAYER ONLY", ArcadeTheme.CYAN, Game2d.assets.arcadeFont, TS * (8), TS * (21));
		Rendering2D.drawText(g, "BONUS PAC-MAN FOR 10000", ArcadeTheme.ROSE, Game2d.assets.arcadeFont, TS * (1), TS * (25));
		Rendering2D.drawText(g, "PTS", ArcadeTheme.ROSE, Game2d.assets.arcadeFont6, TS * (25), TS * (25));
		PacManGameRenderer.drawMidwayCopyright(g, 4, 29);
		r.drawLevelCounter(g, context.game().levelCounter());
	}
}