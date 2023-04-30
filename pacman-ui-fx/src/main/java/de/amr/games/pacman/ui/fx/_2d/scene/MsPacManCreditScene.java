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

package de.amr.games.pacman.ui.fx._2d.scene;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D.drawText;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.ui.fx._2d.rendering.ArcadeTheme;
import de.amr.games.pacman.ui.fx._2d.rendering.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.Keys;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import javafx.scene.canvas.GraphicsContext;

/**
 * @author Armin Reichert
 */
public class MsPacManCreditScene extends GameScene2D {

	public MsPacManCreditScene(GameController gameController) {
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
		if (Keyboard.pressed(Keys.ADD_CREDIT)) {
			Actions.addCredit();
		} else if (Keyboard.pressed(Keys.START_GAME)) {
			Actions.startGame();
		}
	}

	@Override
	public void drawScene(GraphicsContext g) {
		var r = (MsPacManGameRenderer) context.rendering2D();
		var arcade8 = r.screenFont(TS);
		var arcade6 = r.screenFont(6.0);
		drawText(g, "PUSH START BUTTON", ArcadeTheme.ORANGE, arcade8, TS * (6), TS * (16));
		drawText(g, "1 PLAYER ONLY", ArcadeTheme.ORANGE, arcade8, TS * (8), TS * (18));
		drawText(g, "ADDITIONAL    AT 10000", ArcadeTheme.ORANGE, arcade8, TS * (2), TS * (25));
		r.drawSprite(g, r.lifeSymbolRegion(), TS * (13), TS * (23) + 1);
		drawText(g, "PTS", ArcadeTheme.ORANGE, arcade6, TS * (25), TS * (25));
		r.drawMsPacManCopyright(g, 29);
	}
}