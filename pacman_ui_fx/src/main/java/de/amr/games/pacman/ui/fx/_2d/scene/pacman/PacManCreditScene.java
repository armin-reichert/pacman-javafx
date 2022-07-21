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

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.shell.Keyboard;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class PacManCreditScene extends GameScene2D {

	private Font arcade6;

	@Override
	public void init() {
		hud.creditVisible = true;
		arcade6 = Font.font(ctx.r2D.getArcadeFont().getName(), 6);
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(KeyCode.DIGIT5)) {
			Actions.addCredit();
		} else if (Keyboard.pressed(KeyCode.DIGIT1)) {
			Actions.startGame();
		}
	}

	@Override
	public void drawSceneContent(GraphicsContext g) {
		g.setFont(ctx.r2D.getArcadeFont());
		g.setFill(ctx.r2D.getGhostColor(Ghost.ORANGE_GHOST));
		g.fillText("PUSH START BUTTON", t(6), t(17));

		g.setFont(ctx.r2D.getArcadeFont());
		g.setFill(ctx.r2D.getGhostColor(Ghost.CYAN_GHOST));
		g.fillText("1 PLAYER ONLY", t(8), t(21));

		g.setFont(ctx.r2D.getArcadeFont());
		g.setFill(Color.rgb(255, 184, 174));
		g.fillText("BONUS PAC-MAN FOR 10000", t(1), t(25));

		g.setFont(arcade6);
		g.fillText("PTS", t(25), t(25));

		ctx.r2D.drawCopyright(g, 29);
	}
}