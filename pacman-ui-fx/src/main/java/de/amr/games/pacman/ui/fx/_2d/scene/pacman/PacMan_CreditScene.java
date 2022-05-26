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

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx._2d.entity.common.Credit2D;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.shell.GameUI;
import de.amr.games.pacman.ui.fx.sound.GameSound;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class PacMan_CreditScene extends GameScene2D {

	private Credit2D credit2D;

	public PacMan_CreditScene(GameController gameController, V2i unscaledSize) {
		super(gameController, unscaledSize);
	}

	@Override
	public void init() {
		credit2D = new Credit2D(r2D, gameController::credit);
	}

	@Override
	public void handleKeyPressed(KeyEvent e) {
		if (GameUI.pressed(e, KeyCode.DIGIT5)) {
			SoundManager.get().play(GameSound.CREDIT);
			gameController.addCredit();
			return;
		} else if (GameUI.pressed(e, KeyCode.SPACE) || GameUI.pressed(e, KeyCode.DIGIT1)) {
			gameController.requestGame();
			return;
		}
	}

	@Override
	protected void doUpdate() {
	}

	@Override
	protected void doRender(GraphicsContext g) {
		credit2D.render(g);

		g.setFill(Color.WHITE);
		g.setFont(Font.font("Arial", 20));
		g.fillText("CREDIT SCENE", 20, 100);
	}
}