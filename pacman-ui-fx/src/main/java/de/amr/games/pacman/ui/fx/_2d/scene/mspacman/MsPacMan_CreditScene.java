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
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Spritesheet_MsPacMan;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.shell.Keyboard;
import de.amr.games.pacman.ui.fx.sound.GameSound;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class MsPacMan_CreditScene extends GameScene2D {

	@Override
	public void init() {
		creditVisible = true;
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(KeyCode.DIGIT5)) {
			SoundManager.get().play(GameSound.CREDIT);
			gameController.addCredit();
		} else if (Keyboard.pressed(KeyCode.DIGIT1)) {
			gameController.requestGame();
		}
	}

	@Override
	protected void doUpdate() {
	}

	@Override
	protected void doRender(GraphicsContext g) {
		r2D.drawScore(g, game.scores.gameScore);
		r2D.drawScore(g, game.scores.highScore);
		r2D.drawCredit(g, gameController.credit(), creditVisible);
		g.setFont(r2D.getArcadeFont());
		g.setFill(r2D.getGhostColor(Ghost.ORANGE_GHOST));
		g.fillText("PUSH START BUTTON", t(6), t(16));
		g.fillText("1 PLAYER ONLY", t(8), t(18));
		g.fillText("ADDITIONAL    AT 10000", t(2), t(25));
		Spritesheet_MsPacMan r2DMs = (Spritesheet_MsPacMan) r2D;
		r2D.drawSprite(g, r2DMs.rhs(1, 0), t(13), t(23) + 2);
		g.setFont(Font.font(r2D.getArcadeFont().getName(), 6));
		g.fillText("PTS", t(25), t(25));
		r2D.drawCopyright(g, 29);
	}
}