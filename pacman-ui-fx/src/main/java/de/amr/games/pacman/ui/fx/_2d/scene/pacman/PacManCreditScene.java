/*
MIT License

Copyright (c) 2022 Armin Reichert

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

package de.amr.games.pacman.ui.fx._2d.scene.pacman;

import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.Keys;
import de.amr.games.pacman.ui.fx.input.Keyboard;

/**
 * @author Armin Reichert
 */
public class PacManCreditScene extends GameScene2D {

	@Override
	public void init() {
		creditVisible = true;
	}

	@Override
	public void update() {
		// Nothing to do
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(Keys.ADD_CREDIT)) {
			Actions.addCredit();
		} else if (Keyboard.pressed(Keys.START_GAME)) {
			Actions.startGame();
		}
	}

	@Override
	public void drawSceneContent() {
		var r = context.r2D();
		var normalFont = r.arcadeFont(TS);
		var smallFont = r.arcadeFont(6); // TODO looks ugly
		r.drawText(g, "PUSH START BUTTON", Rendering2D.Palette.ORANGE, normalFont, t(6), t(17));
		r.drawText(g, "1 PLAYER ONLY", Rendering2D.Palette.CYAN, normalFont, t(8), t(21));
		r.drawText(g, "BONUS PAC-MAN FOR 10000", Rendering2D.Palette.ROSE, normalFont, t(1), t(25));
		r.drawText(g, "PTS", Rendering2D.Palette.ROSE, smallFont, t(25), t(25));
		r.drawCopyright(g, 29);
		r.drawLevelCounter(g, context.game().levelCounter());
	}
}