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

package de.amr.games.pacman.ui.fx._2d.scene.mspacman;

import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx._2d.rendering.RendererMsPacManGame;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class MsPacManCreditScene extends GameScene2D {

	private Font arcadeFont8;
	private Font arcadeFont6;

	@Override
	public void init() {
		setCreditVisible(true);
		arcadeFont8 = ctx.r2D().arcadeFont();
		arcadeFont6 = Font.font(arcadeFont8.getName(), 6);
	}

	@Override
	public void update() {
		// Nothing to do
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
		g.setFont(arcadeFont8);
		g.setFill(ctx.r2D().ghostColor(Ghost.ORANGE_GHOST));
		g.fillText("PUSH START BUTTON", t(6), t(16));
		g.fillText("1 PLAYER ONLY", t(8), t(18));
		g.fillText("ADDITIONAL    AT 10000", t(2), t(25));
		var msPacManSprite = ((RendererMsPacManGame) ctx.r2D()).lifeSprite();
		ctx.r2D().drawSprite(g, msPacManSprite, t(13), t(23) + 2.0);
		g.setFont(arcadeFont6);
		g.fillText("PTS", t(25), t(25));
		ctx.r2D().drawCopyright(g, 29);
	}
}