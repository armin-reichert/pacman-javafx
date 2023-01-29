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

import static de.amr.games.pacman.model.common.world.World.TS;
import static de.amr.games.pacman.model.common.world.World.t;

import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.ui.fx.Actions;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.MsPacManGameRenderer;
import de.amr.games.pacman.ui.fx._2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import javafx.scene.input.KeyCode;

/**
 * @author Armin Reichert
 */
public class MsPacManCreditScene extends GameScene2D {

	@Override
	public void init() {
		setCreditVisible(true);
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
	public void drawSceneContent() {
		var r = (MsPacManGameRenderer) ctx.r2D();
		var msPacManSprite = r.lifeSprite();
		var arcade8 = r.arcadeFont(TS);
		var arcade6 = r.arcadeFont(6.0);
		r.drawText(g, "PUSH START BUTTON", r.ghostColor(Ghost.ID_ORANGE_GHOST), arcade8, t(6), t(16));
		r.drawText(g, "1 PLAYER ONLY", r.ghostColor(Ghost.ID_ORANGE_GHOST), arcade8, t(8), t(18));
		r.drawText(g, "ADDITIONAL    AT 10000", r.ghostColor(Ghost.ID_ORANGE_GHOST), arcade8, t(2), t(25));
		r.drawSprite(g, msPacManSprite, t(13), t(23) + 1);
		r.drawText(g, "PTS", r.ghostColor(Ghost.ID_ORANGE_GHOST), arcade6, t(25), t(25));
		r.drawCopyright(g, 29);
	}
}